[![Build and test with Maven and Postgres](https://github.com/s-webber/example-api/actions/workflows/ci-maven-postgres.yml/badge.svg?branch=dev)](https://github.com/s-webber/example-api/actions/workflows/ci-maven-postgres.yml)

API that inserts and updates records of a Postgres database. Uses client certificates for authentication.

Can build using:

```
mvnw install
```

Documentation can be viewed at:

```
target/generated-docs/index.html
```

Code coverage report can be viewed at:

```
target/site/jacoco/index.html
```

Application can be started using:

```
java -Dspring.profiles.active=integration -jar target/patient-api-0.0.1-SNAPSHOT.jar
```
