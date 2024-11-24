package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.example.HttpsClientWithCertificate.Response;

@ActiveProfiles("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTest {
   /** Used to tolerate possible differences in timestamps between application and database */
   private static final long TIME_TOLERANCE = 2000;

   @LocalServerPort
   private int port;

   @Autowired
   private JdbcTemplate jdbcTemplate;

   @BeforeEach
   void setupDatabase() throws Exception {
      jdbcTemplate.execute("""
                  DROP TABLE IF EXISTS audit_patient;
                  DROP TABLE IF EXISTS patient;
                  DROP TABLE IF EXISTS certificate;
                  DROP TABLE IF EXISTS client;
                  """);
      jdbcTemplate.execute(Files.readString(new File("src/sql/tables.sql").toPath()));
      jdbcTemplate.execute(Files.readString(new File("src/sql/triggers.sql").toPath()));
      jdbcTemplate.execute("""
                  INSERT INTO client (client_id, name)
                  VALUES
                  (42, 'test client');

                  INSERT INTO certificate (client_id, thumbprint, active)
                  VALUES
                  (42, 'c28469de477c98dab8655b3af9cb49c31a71edebdf4f61402c66085c29c398f6', true);
                  """);
   }

   @Test
   void test() throws Exception {
      HttpsClientWithCertificate client = HttpsClientWithCertificate.create();
      String root = "https://localhost:" + port + "/patient/";
      String createUrl = root + "create";
      String updateUrl = root + "update";
      Timestamp startTime = new Timestamp(System.currentTimeMillis() - TIME_TOLERANCE);

      // send request with invalid NHS number - expect bad request
      assertEquals(new Response(400, "[\"nhsNumber: Invalid NHS number format.\"]"), client.send(updateUrl, """
                  {
                     "nhsNumber": "9999999998",
                     "name": "test invalid nhs number",
                     "dateOfBirth": "2000-12-31",
                     "address": "dummy address",
                     "postcode": "AA1 1AA",
                     "telephoneNumber" : "dummy phone",
                     "emailAddress": "test@test.com"
                  }
                  """));

      // send request to update patient with NHS number that does not exist in the database - expect not found
      assertEquals(new Response(404, null), client.send(updateUrl, """
                  {
                     "nhsNumber": "9999999999",
                     "name": "test update failed as no record of NHS number",
                     "dateOfBirth": "2000-12-31",
                     "address": "dummy address",
                     "postcode": "AA1 1AA",
                     "telephoneNumber" : "dummy phone",
                     "emailAddress": "test@test.com"
                  }
                  """));

      // send request to create patient with NHS number that does not exist in the database - expect created
      assertEquals(new Response(201, ""), client.send(createUrl, """
                  {
                     "nhsNumber": "9999999999",
                     "name": "test create success",
                     "dateOfBirth": "2000-12-31",
                     "address": "dummy address",
                     "postcode": "AA1 1AA",
                     "telephoneNumber" : "dummy phone",
                     "emailAddress": "test@test.com"
                  }
                  """));

      // send request to create patient with NHS number that does exist in the database - expect conflict
      assertEquals(new Response(409, null), client.send(createUrl, """
                  {
                     "nhsNumber": "9999999999",
                     "name": "test create failed as NHS number already exists",
                     "dateOfBirth": "2000-12-31",
                     "address": "dummy address",
                     "postcode": "AA1 1AA",
                     "telephoneNumber" : "dummy phone",
                     "emailAddress": "test@test.com"
                  }
                  """));

      // send request to update patient with NHS number that does exist in the database - expect ok
      assertEquals(new Response(200, ""), client.send(updateUrl, """
                  {
                     "nhsNumber": "9999999999",
                     "name": "test update success",
                     "dateOfBirth": "2001-02-03",
                     "address": "updated address",
                     "postcode": "ZZ9 9ZZ",
                     "telephoneNumber" : "updated phone",
                     "emailAddress": "updated@test.com"
                  }
                  """));

      // make certificate inactive so subsequent requests fail with unauthorised
      jdbcTemplate.execute("UPDATE certificate SET active = false");
      assertEquals(new Response(401, null), client.send(createUrl, """
                  {
                     "nhsNumber": "9999999999",
                     "name": "test inactive certificate",
                     "dateOfBirth": "2000-12-31",
                     "address": "dummy address",
                     "postcode": "AA1 1AA",
                     "telephoneNumber" : "dummy phone",
                     "emailAddress": "test@test.com"
                  }
                  """));

      // make certificate active but update thumbprint so subsequent requests fail with unauthorised
      jdbcTemplate.execute("UPDATE certificate SET active = true, thumbprint='x'");
      assertEquals(new Response(401, null), client.send(updateUrl, """
                  {
                     "nhsNumber": "9999999999",
                     "name": "test unknown certificate",
                     "dateOfBirth": "2000-12-31",
                     "address": "dummy address",
                     "postcode": "AA1 1AA",
                     "telephoneNumber" : "dummy phone",
                     "emailAddress": "test@test.com"
                  }
                  """));

      Timestamp endTime = new Timestamp(System.currentTimeMillis() + TIME_TOLERANCE);

      // assert one patient record and has correct values
      List<Map<String, Object>> patients = jdbcTemplate.queryForList("SELECT * FROM patient");
      assertEquals(1, patients.size());
      Map<String, Object> patient = patients.get(0);
      Timestamp createdTime = (Timestamp) patient.remove("created_date_time");
      Timestamp updatedTime = (Timestamp) patient.remove("updated_date_time");
      assertEquals(Map.of("patient_id", 1, "nhs_number", "9999999999", "name", "test update success", "date_of_birth", date("2001-02-03"), "address", "updated address", "postcode",
                  "ZZ9 9ZZ", "telephone_number", "updated phone", "email_address", "updated@test.com", "updated_by", 42), patient);
      assertAfter(createdTime, startTime);
      assertAfter(updatedTime, createdTime);
      assertBefore(updatedTime, endTime);
      // assert two audit_patient records
      List<Map<String, Object>> audits = jdbcTemplate.queryForList("SELECT * FROM audit_patient");
      assertEquals(2, audits.size());

      // assert first audit record has correct values
      Map<String, Object> firstAudit = audits.get(0);
      assertEquals(1, firstAudit.remove("audit_patient_id"));
      assertEquals("I", firstAudit.remove("change_mode"));
      assertEquals(createdTime, firstAudit.remove("created_date_time"));
      assertBefore(((Timestamp) firstAudit.remove("updated_date_time")), updatedTime);
      Timestamp firstAuditTimestamp = (Timestamp) firstAudit.remove("change_db_date_time");
      assertAfter(firstAuditTimestamp, startTime);
      assertEquals(Map.of("patient_id", 1, "nhs_number", "9999999999", "name", "test create success", "date_of_birth", date("2000-12-31"), "address", "dummy address", "postcode",
                  "AA1 1AA", "telephone_number", "dummy phone", "email_address", "test@test.com", "updated_by", 42), firstAudit);

      // assert second audit record has correct values
      Map<String, Object> secondAudit = audits.get(1);
      assertEquals(2, secondAudit.remove("audit_patient_id"));
      assertEquals("U", secondAudit.remove("change_mode"));
      assertEquals(createdTime, secondAudit.remove("created_date_time"));
      assertEquals(updatedTime, secondAudit.remove("updated_date_time"));
      Timestamp secondAuditTimestamp = (Timestamp) secondAudit.remove("change_db_date_time");
      assertAfter(secondAuditTimestamp, firstAuditTimestamp);
      assertBefore(secondAuditTimestamp, endTime);
      assertEquals(Map.of("patient_id", 1, "nhs_number", "9999999999", "name", "test update success", "date_of_birth", date("2001-02-03"), "address", "updated address", "postcode",
                  "ZZ9 9ZZ", "telephone_number", "updated phone", "email_address", "updated@test.com", "updated_by", 42), secondAudit);
   }

   private void assertAfter(Timestamp t1, Timestamp t2) {
      assertTrue(t1.after(t2), () -> t1 + "not after " + t2);
   }

   private void assertBefore(Timestamp t1, Timestamp t2) {
      assertTrue(t1.before(t2), () -> t1 + "not before " + t2);
   }

   private Date date(String date) {
      return Date.valueOf(LocalDate.parse(date));
   }
}
