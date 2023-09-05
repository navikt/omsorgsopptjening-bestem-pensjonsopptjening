package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash

import io.getunleash.DefaultUnleash
import io.getunleash.strategy.DefaultStrategy
import io.getunleash.util.UnleashConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.InetAddress

@Configuration
class NavUnleashConfig(
    @Value("\${UNLEASH_SERVER_API_URL}") private val unleash_url: String,
    @Value("\${UNLEASH_SERVER_API_TOKEN}") private val unleash_api_key: String) {

    @Bean
    fun unleashConfig(): DefaultUnleash {
        return DefaultUnleash(UnleashConfig.builder()
            .appName("omsorgsopptjening-bestem-pensjonsopptjening")
            .instanceId(InetAddress.getLocalHost().hostName)
            .unleashAPI("$unleash_url/api")
            .apiKey(unleash_api_key)
            .build(),
            DefaultStrategy()
        );
    }

    enum class Feature(val toggleName: String) {
        OPPRETT_OPPGAVER("omsorgsopptjening-bestem-pensjonsopptjening-opprett-oppgaver")
    }
}
