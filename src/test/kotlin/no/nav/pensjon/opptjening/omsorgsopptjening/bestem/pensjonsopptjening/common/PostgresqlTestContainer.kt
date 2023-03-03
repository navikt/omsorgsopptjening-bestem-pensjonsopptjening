package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common

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

    companion object {
        private const val IMAGE_VERSION = "postgres:14.7-alpine"
        private var container: PostgresqlTestContainer = PostgresqlTestContainer().apply {
            start()
        }
        val instance: PostgresqlTestContainer
            get() {
                return container
            }
    }
}
