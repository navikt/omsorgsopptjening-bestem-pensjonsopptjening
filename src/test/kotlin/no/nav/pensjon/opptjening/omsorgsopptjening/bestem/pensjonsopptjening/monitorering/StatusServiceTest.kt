package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.monitorering

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.Rådata
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.Instant
import java.time.Instant.now
import java.time.Month
import java.time.YearMonth
import java.util.UUID
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class StatusServiceTest : SpringContextTest.NoKafka() {

    private inline val Int.daysAgo: Instant get() = now().minus(this.days.toJavaDuration())

    @Autowired
    private lateinit var personGrunnlagRepo: PersongrunnlagRepo

    @Autowired
    private lateinit var statusService: StatusService

    @Autowired
    private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

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
                rådata = Rådata(),
                innlesingId = InnlesingId.generate(),
                correlationId = CorrelationId.generate(),
            ),
            opprettet = opprettet
        )
    }

    private fun lagOgLagreMelding(opprettet: Instant = now()): PersongrunnlagMelding.Mottatt {
        return lagreOgHent(personGrunnlagMelding(opprettet))
    }

    private fun endreStatusTilFeilet(mottatt: PersongrunnlagMelding.Mottatt): PersongrunnlagMelding.Mottatt {
        fun retry(mottatt: PersongrunnlagMelding.Mottatt): PersongrunnlagMelding.Mottatt {
            return mottatt.copy(statushistorikk = mottatt.statushistorikk + mottatt.status.retry("mock"))
        }
        return retry(retry(retry(retry(mottatt))))
    }

    private fun lagreDummyOppgave(
        id: UUID,
        mottatt: PersongrunnlagMelding.Mottatt,
        opprettet: Instant = now(),
    ) {
        jdbcTemplate.update(
            """insert into oppgave (id, behandlingId, opprettet, meldingId, detaljer, statushistorikk, status) 
                |values (:id,:behandlingId, (:opprettet)::timestamptz, :meldingId, cast (:detaljer as json), cast (:statushistorikk as json),:status)""".trimMargin(),
            MapSqlParameterSource(
                mapOf<String, Any?>(
                    "id" to id,
                    "behandlingId" to id,
                    "opprettet" to opprettet.toString(),
                    "meldingId" to mottatt.id,
                    "detaljer" to """{"type":"MottakerOgTekst", "oppgavemottaker":"12345123451", "oppgavetekst":["blah blah", "blah"]}""",
                    "statushistorikk" to """[{"type": "Klar", "tidspunkt": "2023-10-30T08:46:19.690871Z"}]""",
                    "status" to "Klar",
                ),
            ),
        )
    }

    private fun lagreDummyGodskrivOpptjening(
        uuid1: UUID,
        behandlingId: UUID,
        opprettet: Instant = now()
    ) {
        jdbcTemplate.update(
            """insert into godskriv_opptjening (id, opprettet, behandlingId, statushistorikk,status) 
                    |values (:id, (:opprettet)::timestamptz, :behandlingId, cast (:statushistorikk as json),:status)""".trimMargin(),
            MapSqlParameterSource(
                mapOf<String, Any?>(
                    "id" to uuid1,
                    "opprettet" to opprettet.toString(),
                    "behandlingId" to behandlingId,
                    "statushistorikk" to """[]""",
                    "status" to "x"
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

    @Test
    @Order(1)
    fun testIngenPersonStatusMeldinger() {
        val status = statusService.checkStatus()
        assertThat(status).isEqualTo(ApplicationStatus.IkkeKjort)
    }

    @Test
    @Order(2)
    fun testOK() {
        val melding = personGrunnlagMelding(now())
        personGrunnlagRepo.lagre(melding)
        val status = statusService.checkStatus()
        assertThat(status).isEqualTo(ApplicationStatus.OK)
    }

    @Test
    @Order(3)
    fun testForGammeMelding() {
        val melding = personGrunnlagMelding(700.daysAgo)
        personGrunnlagRepo.lagre(melding)
        val status = statusService.checkStatus()
        assertThat(status).isEqualTo(ApplicationStatus.Feil("Siste melding er for gammel"))
    }

    @Test
    @Order(4)
    fun testMeldingFeilet() {
        val mottatt = lagOgLagreMelding(opprettet = 300.daysAgo)
        val feilet = endreStatusTilFeilet(mottatt)
        personGrunnlagRepo.updateStatus(feilet)
        val status = statusService.checkStatus()
        assertThat(status).isEqualTo(ApplicationStatus.Feil("Det finnes feilede persongrunnlagmeldinger"))
    }

    @Test
    @Order(5)
    fun testGammelMeldingIkkeFerdig() {
        lagOgLagreMelding(5.daysAgo)
        val status = statusService.checkStatus()
        assertThat(status).isEqualTo(ApplicationStatus.Feil("Det finnes gamle meldinger som ikke er ferdig behandlet"))
    }

    @Test
    @Order(6)
    fun testGammelOppgaveIkkeFerdig() {
        val melding = personGrunnlagMelding(now())
        val mottatt = lagreOgHent(melding)

        val uuid = UUID.randomUUID()

        lagreDummyBehandling(uuid, mottatt)
        lagreDummyOppgave(uuid, mottatt, 200.daysAgo)

        printDatabaseContent()

        val status = statusService.checkStatus()
        // assertThat(status).isEqualTo(ApplicationStatus.Feil("Det finnes gamle oppgaver som ikke er ferdig behandlet"))
        assertThat(status).isEqualTo(ApplicationStatus.OK) // Ingen rapportering tilbake, så oppgaver blir liggende permanent uferdig
    }

    @Test
    @Order(7)
    fun testGammelGodskrivingIkkeFerdig() {
        val uuid1 = UUID.randomUUID()

        val melding = personGrunnlagMelding(now())
        val mottatt = lagreOgHent(melding)

        lagreDummyBehandling(uuid1, mottatt)
        lagreDummyOppgave(uuid1, mottatt, now())
        lagreDummyGodskrivOpptjening(uuid1, uuid1, 10.daysAgo)

        printDatabaseContent()

        val status = statusService.checkStatus()
        assertThat(status).isEqualTo(ApplicationStatus.Feil("Det finnes gamle godskrivinger som ikke er ferdig behandlet"))
    }

    private fun printDatabaseContent() {
        println("Database content:")
        jdbcTemplate.queryForList("select * from melding", emptyMap<String, Any>()).forEach { println(" melding $it") }
        jdbcTemplate.queryForList("select * from oppgave", emptyMap<String, Any>()).forEach { println(" oppgave $it") }
        jdbcTemplate.queryForList("select * from behandling", emptyMap<String, Any>())
            .forEach { println(" behandling $it") }
        jdbcTemplate.queryForList("select * from godskriv_opptjening", emptyMap<String, Any>())
            .forEach { println(" godskriv_opptjening $it") }
        jdbcTemplate.queryForList("select * from brev", emptyMap<String, Any>()).forEach { println(" brev $it") }
    }

    private fun lagreOgHent(melding: PersongrunnlagMelding.Lest): PersongrunnlagMelding.Mottatt {
        return personGrunnlagRepo.lagre(melding).let { personGrunnlagRepo.find(it!!) }
    }
}