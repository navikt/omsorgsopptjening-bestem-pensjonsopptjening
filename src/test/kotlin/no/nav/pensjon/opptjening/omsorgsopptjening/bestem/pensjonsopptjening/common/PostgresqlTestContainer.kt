package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait


class PostgresqlTestContainer private constructor() : PostgreSQLContainer<PostgresqlTestContainer>(
    IMAGE_VERSION
) {
    override fun start() {
        super.start()
        super.waitingFor(Wait.defaultWaitStrategy())
    }

    override fun stop() {
        //do nothing, JVM handles shut down
    }

    fun removeDataFroDB() {
        dataSource.connection.apply {
            createStatement().execute("DELETE FROM FNR")
            createStatement().execute("DELETE FROM PERSON")
            close()
        }
    }

    companion object {
        private const val IMAGE_VERSION = "postgres:14.7-alpine"
        val instance: PostgresqlTestContainer = PostgresqlTestContainer().apply {
            start()
        }
        private val dataSource = HikariDataSource(HikariConfig().apply {
            jdbcUrl = "jdbc:tc:postgresql:14:///test"
            username = instance.username
            password = instance.password
        })
    }
}
