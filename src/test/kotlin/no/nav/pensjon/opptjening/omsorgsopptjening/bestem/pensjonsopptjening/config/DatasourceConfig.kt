package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DatasourceConfig {

    @Bean
    fun datasource(
    ): HikariDataSource {
        return HikariDataSource(
            HikariConfig().apply {
                jdbcUrl = "jdbc:tc:postgresql:16:///test"
                username = "test"
                password = "test"
                driverClassName = "org.testcontainers.jdbc.ContainerDatabaseDriver"
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