package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.external.spring

import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.external.BestemSakKlient
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.external.BestemSakMetrics
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pensjon.opptjening.azure.ad.client.TokenProvider

@Configuration
class BestemSakKlientConfig {

    @Bean
    fun bestemSakKlient(
        @Value("\${PEN_BASE_URL}") bestemSakUrl: String,
        @Qualifier("PENTokenProvider") tokenProvider: TokenProvider,
        bestemSakMetrics: BestemSakMetrics,
    ): BestemSakKlient {
        return BestemSakKlient(
            bestemSakUrl = bestemSakUrl,
            tokenProvider = tokenProvider,
            metrics = bestemSakMetrics,
        )
    }

    @Bean
    fun bestemSakMetrics(
        meterRegistry: MeterRegistry
    ): BestemSakMetrics {
        return object : BestemSakMetrics {
            override fun tellAntallSakerHentet() {
                meterRegistry.counter("saker", "antall", "hentet")
            }
        }
    }
}