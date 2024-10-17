package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.spring

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.BehandlingRepo
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

@Configuration
class OmsorgsopptjeningRepoConfig {

    @Bean
    fun behandlingRepo(
        jdbcTemplate: NamedParameterJdbcTemplate
    ): BehandlingRepo {
        return BehandlingRepo(jdbcTemplate = jdbcTemplate)
    }
}