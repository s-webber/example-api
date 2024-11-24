package com.example.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.nio.file.Files;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import com.example.model.Client;

@TestInstance(TestInstance.Lifecycle.PER_CLASS) // allows us to us @BeforeAll with a non-static method
public class ClientRepositoryTest {
   private ClientRepository clientRepository;
   private EmbeddedDatabase embeddedDatabase;

   @BeforeAll
   void setupDatabase() throws Exception {
      embeddedDatabase = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).setName("ClientRepositoryTest").build();
      JdbcTemplate jdbcTemplate = new JdbcTemplate(embeddedDatabase);
      jdbcTemplate.execute(Files.readString(new File("src/sql/tables.sql").toPath()));
      jdbcTemplate.execute("""
                  INSERT INTO client (client_id, name)
                  VALUES
                  (180, 'test client');

                  INSERT INTO certificate (client_id, thumbprint, active)
                  VALUES
                  (180, 'thumbprint-of-active-certificate', true),
                  (180, 'thumbprint-of-inactive-certificate', false);
                                    """);

      clientRepository = new ClientRepository(jdbcTemplate);
   }

   @Test
   void active() {
      Client client = clientRepository.findByThumbprint("thumbprint-of-active-certificate").get();
      assertEquals(180, client.id());
      assertEquals("test client", client.name());
   }

   @Test
   void inactive() {
      Optional<Client> optional = clientRepository.findByThumbprint("thumbprint-of-inactive-certificate");
      assertFalse(optional.isPresent());
   }

   @Test
   void missing() {
      Optional<Client> optional = clientRepository.findByThumbprint("thumbprint-not-in-database");
      assertFalse(optional.isPresent());
   }

   @AfterAll
   void shutdownDatabase() {
      embeddedDatabase.shutdown();
   }
}
