Generate Certificates (Example Using OpenSSL)

Generate a self-signed CA certificate:
```
openssl req -x509 -newkey rsa:2048 -keyout ca-key.pem -out ca-cert.pem -days 365 -nodes
```

Generate the server private key and certificate signing request (CSR):

```
openssl req -newkey rsa:2048 -keyout server-key.pem -out server-csr.pem -nodes
```

Sign the server certificate using the CA:
```
openssl x509 -req -in server-csr.pem -CA ca-cert.pem -CAkey ca-key.pem -CAcreateserial -out server-cert.pem -days 365
```

Generate the client private key and CSR:
```
openssl req -newkey rsa:2048 -keyout client-key.pem -out client-csr.pem -nodes
```

Sign the client certificate using the CA:
```
openssl x509 -req -in client-csr.pem -CA ca-cert.pem -CAkey ca-key.pem -CAcreateserial -out client-cert.pem -days 365
```

Combine the client certificate and private key into a PKCS#12 file (for Java clients):
```
openssl pkcs12 -export -out client.p12 -inkey client-key.pem -in client-cert.pem -certfile ca-cert.pem
```

2.1. Add the Server Certificate

Place the server-cert.pem and server-key.pem into a Java KeyStore (JKS) or PKCS#12 format.
```
openssl pkcs12 -export -in server-cert.pem -inkey server-key.pem -out server.p12 -name server-cert
```

Convert the PKCS#12 file into a JKS (optional):
```
keytool -importkeystore -deststorepass changeit -destkeypass changeit -destkeystore server-keystore.jks -srcstorepass changeit -srckeystore server.p12 -srcstoretype PKCS12
```

Import the CA Certificate into the Trust Store
```
keytool -import -trustcacerts -keystore truststore.jks -storepass changeit -noprompt -alias ca-cert -file ca-cert.pem
```

How to Get the SHA-256 Thumbprint of a Certificate
```
openssl x509 -in client-cert.pem -noout -fingerprint -sha256
```
