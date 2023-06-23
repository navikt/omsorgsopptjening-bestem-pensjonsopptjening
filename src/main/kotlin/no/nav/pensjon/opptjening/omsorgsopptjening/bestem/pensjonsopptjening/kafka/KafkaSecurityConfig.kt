package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.common.config.SslConfigs
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.EnableKafka

@EnableKafka
@Configuration
@Profile("!no-kafka")
class KafkaSecurityConfig(@Value("\${kafka.brokers}") private val aivenBootstrapServers: String) {

    @Profile("dev-gcp", "prod-gcp")
    @Bean
    fun securityConfig(
        @Value("\${kafka.keystore.path}") keystorePath: String,
        @Value("\${kafka.credstore.password}") credstorePassword: String,
        @Value("\${kafka.truststore.path}") truststorePath: String,
    ) = Common(
        SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG to keystorePath,
        SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG to credstorePassword,
        SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG to credstorePassword,
        SslConfigs.SSL_KEY_PASSWORD_CONFIG to credstorePassword,
        SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG to "JKS",
        SslConfigs.SSL_KEYSTORE_TYPE_CONFIG to "PKCS12",
        SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG to truststorePath,
        CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to "SSL",
    )

    class Common(vararg input: Pair<String, Any>) {
        internal val securityConfigs = input.toMap()
    }
}