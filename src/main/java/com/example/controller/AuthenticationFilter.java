package com.example.controller;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Optional;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.model.Client;
import com.example.repository.ClientRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Checks request contains valid certificate before forwarding request to controller.
 */
@Component
public class AuthenticationFilter extends OncePerRequestFilter {
   private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationFilter.class);

   private final ClientRepository clientRepository;

   public AuthenticationFilter(ClientRepository clientRepository) {
      this.clientRepository = clientRepository;
   }

   @Override
   protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
      MDC.clear();

      try {
         X509Certificate[] certificates = (X509Certificate[]) request.getAttribute("jakarta.servlet.request.X509Certificate");
         if (certificates == null || certificates.length == 0) {
            LOGGER.warn("no certificates in request");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
         }

         Optional<Client> optional = findClient(certificates);
         if (optional.isEmpty()) {
            LOGGER.warn("cannot find client for certificate");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
         }

         Client client = optional.get();
         request.setAttribute("client", client);
         MDC.put("client_name", client.name());
      } catch (Exception e) {
         LOGGER.error("could not authenticate", e);
         response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
         return;
      }

      filterChain.doFilter(request, response);
   }

   private Optional<Client> findClient(X509Certificate[] certificates) {
      X509Certificate clientCert = certificates[0]; // TODO
      String subjectDN = clientCert.getSubjectX500Principal().getName();
      String issuerDN = clientCert.getIssuerX500Principal().getName();
      String serialNumber = clientCert.getSerialNumber().toString();
      Date notAfter = clientCert.getNotAfter();

      LOGGER.info("Client Certificate Details:  Subject: {}  Issuer: {}  Serial Number: {}  Not After {}", subjectDN, issuerDN, serialNumber, notAfter);

      String thumbprint = getCertificateThumbprint(clientCert);
      return clientRepository.findByThumbprint(thumbprint);
   }

   public static String getCertificateThumbprint(X509Certificate certificate) {
      try {
         byte[] encodedCert = certificate.getEncoded();
         MessageDigest digest = MessageDigest.getInstance("SHA-256");
         byte[] hash = digest.digest(encodedCert);

         return Hex.encodeHexString(hash);
      } catch (CertificateEncodingException | NoSuchAlgorithmException e) {
         throw new RuntimeException("cannot determine certificate thumbprint", e);
      }
   }
}
