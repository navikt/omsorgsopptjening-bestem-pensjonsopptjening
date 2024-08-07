import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val domeneVersion = "1.0.70"
val azureAdClient = "0.0.7"
val jacksonVersion = "2.17.1"
val logbackEncoderVersion = "8.0"
val postgresqlVersion = "42.7.3"
val flywayCoreVersion = "9.22.3" // 10.x krever nyere postgres-database
val springKafkaTestVersion = "3.2.2"
val springCloudContractVersion = "4.0.4"
val testcontainersVersion = "1.20.1"
val mockkVersion = "1.13.12"
val assertJVersion = "3.26.3"
val jsonAssertVersion = "1.5.3"
val wiremockVersion = "3.9.1"
val mockitoVersion = "5.4.0"
val unleashVersion = "9.2.4"
val navTokenSupportVersion = "5.0.1"
val hibernateValidatorVersion = "8.0.1.Final"
val junit5Version = "5.10.3"


val snakeYamlVersion = "2.2"
val snappyJavaVersion = "1.1.10.5"
val httpClient5Version = "5.3.1"
val httpClientVersion = "4.5.14" // deprecated, men brukes av

plugins {
    val kotlinVersion = "2.0.0"
    id("org.jetbrains.kotlin.jvm") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.jpa") version kotlinVersion
    id("org.springframework.boot") version "3.3.2"
    id("com.github.ben-manes.versions") version "0.51.0"
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
    testImplementation("org.wiremock:wiremock-jetty12:$wiremockVersion")
    testImplementation("org.assertj:assertj-core:$assertJVersion")
    testImplementation("no.nav.security:token-validation-spring-test:$navTokenSupportVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit5Version")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit5Version")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget.set(JvmTarget.JVM_21)
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
