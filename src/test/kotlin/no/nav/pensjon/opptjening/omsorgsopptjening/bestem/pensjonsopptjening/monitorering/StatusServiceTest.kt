package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.monitorering

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.PostgresqlTestContainer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjeningRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.BehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.repository.OppgaveRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.RådataFraKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import org.assertj.core.api.Assertions.assertThat
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.*
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.Instant
import java.time.Instant.now
import java.time.Month
import java.time.YearMonth
import java.util.*
import javax.sql.DataSource
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
object StatusServiceTest {

    inline val Int.daysAgo: Instant get() = now().minus(this.days.toJavaDuration())

    private lateinit var oppgaveRepo: OppgaveRepo
    private lateinit var personGrunnlagRepo: PersongrunnlagRepo
    private lateinit var behandlingRepo: BehandlingRepo
    private lateinit var godskrivOpptjeningRepo: GodskrivOpptjeningRepo

    private lateinit var statusService: StatusService
    private lateinit var dataSource: DataSource
    private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

    @BeforeAll
    fun beforeAll() {
        dataSource = PostgresqlTestContainer.createInstance("test-status")
        val flyway =
            Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
        flyway.migrate()

        jdbcTemplate = NamedParameterJdbcTemplate(dataSource)
        oppgaveRepo = OppgaveRepo(jdbcTemplate)
        personGrunnlagRepo = PersongrunnlagRepo(jdbcTemplate)
        behandlingRepo = BehandlingRepo(jdbcTemplate)
        godskrivOpptjeningRepo = GodskrivOpptjeningRepo(jdbcTemplate)
        statusService = StatusService(oppgaveRepo, behandlingRepo, personGrunnlagRepo, godskrivOpptjeningRepo)
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
                                utbetalt = 7234,
                                landstilknytning = Landstilknytning.NORGE
                            )
                        ),
                        hjelpestønadsperioder = emptyList()
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
        val melding = personGrunnlagMelding(700.daysAgo)
        personGrunnlagRepo.persist(melding)
        val status = statusService.checkStatus()
        assertThat(status).isEqualTo(ApplicationStatus.Feil("Siste melding er for gammel"))
    }

    @Test
    @Order(3)
    fun testMeldingFeilet() {
        val mottatt = lagOgLagreMelding(opprettet = 300.daysAgo)
        val feilet = endreStatusTilFeilet(mottatt)
        personGrunnlagRepo.updateStatus(feilet)
        val status = statusService.checkStatus()
        assertThat(status).isEqualTo(ApplicationStatus.Feil("Det finnes feilede persongrunnlagmeldinger"))
    }

    private fun lagOgLagreMelding(opprettet: Instant = now()): PersongrunnlagMelding.Mottatt {
        val melding = personGrunnlagMelding(opprettet)
        return personGrunnlagRepo.persist(melding)
    }

    private fun endreStatusTilFeilet(mottatt: PersongrunnlagMelding.Mottatt): PersongrunnlagMelding.Mottatt {
        fun retry(mottatt: PersongrunnlagMelding.Mottatt): PersongrunnlagMelding.Mottatt {
            return mottatt.copy(statushistorikk = mottatt.statushistorikk + mottatt.status.retry("mock"))
        }
        return retry(retry(retry(retry(mottatt))))
    }

    @Test
    @Order(3)
    fun testGammelMeldingIkkeFerdig() {
        lagOgLagreMelding(5.daysAgo)
        val status = statusService.checkStatus()
        assertThat(status).isEqualTo(ApplicationStatus.Feil("Det finnes gamle meldinger som ikke er ferdig behandlet"))
    }

    @Test
    @Order(4)
    fun testGammelOppgaveIkkeFerdig() {
        val melding = personGrunnlagMelding(now())
        val mottatt = personGrunnlagRepo.persist(melding)

        val uuid1 = UUID.randomUUID()

        lagreDummyBehandling(uuid1, mottatt)
        lagreDummyOppgave(uuid1, mottatt, 200.daysAgo)

        lagreDummyOppgaveStatus(uuid1)

        printDatabaseContent()

        val status = statusService.checkStatus()
        assertThat(status).isEqualTo(ApplicationStatus.Feil("Det finnes gamle oppgaver som ikke er ferdig behandlet"))
    }

    private fun lagreDummyOppgave( // 200.daysAgo.toString(),
        uuid1: UUID,
        mottatt: PersongrunnlagMelding.Mottatt,
        opprettet: Instant,
    ) {
        jdbcTemplate.update(
            """insert into oppgave (id, behandlingId, opprettet, meldingId, detaljer) values (:id,:behandlingId, (:opprettet)::timestamptz, :meldingId, cast (:detaljer as json) )""",
            MapSqlParameterSource(
                mapOf<String, Any?>(
                    "id" to uuid1,
                    "behandlingId" to uuid1,
                    "opprettet" to opprettet.toString(),
                    "meldingId" to mottatt.id,
                    "detaljer" to """{"type":"UspesifisertFeilsituasjon", "omsorgsyter":"12345123451"}"""
                ),
            ),
        )
    }


    @Test
    @Order(4)
    fun testGammelGodskrivingIkkeFerdig() {
        val uuid1 = UUID.randomUUID()

        val melding = personGrunnlagMelding(now())
        val mottatt = personGrunnlagRepo.persist(melding)

        lagreDummyBehandling(uuid1, mottatt)
        lagreDummyOppgave(uuid1, mottatt, now())
        lagreDummyOppgaveStatus(uuid1)
        lagreDummyGodskrivOpptjening(uuid1, uuid1, 10.daysAgo)
        lagreDummyGodskrivOpptjeningStatus(uuid1)

        printDatabaseContent()

        val status = statusService.checkStatus()
        assertThat(status).isEqualTo(ApplicationStatus.Feil("Det finnes gamle godskrivinger som ikke er ferdig behandlet"))
    }


    private fun lagreDummyGodskrivOpptjening(
        uuid1: UUID,
        behandlingId: UUID,
        opprettet: Instant /* = now() */
    ) { // 10.daysAgo.toString()
        jdbcTemplate.update(
            """insert into godskriv_opptjening (id, opprettet, behandlingId) 
                    |values (:id, (:opprettet)::timestamptz, :behandlingId)""".trimMargin(),
            MapSqlParameterSource(
                mapOf<String, Any?>(
                    "id" to uuid1,
                    "opprettet" to opprettet.toString(),
                    "behandlingId" to behandlingId,
                ),
            ),
        )
    }

    private fun lagreDummyOppgaveStatus(id: UUID) {
        jdbcTemplate.update(
            """insert into oppgave_status (id, status, statushistorikk) 
                    |values (:id, cast(:status as json), cast (:statushistorikk as json))""".trimMargin(),
            MapSqlParameterSource(
                mapOf<String, Any?>(
                    "id" to id,
                    "status" to """{"type": "Klar"}""",
                    "statushistorikk" to """[{"type": "Klar", "tidspunkt": "2023-10-30T08:46:19.690871Z"}]"""
                ),
            ),
        )
    }

    private fun lagreDummyBehandling(
        id: UUID,
        mottatt: PersongrunnlagMelding.Mottatt
    ) {
        jdbcTemplate.update(
            """insert into behandling (id, opprettet, omsorgs_ar, omsorgsyter, omsorgsmottaker, omsorgstype, grunnlag,vilkarsvurdering, utfall, kafkaMeldingId) 
                    |values (:id, (:opprettet)::timestamptz, :omsorgs_ar, :omsorgsyter, :omsorgsmottaker, :omsorgstype, cast (:grunnlag as json), cast (:vilkarsvurdering as json), cast (:utfall as json), :kafkaMeldingId)""".trimMargin(),
            MapSqlParameterSource(
                mapOf<String, Any?>(
                    "id" to id,
                    "opprettet" to now().toString(),
                    "omsorgs_ar" to 1337,
                    "omsorgsyter" to "x",
                    "omsorgsmottaker" to "x",
                    "omsorgstype" to "x",
                    "grunnlag" to "{}",
                    "vilkarsvurdering" to "{}",
                    "utfall" to "{}",
                    "kafkaMeldingId" to mottatt.id,
                ),
            ),
        )
    }

    private fun lagreDummyGodskrivOpptjeningStatus(
        godskrivOpptjeningId: UUID
    ) {
        jdbcTemplate.update(
            """insert into godskriv_opptjening_status (id, status, statushistorikk) 
                    |values (:id, cast (:status as json), cast (:statushistorikk as json))""".trimMargin(),
            MapSqlParameterSource(
                mapOf<String, Any?>(
                    "id" to godskrivOpptjeningId,
                    "status" to """{"type":"x"}""",
                    "statushistorikk" to """[]""",
                ),
            ),
        )
    }

    private fun printDatabaseContent() {
        println("Database content:")
        jdbcTemplate.queryForList("select * from melding", emptyMap<String, Any>()).forEach { println(" melding $it") }
        jdbcTemplate.queryForList("select * from melding_status", emptyMap<String, Any>())
            .forEach { println(" melding_status $it") }
        jdbcTemplate.queryForList("select * from oppgave", emptyMap<String, Any>()).forEach { println(" oppgave $it") }
        jdbcTemplate.queryForList("select * from oppgave_status", emptyMap<String, Any>())
            .forEach { println(" oppgave_status $it") }
        jdbcTemplate.queryForList("select * from behandling", emptyMap<String, Any>())
            .forEach { println(" behandling $it") }
        jdbcTemplate.queryForList("select * from godskriv_opptjening", emptyMap<String, Any>())
            .forEach { println(" godskriv_opptjening $it") }
        jdbcTemplate.queryForList("select * from godskriv_opptjening_status", emptyMap<String, Any>())
            .forEach { println(" godskriv_opptjening_status $it") }
        jdbcTemplate.queryForList("select * from brev", emptyMap<String, Any>()).forEach { println(" brev $it") }
        jdbcTemplate.queryForList("select * from brev_status", emptyMap<String, Any>())
            .forEach { println(" brev_status $it") }
    }
}