# Getting Started

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

### How to run the setup on local 

1. Install Vue deps:
cd /Users/mathuias/DEV/Projects/Spring/vue-ui && npm install

2. Start the Spring backend with the test profile (so the seeded users exist):
cd /Users/mathuias/DEV/Projects/Spring && ./gradlew bootRun --args='--spring.profiles.active=localdev'

3. Start the Vite dev server in another terminal:
cd /Users/mathuias/DEV/Projects/Spring/vue-ui && npm run dev

4. Open http://localhost:5173/. You'll be redirected to /login with the form pre-filled with alice@example.com / password. Click Sign in → JWT lands in localStorage → routed to /hello → "Hello World!" appears.

5. Start local postgres database
brew install postgresql@17
brew services start postgresql@17

### Test Login with 
curl -X POST http://localhost:8080/api/auth/login \
        -H 'Content-Type: application/json' \
        -d '{"email":"alice@example.com","password":"password"}'


### 