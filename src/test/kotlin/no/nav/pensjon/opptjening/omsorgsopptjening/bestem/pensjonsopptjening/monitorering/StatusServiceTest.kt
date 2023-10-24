package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.monitorering

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.PostgresqlTestContainer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.repository.OppgaveRepo
import org.assertj.core.api.Assertions.assertThat
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.*
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StatusServiceTest {

    lateinit var oppgaveRepo: OppgaveRepo
    @BeforeAll
    fun beforeAll() {
        val dataSource = PostgresqlTestContainer.createInstance("test-status")
        val flyway =
            Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
        flyway.migrate()

        oppgaveRepo = OppgaveRepo(NamedParameterJdbcTemplate(dataSource))
    }

    @Test
    @Order(1)
    @Disabled
    fun test1() {
        // oppgaveRepo.find(UUID.randomUUID())
        assertThat(true).isFalse()
    }
}