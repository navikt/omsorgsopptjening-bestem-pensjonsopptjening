package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.monitorering

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.PostgresqlTestContainer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.BehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.repository.OppgaveRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.RådataFraKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.MedlemIFolketrygden
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import org.assertj.core.api.Assertions.assertThat
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.*
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.Instant
import java.time.Instant.now
import java.time.Month
import java.time.YearMonth
import javax.sql.DataSource
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
object StatusServiceTest {

    private lateinit var oppgaveRepo: OppgaveRepo
    private lateinit var personGrunnlagRepo: PersongrunnlagRepo
    private lateinit var behandlingRepo: BehandlingRepo
    private lateinit var statusService: StatusService
    private lateinit var dataSource : DataSource

    @BeforeAll
    fun beforeAll() {
        dataSource = PostgresqlTestContainer.createInstance("test-status")
        val flyway =
            Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
        flyway.migrate()

        oppgaveRepo = OppgaveRepo(NamedParameterJdbcTemplate(dataSource))
        personGrunnlagRepo = PersongrunnlagRepo(NamedParameterJdbcTemplate(dataSource))
        behandlingRepo = BehandlingRepo(NamedParameterJdbcTemplate(dataSource))
        statusService = StatusService(oppgaveRepo, behandlingRepo, personGrunnlagRepo)
    }

    @BeforeEach
    fun beforeEach() {
        PostgresqlTestContainer.removeDataFromDB(dataSource)
    }

    private fun personGrunnlagMelding(opprettet: Instant): PersongrunnlagMelding.Lest {
        return PersongrunnlagMelding.Lest(
            innhold = no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding(
                omsorgsyter = "12345678910",
                persongrunnlag = listOf(
                    no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding.Persongrunnlag(
                        omsorgsyter = "12345678910",
                        omsorgsperioder = listOf(
                            no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding.Omsorgsperiode(
                                fom = YearMonth.of(2018, Month.SEPTEMBER),
                                tom = YearMonth.of(2025, Month.DECEMBER),
                                omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                omsorgsmottaker = "07081812345",
                                kilde = Kilde.BARNETRYGD,
                                medlemskap = MedlemIFolketrygden.Ukjent,
                                utbetalt = 7234,
                                landstilknytning = Landstilknytning.NORGE
                            )
                        )
                    ),
                ),
                rådata = RådataFraKilde(""),
                innlesingId = InnlesingId.generate(),
                correlationId = CorrelationId.generate(),
            ),
            opprettet = opprettet
        )
    }

    @Test
    @Order(1)
    fun testIngenPersonStatusMeldinger() {
        val status = statusService.checkStatus()
        assertThat(status).isEqualTo(ApplicationStatus.Feil("Ingen meldinger"))
    }

    @Test
    @Order(2)
    fun testForGammeMelding() {
        val melding = personGrunnlagMelding(now().minus(700.days.toJavaDuration()))
        val mottatt = personGrunnlagRepo.persist(melding)
        val status = statusService.checkStatus()
        assertThat(status).isEqualTo(ApplicationStatus.Feil("Siste melding er for gammel"))
    }

    @Test
    @Order(3)
    fun testMeldingFeilet() {
        val melding = personGrunnlagMelding(now().minus(300.days.toJavaDuration()))
        val mottatt = personGrunnlagRepo.persist(melding)
        val mottatt1 = mottatt.copy(statushistorikk = mottatt.statushistorikk + mottatt.status.retry("1"))
        val mottatt2 = mottatt1.copy(statushistorikk = mottatt1.statushistorikk + mottatt1.status.retry("2"))
        val mottatt3 = mottatt2.copy(statushistorikk = mottatt2.statushistorikk + mottatt2.status.retry("3"))
        val feilet = mottatt3.copy(statushistorikk = mottatt3.statushistorikk + mottatt3.status.retry("feilet"))
        personGrunnlagRepo.updateStatus(feilet)
        val status = statusService.checkStatus()
        assertThat(status).isEqualTo(ApplicationStatus.Feil("Det finnes feilede persongrunnlagmeldinger"))
    }

    @Test
    @Order(3)
    fun testGammelMeldingIkkeFerdig() {
        val melding = personGrunnlagMelding(now().minus(5.days.toJavaDuration()))
        val mottatt = personGrunnlagRepo.persist(melding)
        val status = statusService.checkStatus()
        assertThat(status).isEqualTo(ApplicationStatus.Feil("Det finnes gamle meldinger som ikke er ferdig behandlet"))
    }
}