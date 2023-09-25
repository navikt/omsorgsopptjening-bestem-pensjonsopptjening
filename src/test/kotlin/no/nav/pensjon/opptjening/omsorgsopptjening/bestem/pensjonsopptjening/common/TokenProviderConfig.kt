package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pensjon.opptjening.azure.ad.client.TokenProvider
import pensjon.opptjening.azure.ad.client.mock.MockTokenProvider

@Configuration
class TokenProviderConfig {

    @Bean("pdlTokenProvider")
    fun pdlTokenProvider(): TokenProvider = MockTokenProvider(MOCK_TOKEN)

    @Bean("poppTokenProvider")
    fun poppTokenProvider(): TokenProvider = MockTokenProvider(MOCK_TOKEN)

    @Bean("oppgaveTokenProvider")
    fun oppgaveTokenProvider(): TokenProvider = MockTokenProvider(MOCK_TOKEN)

    @Bean("PENTokenProvider")
    fun PENTokenProvider(): TokenProvider = MockTokenProvider(MOCK_TOKEN)


    companion object {
        const val MOCK_TOKEN = "test.token.test"
    }
}