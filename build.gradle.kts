import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val domeneVersion = "1.0.65"
val azureAdClient = "0.0.7"
val jacksonVersion = "2.15.3"
val logbackEncoderVersion = "7.4"
val postgresqlVersion = "42.7.1"
val flywayCoreVersion = "9.22.3" // 10.x krever nyere postgres-database
val springKafkaTestVersion = "3.1.1"
val springCloudContractVersion = "4.0.4"
val testcontainersVersion = "1.19.3"
val mockkVersion = "1.13.9"
val assertJVersion = "3.25.1"
val jsonAssertVersion = "1.5.1"
val wiremockVersion = "2.35.1"
val mockitoVersion = "5.2.1"
val unleashVersion = "9.2.0"
val navTokenSupportVersion = "4.0.3"
val hibernateValidatorVersion = "8.0.1.Final"


val snakeYamlVersion = "2.2"
val snappyJavaVersion = "1.1.10.5"
val httpClient5Version = "5.3"
val httpClientVersion = "4.5.14" // deprecated, men brukes av

plugins {
    val kotlinVersion = "1.9.22"
    id("org.jetbrains.kotlin.jvm") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.jpa") version kotlinVersion
    id("org.springframework.boot") version "3.2.1"
    id("com.github.ben-manes.versions") version "0.50.0"
}

apply(plugin = "io.spring.dependency-management")
apply(plugin = "kotlin-jpa")


group = "no.nav.pensjon.opptjening"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://maven.pkg.github.com/navikt/maven-release") {
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework:spring-aspects")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("io.getunleash:unleash-client-java:$unleashVersion")
    implementation("no.nav.security:token-validation-spring:$navTokenSupportVersion")

    // Internal libraries
    implementation("no.nav.pensjon.opptjening:omsorgsopptjening-domene-lib:$domeneVersion")
    implementation("no.nav.pensjonopptjening:pensjon-opptjening-azure-ad-client:$azureAdClient")
    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    // Log and metric
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("net.logstash.logback:logstash-logback-encoder:$logbackEncoderVersion")
    // DB
    implementation("org.postgresql:postgresql:$postgresqlVersion")
    implementation("org.flywaydb:flyway-core:$flywayCoreVersion")

    // transitive dependency overrides
    implementation("org.yaml:snakeyaml:$snakeYamlVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("org.xerial.snappy:snappy-java:$snappyJavaVersion")
    implementation("org.apache.httpcomponents.client5:httpclient5:$httpClient5Version")
    implementation("org.hibernate.validator:hibernate-validator:$hibernateValidatorVersion")

    // Test
    testImplementation("org.springframework.kafka:spring-kafka-test:$springKafkaTestVersion")
    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    testImplementation("org.skyscreamer:jsonassert:$jsonAssertVersion")
    testImplementation("org.mockito.kotlin:mockito-kotlin:$mockitoVersion")
    testImplementation("io.mockk:mockk:${mockkVersion}")
    testImplementation("com.github.tomakehurst:wiremock-jre8-standalone:$wiremockVersion")
    testImplementation("org.assertj:assertj-core:$assertJVersion")
    testImplementation("no.nav.security:token-validation-spring-test:$navTokenSupportVersion")



}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    maxParallelForks = 1 //Disable parallell execution due to shared resources (db/wiremock)
    useJUnitPlatform()
    testLogging {
        events(
            org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
        )
    }
}
