package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.spring

import com.zaxxer.hikari.HikariDataSource
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.DatasourceReadinessCheck
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Config {

    @Bean
    fun datasourceReadiness(
        hikariDataSource: HikariDataSource
    ): DatasourceReadinessCheck {
        return DatasourceReadinessCheck(hikariDataSource)
    }
}
