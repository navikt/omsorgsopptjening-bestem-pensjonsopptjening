package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.spring

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.DatasourceReadinessCheck
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class DatasourceConfig {

    @Bean
    @Profile("dev-gcp","prod-gcp")
    fun datasource(
        @Value($$"${DATABASE_JDBC_URL}") jdbcUrl: String,
    ): HikariDataSource {
        return HikariDataSource(
            HikariConfig().apply {
                this.jdbcUrl = jdbcUrl
                maximumPoolSize = 32
                minimumIdle = 6
            }
        )
    }

    @Bean
    fun datasourceReadiness(
        datasource: HikariDataSource
    ): DatasourceReadinessCheck {
        return DatasourceReadinessCheck(datasource)
    }
}
