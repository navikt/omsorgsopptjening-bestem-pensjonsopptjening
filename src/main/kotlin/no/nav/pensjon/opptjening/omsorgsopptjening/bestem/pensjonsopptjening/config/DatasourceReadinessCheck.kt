package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config

import com.zaxxer.hikari.HikariDataSource
import org.springframework.stereotype.Component

@Component
class DatasourceReadinessCheck(
    private val hikariDataSource: HikariDataSource
) {
    fun isReady(): Boolean {
        return !hikariDataSource.isClosed
    }
}