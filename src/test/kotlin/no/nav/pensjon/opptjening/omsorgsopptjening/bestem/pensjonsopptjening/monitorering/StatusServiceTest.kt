package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.monitorering

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.PostgresqlTestContainer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.BehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.repository.OppgaveRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
import org.assertj.core.api.Assertions.assertThat
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.*
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StatusServiceTest {

    private lateinit var oppgaveRepo: OppgaveRepo
    private lateinit var personGrunnlagRepo: PersongrunnlagRepo
    private lateinit var behandlingRepo: BehandlingRepo

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
        personGrunnlagRepo = PersongrunnlagRepo(NamedParameterJdbcTemplate(dataSource))
        behandlingRepo = BehandlingRepo(NamedParameterJdbcTemplate(dataSource))
    }

    @Test
    @Order(1)
    @Disabled
    fun test1() {
        // oppgaveRepo.find(UUID.randomUUID())
        assertThat(true).isFalse()
    }
}