package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening

import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.test.EmbeddedKafkaKraftBroker

/**
 * Local smoke-run: boots the FULL application context (Flyway, alle beans, Kafka-lytter og de
 * profil-gatede bakgrunnstaskene) mot en in-process Kafka-broker og en Testcontainers Postgres
 * (via jdbc:tc i src/test/resources/application.yml). Gjenbruker `kafkaIntegrationTest`-profilen
 * som allerede gir PLAINTEXT Kafka, FakeUnleash og mock OAuth2 - ingen ekstern infra kreves.
 *
 * Kjør:  ./gradlew runLocal
 * Sjekk: http://localhost:8080/actuator/health  ->  {"status":"UP"}
 *
 * Appen idler etter oppstart (ingen meldinger i køen), så eksterne HTTP-kall (PDL/PEN/POPP) skjer
 * ikke. Dette verifiserer at wiringen etter Spring Boot 4-migreringen faktisk starter.
 * ponytail: throwaway dev-launcher i test-scope, ikke prod-kode.
 */
fun main(args: Array<String>) {
    val broker = EmbeddedKafkaKraftBroker(1, 1, "pensjonopptjening.omsorgsopptjening")
    broker.afterPropertiesSet()
    System.setProperty("kafka.brokers", broker.brokersAsString)

    SpringApplicationBuilder(Application::class.java, LocalRunConfig::class.java)
        .profiles("kafkaIntegrationTest")
        .run(*args)
}

@Configuration
@EnableMockOAuth2Server
class LocalRunConfig
