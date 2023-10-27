package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import javax.sql.DataSource


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
        removeDataFromDB(dataSource)
    }


    companion object {
        val instance: PostgresqlTestContainer = PostgresqlTestContainer("postgres:14.7-alpine")
        private val dataSource = HikariDataSource(HikariConfig().apply {
            jdbcUrl = "jdbc:tc:postgresql:14:///test"
            username = instance.username
            password = instance.password
        })
        fun createInstance(name: String): DataSource {
            val instance = PostgresqlTestContainer("postgres:14.7-alpine")
            val dataSource = HikariDataSource(HikariConfig().apply {
                jdbcUrl = "jdbc:tc:postgresql:14:///$name"
                username = instance.username
                password = instance.password
            })
            return dataSource
        }

        fun removeDataFromDB(dataSource: DataSource) {
            dataSource.connection.apply {
                createStatement().execute(
                    """           
                        DELETE FROM BREV_STATUS;
                        DELETE FROM BREV;
                        DELETE FROM GODSKRIV_OPPTJENING_STATUS;   
                        DELETE FROM GODSKRIV_OPPTJENING;
                        DELETE FROM OPPGAVE_STATUS;
                        DELETE FROM OPPGAVE;
                        DELETE FROM BEHANDLING;
                        DELETE FROM MELDING_STATUS;
                        DELETE FROM MELDING;
                    """
                )
                close()
            }
        }
    }
}