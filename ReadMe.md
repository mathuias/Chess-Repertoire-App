# Getting Started

## Start the Application
### How to run the setup on local 

1. Install Vue deps:
cd /Users/mathuias/DEV/Projects/Chess-Repertoire-App/vue-ui && npm install

2. Start the Spring Backend with the test profile (so the seeded users exist):
cd /Users/mathuias/DEV/Projects/Chess-Repertoire-App && ./gradlew bootRun --args='--spring.profiles.active=localdev'

3. Start the Vite dev server in another terminal:
cd /Users/mathuias/DEV/Projects/Chess-Repertoire-App/vue-ui && npm run dev

4. Open http://localhost:5173/. You'll be redirected to /login with the form pre-filled with alice@example.com / password. Click Sign in → JWT lands in localStorage → routed to /hello → "Hello World!" appears.

5. Start local postgres database or simply use H2 for local 


## Setup Database

### Install postgres database (on mac)
brew install postgresql@17
brew services start postgresql@17
brew services list 

// check connection
psql -U postgres -d postgres -h localhost

// stop db service if not needed anymore
brew services stop postgresql@17

### Set up the database
createdb postgres

// init db
psql postgres

// create user and grant priviledges
CREATE USER postgres WITH PASSWORD 'admin';

### grant all permissions to user
GRANT ALL PRIVILEGES ON DATABASE postgres TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA public TO postgres;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO postgres;

## Setup the Vue Frontend
### Clean the node dependencies 
rm -rf node_modules
rm package-lock.json
npm cache clean --force



## Developers Troubleshoot
### Test Login with 
curl -X POST http://localhost:8080/api/auth/login \
        -H 'Content-Type: application/json' \
        -d '{"email":"alice@example.com","password":"password"}'




## Helpful Links
### Reference Documentation
For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/4.0.6/gradle-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/4.0.6/gradle-plugin/packaging-oci-image.html)
* [Spring Web](https://docs.spring.io/spring-boot/4.0.6/reference/web/servlet.html)
* [Spring Data JPA](https://docs.spring.io/spring-boot/4.0.6/reference/data/sql.html#data.sql.jpa-and-spring-data)
* [Spring Security](https://docs.spring.io/spring-boot/4.0.6/reference/web/spring-security.html)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
* [Securing a Web Application](https://spring.io/guides/gs/securing-web/)
* [Spring Boot and OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
* [Authenticating a User with LDAP](https://spring.io/guides/gs/authenticating-ldap/)

### Additional Links
These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)