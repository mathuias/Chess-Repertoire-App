plugins {
	id("idea")
	id("java")
	id("org.springframework.boot") version "4.0.6"

	//id("com.gorylenko.gradle-git-properties") version "2.5.3"
	id("io.spring.dependency-management") version "1.1.7"
	id("com.github.ben-manes.versions") version "0.51.0"

	id("jacoco")
	id("io.qameta.allure") version "3.0.0"
	id("org.owasp.dependencycheck") version "12.1.8"
	//id("com.github.spotbugs") version "5.0.14"

	//id("com.github.hierynomus.license-report") version "0.16.1"
	//id("com.github.jk1.dependency-license-report") version "2.0"

}

// import plugin configurations
//apply from = "gradle/plugins/allure.gradle.kts"
//apply(from = "gradle/plugins/jacoco.gradle.kts")
//apply(from = "gradle/plugins/owasp.gradle.kts")
//apply(from = "gradle/plugins/spotbugs.gradle.kts")

// gitProperties plugin has compatibility issues with Gradle 9.x
// gitProperties {
// 	extProperty = "gitProps"
// 	dateFormat = "yyyy-MM-dd'T'HH:mm:ssZ"
// 	dateFormatTimeZone = "UTC"
// }

group = "dev.mathuias"
version = "0.0.2"
description = "Application to manage your Chess Opening Repertoire"

java {
	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21

	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
	gradlePluginPortal()
}

dependencies {

    compileOnly("org.jspecify:jspecify:1.0.0")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web") 
	implementation("org.springframework.boot:spring-boot-starter-webmvc")

	implementation("org.springframework.boot:spring-boot-liquibase")
	implementation("org.liquibase:liquibase-core")

	implementation("io.micrometer:micrometer-registry-prometheus")

	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

	runtimeOnly("com.h2database:h2")
	runtimeOnly("org.postgresql:postgresql")
	
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:deprecation")
}


