package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.external.spring

import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.external.pdl.GraphqlQuery
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.external.pdl.PdlClient
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.external.pdl.PdlClientMetrics
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.external.pdl.PdlService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.PersonOppslag
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.web.client.RestTemplate
import pensjon.opptjening.azure.ad.client.TokenProvider

@Configuration
internal class PdlConfig {

    @Bean
    fun pdlClient(
        @Value($$"${PDL_URL}") pdlUrl: String,
        @Qualifier("pdlTokenProvider") tokenProvider: TokenProvider,
        pdlClientMetrics: PdlClientMetrics,
        @Value("classpath:pdl/folkeregisteridentifikator.graphql") hentPersonQuery: Resource,
        @Value("classpath:pdl/hentAktorId.graphql") hentAktorIdQuery: Resource,
        restTemplate: RestTemplate,
    ): PdlClient {
        return PdlClient(
            pdlUrl = pdlUrl,
            tokenProvider = tokenProvider,
            metrics = pdlClientMetrics,
            graphqlQuery = GraphqlQuery(
                hentPersonQuery = hentPersonQuery,
                hentAktorIdQuery = hentAktorIdQuery,
            ),
            restTemplate = restTemplate
        )
    }

    @Bean
    fun pdlService(
        pdlClient: PdlClient
    ): PersonOppslag {
        return PdlService(pdlClient)
    }

    @Bean
    fun pdlClientMetrics(
        meterRegistry: MeterRegistry
    ): PdlClientMetrics {
        return object : PdlClientMetrics {
            override fun tellPersonHentet() {
                meterRegistry.counter("personer", "antall", "hentet").increment()
            }

            override fun tellAktørIdHentet() {
                meterRegistry.counter("aktorid", "antall", "hentet").increment()
            }

        }
    }
}