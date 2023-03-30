package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait


class PostgresqlTestContainer private constructor(image: String) : PostgreSQLContainer<PostgresqlTestContainer>(image) {

    init {
        start()
    }

    override fun start() {
        super.start()
        super.waitingFor(Wait.defaultWaitStrategy())
    }

    override fun stop() {
        //Do nothing, JVM handles shut down
    }

    fun removeDataFromDB() {
        dataSource.connection.apply {
            createStatement().execute(
                """
                        DELETE FROM INVOLVERTE_PERSONER;
                        DELETE FROM OMSORGSOPPTJENINGSGRUNNLAG;
                        DELETE FROM  OMSORGSYTER;
                        DELETE FROM OMSORGSARBEIDSMOTTAKER;
                        DELETE FROM OMSORGSARBEID_PERIODE;
                        DELETE FROM OMSORGSARBEID_SAK;
                        DELETE FROM OMSORGSARBEID_SNAPSHOT;
                        DELETE FROM FNR;
                        DELETE FROM PERSON;
                    """
            )
            close()
        }
    }

    companion object {
        val instance: PostgresqlTestContainer = PostgresqlTestContainer("postgres:14.7-alpine")
        private val dataSource = HikariDataSource(HikariConfig().apply {
            jdbcUrl = "jdbc:tc:postgresql:14:///test"
            username = instance.username
            password = instance.password
        })
    }
}