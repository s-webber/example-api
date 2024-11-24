package com.example;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpsClientWithCertificate {
   public static HttpsClientWithCertificate create() throws Exception {
      String keystorePath = "src/test/resources/certificates/client.p12";
      String keystorePassword = "password";

      KeyStore keyStore = KeyStore.getInstance("PKCS12");
      keyStore.load(new FileInputStream(keystorePath), keystorePassword.toCharArray());

      SSLContext sslContext = SSLContext.getInstance("TLS");
      KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      kmf.init(keyStore, keystorePassword.toCharArray());

      // Trust all certificates (ignoring trust issues)
      TrustManager[] trustAllCertificates = new TrustManager[] {new X509TrustManager() {
         @Override
         public X509Certificate[] getAcceptedIssuers() {
            return null;
         }

         @Override
         public void checkClientTrusted(X509Certificate[] certs, String authType) {
         }

         @Override
         public void checkServerTrusted(X509Certificate[] certs, String authType) {
         }
      }};

      sslContext.init(kmf.getKeyManagers(), trustAllCertificates, new java.security.SecureRandom());

      HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

      // Set a HostnameVerifier that accepts all hostnames (unsafe, for testing purposes only)
      HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

      return new HttpsClientWithCertificate();
   }

   private HttpsClientWithCertificate() {
   }

   Response send(String path, String requestBody) throws Exception {
      URL url = new URL(path);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      try {
         connection.setRequestMethod("POST");
         connection.setRequestProperty("Content-Type", "application/json");
         connection.setDoOutput(true);

         try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes("utf-8");
            os.write(input, 0, input.length);
         }

         return parseResponse(connection);
      } finally {
         connection.disconnect();
      }
   }

   private Response parseResponse(HttpURLConnection connection) throws IOException {
      int responseCode = connection.getResponseCode();
      String responseBody;

      BufferedReader reader = null;
      try {
         if (responseCode >= 200 && responseCode < 300) {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
         } else {
            reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
         }

         StringBuilder sb = new StringBuilder();
         String line;
         while ((line = reader.readLine()) != null) {
            sb.append(line);
         }
         responseBody = sb.toString();
      } catch (Exception e) {
         if (reader != null) {
            reader.close();
         }
         responseBody = null;
      }

      return new Response(connection.getResponseCode(), responseBody);
   }

   record Response(int code, String body) {
   }
}
