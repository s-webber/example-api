package com.example.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.example.model.Client;

import jakarta.servlet.Filter;

@SpringBootTest
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
public class PatientControllerTest {
   private static final String CREATE = "/patient/create";
   private static final String UPDATE = "/patient/update";
   private static final Client DUMMY_CLIENT = new Client(42, "dummy client name");

   @Autowired
   private JdbcTemplate jdbcTemplate;

   @Autowired
   private WebApplicationContext webApplicationContext;

   private MockMvc mockMvc;

   @BeforeEach
   void setUp(RestDocumentationContextProvider restDocumentation) {
      // reset database to consistent state before each test
      jdbcTemplate.execute("DELETE FROM patient");

      // use stub filter that always includes a Client in the request
      Filter filter = (request, response, chain) -> {
         request.setAttribute("client", DUMMY_CLIENT);
         chain.doFilter(request, response);
      };

      RestDocumentationResultHandler document = document("{method-name}", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()));
      mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).addFilters(filter).apply(documentationConfiguration(restDocumentation)).alwaysDo(document).build();
   }

   @Test
   void create() throws Exception {
      mockMvc.perform(post(CREATE).contentType(MediaType.APPLICATION_JSON).content("""
                  {
                     "nhsNumber": "9999999999",
                     "name": "dummy name",
                     "dateOfBirth": "2000-12-31",
                     "address": "dummy address",
                     "postcode": "AA1 1AA",
                     "telephoneNumber" : "dummy phone",
                     "emailAddress": "test@test.com"
                  }
                  """)).andExpect(status().isCreated()).andExpect(content().string("")).andReturn();

      assertEquals(Map.of("DATE_OF_BIRTH", date("2000-12-31"), "PATIENT_ID", 1, "UPDATED_BY", DUMMY_CLIENT.id(), "ADDRESS", "dummy address", "EMAIL_ADDRESS", "test@test.com",
                  "NAME", "dummy name", "NHS_NUMBER", "9999999999", "POSTCODE", "AA1 1AA", "TELEPHONE_NUMBER", "dummy phone"), selectPatient());
   }

   @Test
   void update() throws Exception {
      // insert patient before calling api, so there is a patient that can be updated
      insertPatient();
      assertEquals(Map.of("DATE_OF_BIRTH", date("1900-01-01"), "PATIENT_ID", 999999, "UPDATED_BY", -1, "ADDRESS", "", "EMAIL_ADDRESS", "", "NAME", "", "NHS_NUMBER", "9999999999",
                  "POSTCODE", "", "TELEPHONE_NUMBER", ""), selectPatient());

      mockMvc.perform(post(UPDATE).contentType(MediaType.APPLICATION_JSON).content("""
                  {
                     "nhsNumber": "9999999999",
                     "name": "dummy name",
                     "dateOfBirth": "2000-12-31",
                     "address": "dummy address",
                     "postcode": "AA1 1AA",
                     "telephoneNumber" : "dummy phone",
                     "emailAddress": "test@test.com"
                  }
                  """)).andExpect(status().isOk()).andExpect(content().string("")).andReturn();

      assertEquals(Map.of("DATE_OF_BIRTH", date("2000-12-31"), "PATIENT_ID", 999999, "UPDATED_BY", DUMMY_CLIENT.id(), "ADDRESS", "dummy address", "EMAIL_ADDRESS", "test@test.com",
                  "NAME", "dummy name", "NHS_NUMBER", "9999999999", "POSTCODE", "AA1 1AA", "TELEPHONE_NUMBER", "dummy phone"), selectPatient());
   }

   @Test
   void createNhsNumberAlreadyExists() throws Exception {
      // insert patient before calling api, to confirm cannot create another patient with the same NHS number
      insertPatient();

      mockMvc.perform(post(CREATE).contentType(MediaType.APPLICATION_JSON).content("""
                  {
                     "nhsNumber": "9999999999",
                     "name": "dummy name",
                     "dateOfBirth": "2000-12-31",
                     "address": "dummy address",
                     "postcode": "AA1 1AA",
                     "telephoneNumber" : "dummy phone",
                     "emailAddress": "test@test.com"
                  }
                  """)).andExpect(status().isConflict()).andExpect(content().string("")).andReturn();

      // assert that the existing details were not updated by this request
      assertEquals(Map.of("DATE_OF_BIRTH", date("1900-01-01"), "PATIENT_ID", 999999, "UPDATED_BY", -1, "ADDRESS", "", "EMAIL_ADDRESS", "", "NAME", "", "NHS_NUMBER", "9999999999",
                  "POSTCODE", "", "TELEPHONE_NUMBER", ""), selectPatient());
   }

   @Test
   void updateNhsNumberNotFound() throws Exception {
      mockMvc.perform(post(UPDATE).contentType(MediaType.APPLICATION_JSON).content("""
                  {
                     "nhsNumber": "9999999999",
                     "name": "dummy name",
                     "dateOfBirth": "2000-12-31",
                     "address": "dummy address",
                     "postcode": "AA1 1AA",
                     "telephoneNumber" : "dummy phone",
                     "emailAddress": "test@test.com"
                  }
                  """)).andExpect(status().isNotFound()).andExpect(content().string("")).andReturn();
   }

   @ParameterizedTest
   @ValueSource(strings = {CREATE, UPDATE})
   void emptyBody(String endpoint) throws Exception {
      mockMvc.perform(post(endpoint).contentType(MediaType.APPLICATION_JSON).content("")).andExpect(status().isBadRequest()).andExpect(content().string("")).andReturn();
   }

   @ParameterizedTest
   @ValueSource(strings = {CREATE, UPDATE})
   void invalidBody(String endpoint) throws Exception {
      mockMvc.perform(post(endpoint).contentType(MediaType.APPLICATION_JSON).content("some text")).andExpect(status().isBadRequest()).andExpect(content().string("")).andReturn();
   }

   @ParameterizedTest
   @ValueSource(strings = {CREATE, UPDATE})
   void unsupportedMediaType(String endpoint) throws Exception {
      mockMvc.perform(post(endpoint).contentType(MediaType.TEXT_PLAIN).content("{}")).andExpect(status().isUnsupportedMediaType()).andExpect(content().string("")).andReturn();
   }

   @ParameterizedTest
   @ValueSource(strings = {CREATE, UPDATE})
   void emptyJson(String endpoint) throws Exception {
      assertBadRequest(endpoint, """
                  {
                  }
                  """, """
                  [
                     "dateOfBirth: must not be null",
                     "name: must not be blank",
                     "nhsNumber: must not be null"
                  ]
                  """);
   }

   @ParameterizedTest
   @ValueSource(strings = {CREATE, UPDATE})
   void invalidValues(String endpoint) throws Exception {
      assertBadRequest(endpoint, """
                  {
                     "nhsNumber": "9434765918",
                     "name": "very long name aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                     "dateOfBirth": "2000-13-01",
                     "address": "very long address aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                     "postcode": "ZZZZ ZZZ",
                     "telephoneNumber" : "long phone number aaa",
                     "emailAddress": "test$test.com"
                  }
                  """, """
                  [
                     "address: size must be between 0 and 100",
                     "dateOfBirth: Invalid date format. Expected format: YYYY-MM-DD",
                     "emailAddress: must be a well-formed email address",
                     "name: size must be between 0 and 100",
                     "nhsNumber: Invalid NHS number format.",
                     "postcode: Invalid postcode format.",
                     "telephoneNumber: size must be between 0 and 20"
                  ]
                  """);
   }

   private void assertBadRequest(String endpoint, String requestBody, String expectedResponseBody) throws Exception {
      // confirm bad request (400) response with expected body
      mockMvc.perform(post(endpoint).contentType(MediaType.APPLICATION_JSON).content(requestBody)).andExpect(status().isBadRequest())
                  .andExpect(content().json(expectedResponseBody, true)).andReturn();

      // confirm no patient records inserted by the request
      assertTrue(selectPatients().isEmpty());
   }

   private Map<String, Object> selectPatient() {
      List<Map<String, Object>> patients = selectPatients();
      assertEquals(1, patients.size());
      return patients.get(0);
   }

   private List<Map<String, Object>> selectPatients() {
      return jdbcTemplate.queryForList("SELECT * FROM patient");
   }

   private void insertPatient() {
      jdbcTemplate.execute("""
                  INSERT INTO patient
                  (patient_id, nhs_number, name, date_of_birth, address, postcode, telephone_number, email_address, updated_by)
                  VALUES
                  (999999, '9999999999', '', '1900-01-01', '', '', '', '', -1);""");
   }

   private Date date(String date) {
      return Date.valueOf(LocalDate.parse(date));
   }
}