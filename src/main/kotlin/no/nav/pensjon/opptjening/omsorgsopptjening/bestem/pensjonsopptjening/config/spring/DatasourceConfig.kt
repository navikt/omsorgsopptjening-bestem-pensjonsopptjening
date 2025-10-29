package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.spring

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.DatasourceReadinessCheck
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("dev-gcp", "prod-gcp", "kafkaIntegrationTest")
class DatasourceConfig {

    @Bean
    fun datasource(
        @Value($$"${DATABASE_HOST}") dbHost: String,
        @Value($$"${DATABASE_PORT}") dbPort: String,
        @Value($$"${DATABASE_DATABASE}") dbName: String,
        @Value($$"${DATABASE_USERNAME}") dbUsername: String,
        @Value($$"${DATABASE_PASSWORD}") dbPassword: String,
    ): HikariDataSource {
        return HikariDataSource(
            HikariConfig().apply {
                jdbcUrl = "jdbc:postgresql://$dbHost:$dbPort/$dbName"
                username = dbUsername
                password = dbPassword
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
