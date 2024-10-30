package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config

import com.zaxxer.hikari.HikariDataSource

class DatasourceReadinessCheck(
    private val datasource: HikariDataSource
) {
    fun isReady(): Boolean {
        return !datasource.isClosed
    }
}