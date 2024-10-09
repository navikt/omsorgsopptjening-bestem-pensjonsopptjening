package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.external.spring

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.external.OppgaveKlient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pensjon.opptjening.azure.ad.client.TokenProvider

@Configuration
class OppgaveKlientConfig {

    @Bean
    fun oppgaveKlient(
        @Value("\${OPPGAVE_URL}") oppgaveUrl: String,
        @Qualifier("oppgaveTokenProvider") tokenProvider: TokenProvider,
    ): OppgaveKlient {
        return OppgaveKlient(
            oppgaveUrl = oppgaveUrl,
            tokenProvider = tokenProvider
        )
    }
}