import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val domeneVersion = "1.0.60"
val azureAdClient = "0.0.7"
val jacksonVersion = "2.15.2"
val logbackEncoderVersion = "7.4"
val postgresqlVersion = "42.6.0"
val flywayCoreVersion = "9.22.2"
val springKafkaTestVersion = "3.0.11"
val springCloudContractVersion = "4.0.4"
val testcontainersVersion = "1.19.1"
val mockkVersion = "1.13.8"

val snakeYamlVersion = "1.33" // latest is 2.2, but spring uses 1.x

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.10"
    id("org.jetbrains.kotlin.plugin.spring") version "1.9.10"
    id("org.jetbrains.kotlin.plugin.jpa") version "1.9.10"
    id("org.springframework.boot") version "3.1.4"
    id("com.github.ben-manes.versions") version "0.49.0"
}

apply(plugin = "io.spring.dependency-management")
apply(plugin = "kotlin-jpa")


group = "no.nav.pensjon.opptjening"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
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

    // Test
    testImplementation("org.springframework.kafka:spring-kafka-test:$springKafkaTestVersion")
    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.cloud:spring-cloud-starter-contract-stub-runner:$springCloudContractVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    testImplementation("org.skyscreamer:jsonassert:1.5.1")
    implementation("io.getunleash:unleash-client-java:8.3.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("io.mockk:mockk:${mockkVersion}")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
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
