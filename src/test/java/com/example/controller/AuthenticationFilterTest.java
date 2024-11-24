package com.example.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.FileInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.model.Client;
import com.example.repository.ClientRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthenticationFilterTest {
   private static final String CERTICATE_REQUEST_ATTRIBUTE = "jakarta.servlet.request.X509Certificate";
   private static final String THUMBPRINT = "c28469de477c98dab8655b3af9cb49c31a71edebdf4f61402c66085c29c398f6";
   private static final Client DUMMY_CLIENT = new Client(147, "dummy client");

   private AuthenticationFilter filter;
   private ClientRepository clientRepository;
   private HttpServletRequest request = mock(HttpServletRequest.class);
   private HttpServletResponse response = mock(HttpServletResponse.class);
   private FilterChain chain = mock(FilterChain.class);

   @BeforeEach
   void setUp() {
      request = mock(HttpServletRequest.class);
      response = mock(HttpServletResponse.class);
      chain = mock(FilterChain.class);

      clientRepository = mock(ClientRepository.class);
      filter = new AuthenticationFilter(clientRepository);
   }

   @Test
   void success() throws Exception {
      X509Certificate certificate = createCertificate();

      when(request.getAttribute(CERTICATE_REQUEST_ATTRIBUTE)).thenReturn(new X509Certificate[] {certificate});
      when(clientRepository.findByThumbprint(THUMBPRINT)).thenReturn(Optional.of(DUMMY_CLIENT));

      filter.doFilterInternal(request, response, chain);

      verify(request).getAttribute(CERTICATE_REQUEST_ATTRIBUTE);
      verify(clientRepository).findByThumbprint(THUMBPRINT);
      verify(request).setAttribute("client", DUMMY_CLIENT);
      verify(chain).doFilter(request, response);
      verifyNoMoreInteractions(clientRepository, request, response, chain);
   }

   @Test
   void unknownCertificates() throws Exception {
      X509Certificate certificate = createCertificate();

      when(request.getAttribute(CERTICATE_REQUEST_ATTRIBUTE)).thenReturn(new X509Certificate[] {certificate});
      when(clientRepository.findByThumbprint(THUMBPRINT)).thenReturn(Optional.empty());

      filter.doFilterInternal(request, response, chain);

      verify(request).getAttribute(CERTICATE_REQUEST_ATTRIBUTE);
      verify(clientRepository).findByThumbprint(THUMBPRINT);
      verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      verifyNoMoreInteractions(clientRepository, request, response, chain);
   }

   @Test
   void nullCertificates() throws Exception {
      when(request.getAttribute(CERTICATE_REQUEST_ATTRIBUTE)).thenReturn(null);

      filter.doFilterInternal(request, response, chain);

      verify(request).getAttribute(CERTICATE_REQUEST_ATTRIBUTE);
      verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      verifyNoMoreInteractions(clientRepository, request, response, chain);
   }

   @Test
   void emptyArrayCerticates() throws Exception {
      when(request.getAttribute(CERTICATE_REQUEST_ATTRIBUTE)).thenReturn(new X509Certificate[0]);

      filter.doFilterInternal(request, response, chain);

      verify(request).getAttribute(CERTICATE_REQUEST_ATTRIBUTE);
      verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      verifyNoMoreInteractions(clientRepository, request, response, chain);
   }

   private X509Certificate createCertificate() throws Exception {
      FileInputStream fis = new FileInputStream("src/test/resources/certificates/client-cert.pem");
      CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
      X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(fis);
      return certificate;
   }
}
