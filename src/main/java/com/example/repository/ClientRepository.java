package com.example.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.model.Client;

@Repository
public class ClientRepository {
   private final JdbcTemplate jdbcTemplate;

   public ClientRepository(JdbcTemplate jdbcTemplate) {
      this.jdbcTemplate = jdbcTemplate;
   }

   public Optional<Client> findByThumbprint(String thumbprint) {
      List<Client> clients = jdbcTemplate.query("""
                  SELECT client.client_id, client.name
                  FROM client
                  INNER JOIN certificate ON client.client_id = certificate.client_id
                  WHERE certificate.active
                  AND certificate.thumbprint = ?
                  """, (rs, rowNum) -> new Client(rs.getInt("client_id"), rs.getString("name")), thumbprint);

      if (clients.size() == 1) {
         return Optional.of(clients.get(0));
      } else if (clients.isEmpty()) {
         return Optional.empty();
      } else {
         throw new IllegalStateException("found " + clients.size() + " clients");
      }
   }
}
