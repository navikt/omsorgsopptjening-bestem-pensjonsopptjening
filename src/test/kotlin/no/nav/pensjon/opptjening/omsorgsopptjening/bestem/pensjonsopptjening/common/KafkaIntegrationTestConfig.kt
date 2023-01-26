package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common


import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.KafkaSecurityConfig
import org.apache.kafka.clients.CommonClientConfigs
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka


@EnableKafka
@Configuration
class KafkaIntegrationTestConfig {
    @Bean
    fun securityConfig() = KafkaSecurityConfig(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to "PLAINTEXT")
}