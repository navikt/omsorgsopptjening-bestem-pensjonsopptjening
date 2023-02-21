package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.template

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class RestTemplateConfig {
    @Bean
    fun poppTemplate(headerInterceptor: HeaderInterceptor) = RestTemplateBuilder()
        .additionalInterceptors(headerInterceptor)
        .build()
}