import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val domeneVersion = "2.1.101"
val azureAdClient = "0.0.7"
val logbackEncoderVersion = "9.0"
val flywayCoreVersion = "12.6.0"
val wiremockVersion = "3.13.2"
val mockitoVersion = "6.3.0"
val unleashVersion = "9.2.6"
val navTokenSupportVersion = "6.0.8"

plugins {
    val kotlinVersion = "2.3.21"
    id("org.jetbrains.kotlin.jvm") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.jpa") version kotlinVersion
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.github.ben-manes.versions") version "0.54.0"
}

group = "no.nav.pensjon.opptjening"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://maven.pkg.github.com/navikt/maven-release") {
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework:spring-aspects")
    implementation("org.springframework.retry:spring-retry:2.0.13")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("io.getunleash:unleash-client-java:$unleashVersion")
    implementation("no.nav.security:token-validation-spring:$navTokenSupportVersion")
    implementation("no.nav.security:token-client-spring:$navTokenSupportVersion")
    implementation("org.hibernate.validator:hibernate-validator") // version managed by BOM

    // Apache HttpClient 5 for connection pool management (version managed by BOM)
    implementation("org.apache.httpcomponents.client5:httpclient5")

    // Internal libraries
    implementation("no.nav.pensjon.opptjening:omsorgsopptjening-domene-lib:$domeneVersion")
    implementation("no.nav.pensjonopptjening:pensjon-opptjening-azure-ad-client:$azureAdClient")

    // Spring Boot 4 leverer Jackson 3 (tools.jackson.*) transitivt via startere
    implementation("tools.jackson.module:jackson-module-kotlin")

    // Log and metric
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("net.logstash.logback:logstash-logback-encoder:$logbackEncoderVersion")

    // DB (postgresql version managed by BOM)
    implementation("org.postgresql:postgresql")
    implementation("org.springframework.boot:spring-boot-flyway")
    implementation("org.flywaydb:flyway-database-postgresql:$flywayCoreVersion")

    // Test
    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test") // includes assertj, jsonassert, mockito
    testImplementation("org.testcontainers:postgresql:1.21.4") // ponytail: TC 1.x jdbc:tc driver approach, same as afp-api
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:$mockitoVersion")
    testImplementation("org.wiremock:wiremock-jetty12:$wiremockVersion")
    testImplementation("no.nav.security:token-validation-spring-test:$navTokenSupportVersion")

    // WireMock 3.13 requires Jetty ee10 12.1.x aligned with jetty-core. BOM pins ee10 to 12.0.x.
    testImplementation("org.eclipse.jetty.ee10:jetty-ee10-servlet:12.1.10")
}

// Lokal smoke-run av hele appen mot in-process Kafka + Testcontainers Postgres. Se LocalRun.kt.
tasks.register<JavaExec>("runLocal") {
    group = "application"
    description = "Starter appen lokalt (kafkaIntegrationTest-profil, ingen ekstern infra)"
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.LocalRunKt")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget = JvmTarget.JVM_25
    }
}

tasks.withType<Test> {
    maxParallelForks = 1 // Shared resources (db/wiremock)
    useJUnitPlatform()
    testLogging {
        events(
            org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
        )
    }
}

tasks.withType<DependencyUpdatesTask>().configureEach {
    rejectVersionIf {
        isNonStableVersion(candidate.version)
    }
}

fun isNonStableVersion(version: String): Boolean {
    return listOf("BETA", "RC", "-M", "-rc-", "Alpha").any { version.uppercase().contains(it) }
}
