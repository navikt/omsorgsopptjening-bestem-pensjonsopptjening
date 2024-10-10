package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config

import com.zaxxer.hikari.HikariDataSource

class DatasourceReadinessCheck(
    private val hikariDataSource: HikariDataSource
) {
    fun isReady(): Boolean {
        return !hikariDataSource.isClosed
    }
}