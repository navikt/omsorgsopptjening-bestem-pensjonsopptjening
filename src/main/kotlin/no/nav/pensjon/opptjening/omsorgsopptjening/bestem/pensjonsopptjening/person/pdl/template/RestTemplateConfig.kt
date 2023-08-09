package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.template

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class RestTemplateConfig {
    @Bean
    fun restTemplate(headerInterceptor: HeaderInterceptor): RestTemplate = RestTemplateBuilder()
        .additionalInterceptors(headerInterceptor)
        .build()
}