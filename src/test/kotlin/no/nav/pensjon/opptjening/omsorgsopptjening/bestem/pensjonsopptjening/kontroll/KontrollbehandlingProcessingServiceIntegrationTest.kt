package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kontroll

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.Resultat
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ingenPensjonspoeng
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ingenUnntaksperioderForMedlemskap
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubForPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.wiremockWithPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kontrollbehandling.KontrollbehandlingProcessingService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kontrollbehandling.KontrollbehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.BehandlingUtfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførteBehandlinger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.erAvslått
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMeldingProcessingService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.processAndExpectResult
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.Rådata
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import java.time.Month
import java.time.YearMonth
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding as PersongrunnlagMeldingKafka

class KontrollbehandlingProcessingServiceIntegrationTest : SpringContextTest.NoKafka() {

    @Autowired
    private lateinit var kontrollbehandlingRepo: KontrollbehandlingRepo

    @Autowired
    private lateinit var kontrollbehandlingProcessingService: KontrollbehandlingProcessingService

    @Autowired
    private lateinit var persongrunnlagProcessingService: PersongrunnlagMeldingProcessingService

    @Autowired
    private lateinit var persongrunnlagRepo: PersongrunnlagRepo

    @Autowired
    private lateinit var jdbcTemplate: NamedParameterJdbcOperations

    companion object {
        @JvmField
        @RegisterExtension
        val wiremock = wiremockWithPdlTransformer()
    }

    @BeforeEach
    override fun beforeEach() {
        super.beforeEach()
        wiremock.stubForPdlTransformer()
        wiremock.ingenUnntaksperioderForMedlemskap()
    }

    @Test
    fun `happy path`() {
        val innlesingId = InnlesingId.generate()

        lagrePersongrunnlag(
            omsorgsyter = "12345678910",
            omsorgsmottaker = "07081812345",
            innlesingId = innlesingId
        )

        val behandling = persongrunnlagProcessingService.processAndExpectResult().single().single()

        kontrollbehandlingRepo.bestillKontroll(innlesingId, "vil gjerne", 2020)

        val kontrollbehandling = kontrollbehandlingProcessingService.processAndExpectResult().single().single()

        assertThat(behandling.id).isNotEqualTo(kontrollbehandling.id)
        assertThat(behandling.opprettet).isNotEqualTo(kontrollbehandling.vilkårsvurdering)
        assertThat(behandling.omsorgsAr).isEqualTo(kontrollbehandling.omsorgsAr)
        assertThat(behandling.omsorgstype).isEqualTo(kontrollbehandling.omsorgstype)
        assertThat(behandling.omsorgsmottaker).isEqualTo(kontrollbehandling.omsorgsmottaker)
        assertThat(behandling.omsorgsyter).isEqualTo(kontrollbehandling.omsorgsyter)
        assertThat(behandling.grunnlag).isEqualTo(kontrollbehandling.grunnlag)
        assertThat(behandling.vilkårsvurdering).isEqualTo(kontrollbehandling.vilkårsvurdering)
        assertThat(behandling.utfall).isEqualTo(kontrollbehandling.utfall)
    }

    @Test
    fun `hvis bruker har flere behandlinger for samme innlesingid vil disse påvirke resultatet av hverandre innenfor samme referanse`() {
        val innlesingId = InnlesingId.generate()

        lagrePersongrunnlag(
            omsorgsyter = "12345678910",
            omsorgsmottaker = "07081812345",
            innlesingId = innlesingId
        )
        lagrePersongrunnlag(
            omsorgsyter = "12345678910",
            omsorgsmottaker = "07081812345",
            innlesingId = innlesingId
        )

        persongrunnlagProcessingService.processAndExpectResult().also {
            assertThat(it).hasSize(2)
            //påvirker disse hverandre siden man her sjekker om det allerede er innvilget for omsorgsyter/omsorgsmottaker for gitt år
            val (innvilget, avslag) = it.first().single() to it.last().single()
            assertThat(innvilget.utfall).isEqualTo(BehandlingUtfall.Innvilget)
            assertThat(avslag.utfall).isEqualTo(BehandlingUtfall.Avslag)
            assertThat(avslag.vilkårsvurdering.erAvslått<OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering>()).isTrue()
            assertThat(avslag.vilkårsvurdering.erAvslått<OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Vurdering>()).isTrue()
        }

        kontrollbehandlingRepo.bestillKontroll(innlesingId, "forventer samme resultat som normalt", 2020)

        kontrollbehandlingProcessingService.processAndExpectResult().also {
            assertThat(it).hasSize(2)
            //ved kontroll sjekker vi om det allerede er innvilget for omsorgsyter/omsorgsmottaker for gitt år innenfor samme referanse
            val (innvilget, avslag) = it.first().single() to it.last().single()
            assertThat(innvilget.utfall).isEqualTo(BehandlingUtfall.Innvilget)
            assertThat(avslag.utfall).isEqualTo(BehandlingUtfall.Avslag)
            assertThat(avslag.vilkårsvurdering.erAvslått<OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering>()).isTrue()
            assertThat(avslag.vilkårsvurdering.erAvslått<OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Vurdering>()).isTrue()
        }
    }

    @Test
    fun `en behandling kan behandles flere ganger for forskjellige referanser`() {
        val innlesingId = InnlesingId.generate()

        lagrePersongrunnlag(
            omsorgsyter = "12345678910",
            omsorgsmottaker = "07081812345",
            innlesingId = innlesingId
        )

        persongrunnlagProcessingService.processAndExpectResult()

        kontrollbehandlingRepo.bestillKontroll(innlesingId, "første", 2020)
        kontrollbehandlingRepo.bestillKontroll(innlesingId, "andre", 2020)
        kontrollbehandlingRepo.bestillKontroll(innlesingId, "tredje", 2020)

        kontrollbehandlingProcessingService.processAndExpectResult().also {
            assertThat(it).hasSize(3)
        }
    }

    @Test
    fun `kan behandling kan bare behandles en gang per referanse`() {
        val innlesingId = InnlesingId.generate()

        lagrePersongrunnlag(
            omsorgsyter = "12345678910",
            omsorgsmottaker = "07081812345",
            innlesingId = innlesingId
        )

        persongrunnlagProcessingService.processAndExpectResult()

        kontrollbehandlingRepo.bestillKontroll(innlesingId, "første", 2020)
        assertThrows<DuplicateKeyException> { kontrollbehandlingRepo.bestillKontroll(innlesingId, "første", 2020) }
    }

    @Test
    fun `hvis bruker har flere behandlinger for forskjellig innlesingid vil ikke resultatene påvirke hverandre på tvers av referanser`() {
        val innlesingId = InnlesingId.generate()
        val annenInnlesingId = InnlesingId.generate()

        lagrePersongrunnlag(
            omsorgsyter = "12345678910",
            omsorgsmottaker = "07081812345",
            innlesingId = innlesingId
        )
        lagrePersongrunnlag(
            omsorgsyter = "12345678910",
            omsorgsmottaker = "07081812345",
            innlesingId = annenInnlesingId
        )

        persongrunnlagProcessingService.processAndExpectResult().also {
            assertThat(it).hasSize(2)
            //påvirker disse hverandre siden man her sjekker om det allerede er innvilget for omsorgsyter/omsorgsmottaker for gitt år
            val (innvilget, avslag) = it.first().single() to it.last().single()
            assertThat(innvilget.utfall).isEqualTo(BehandlingUtfall.Innvilget)
            assertThat(avslag.utfall).isEqualTo(BehandlingUtfall.Avslag)
            assertThat(avslag.vilkårsvurdering.erAvslått<OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering>()).isTrue()
            assertThat(avslag.vilkårsvurdering.erAvslått<OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Vurdering>()).isTrue()
        }

        kontrollbehandlingRepo.bestillKontroll(innlesingId, "forventer at denne innvilges", 2020)
        kontrollbehandlingRepo.bestillKontroll(annenInnlesingId, "forventer at denne også innvilges", 2020)

        kontrollbehandlingProcessingService.processAndExpectResult().also {
            assertThat(it).hasSize(2)
            //ved kontroll sjekker vi ikke om det allerede er innvilget for omsorgsyter/omsorgsmottaker for gitt år på tvers av flere referanser
            val (innvilget, innvilget2) = it.first().single() to it.last().single()
            assertThat(innvilget.utfall).isEqualTo(BehandlingUtfall.Innvilget)
            assertThat(innvilget2.utfall).isEqualTo(BehandlingUtfall.Innvilget)
        }
    }

    @Test
    fun `bestiller kontroll for alle behandlinger knyttet til samme innlesing id`() {
        val innlesingId = InnlesingId.generate()
        repeat(3) {
            lagrePersongrunnlag(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "07081812345",
                innlesingId = innlesingId
            )
        }

        val behandlinger = persongrunnlagProcessingService.processAndExpectResult()

        kontrollbehandlingRepo.bestillKontroll(innlesingId, "test", 2020)

        kontrollbehandlingProcessingService.processAndExpectResult().also {
            assertThat(it).hasSize(3)
            assertThat(it).hasSize(behandlinger.count())
        }
    }

    @Test
    fun `sparer på informasjon om godskriving i databasen`() {
        val innlesingId = InnlesingId.generate()
        repeat(1) {
            lagrePersongrunnlag(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "07081812345",
                innlesingId = innlesingId
            )
        }

        val behandlinger = persongrunnlagProcessingService.processAndExpectResult()

        kontrollbehandlingRepo.bestillKontroll(innlesingId, "test", 2020)

        kontrollbehandlingProcessingService.processAndExpectResult().single().single().also {
            val value = jdbcTemplate.query(
                """select godskriv from kontrollbehandling where id = :id""",
                MapSqlParameterSource(mapOf("id" to it.id)),
                ResultSetExtractor { rs -> if (rs.next()) rs.getString(1) else null }
            )

            assertThat(value).contains(it.id.toString())
        }
    }

    @Test
    fun `sparer på informasjon om brev i databasen`() {
        wiremock.ingenPensjonspoeng("12345678910") //mor
        val innlesingId = InnlesingId.generate()
        repeat(1) {
            lagrePersongrunnlagForBrev(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "03041212345",
                innlesingId = innlesingId
            )
        }

        persongrunnlagProcessingService.processAndExpectResult()

        kontrollbehandlingRepo.bestillKontroll(innlesingId, "test", 2020)

        kontrollbehandlingProcessingService.processAndExpectResult().single().single().also {
            val value = jdbcTemplate.query(
                """select brev from kontrollbehandling where id = :id""",
                MapSqlParameterSource(mapOf("id" to it.id)),
                ResultSetExtractor { rs -> if (rs.next()) rs.getString(1) else null }
            )

            assertThat(value).contains("OMSORGSYTER_INGEN_PENSJONSPOENG_FORRIGE_ÅR")
        }
    }

    @Test
    fun `sparer på informasjon om oppgave i databasen`() {
        wiremock.ingenPensjonspoeng("12345678910") //mor
        val innlesingId = InnlesingId.generate()
        repeat(1) {
            lagrePersongrunnlagForOppgave(
                omsorgsyter = "12345678910",
                annenOmsorgsyter = "04010012797",
                omsorgsmottaker = "07081812345",
                innlesingId = innlesingId
            )
        }

        persongrunnlagProcessingService.processAndExpectResult()

        kontrollbehandlingRepo.bestillKontroll(innlesingId, "test", 2020)

        kontrollbehandlingProcessingService.processAndExpectResult().single().single().also {
            val value = jdbcTemplate.query(
                """select oppgave from kontrollbehandling where id = :id""",
                MapSqlParameterSource(mapOf("id" to it.id)),
                ResultSetExtractor { rs -> if (rs.next()) rs.getString(1) else null }
            )
            assertThat(value).contains("Godskr. omsorgspoeng, flere mottakere: Flere personer har mottatt barnetrygd samme år for barnet under 6 år med fnr 07081812345")
        }
    }

    private fun lagrePersongrunnlag(
        omsorgsyter: String,
        omsorgsmottaker: String,
        innlesingId: InnlesingId
    ) {
        persongrunnlagRepo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = omsorgsyter,
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = omsorgsyter,
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.MARCH),
                                    tom = YearMonth.of(2020, Month.NOVEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = omsorgsmottaker,
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = innlesingId,
                    correlationId = CorrelationId.generate(),
                )
            )
        )!!
    }

    private fun lagrePersongrunnlagForBrev(
        omsorgsyter: String,
        omsorgsmottaker: String,
        innlesingId: InnlesingId
    ) {
        persongrunnlagRepo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = omsorgsyter,
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = omsorgsyter,
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = omsorgsmottaker,
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                            ),
                            hjelpestønadsperioder = listOf(
                                PersongrunnlagMeldingKafka.Hjelpestønadperiode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_3,
                                    omsorgsmottaker = omsorgsmottaker,
                                    kilde = Kilde.INFOTRYGD,
                                )
                            )
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = innlesingId,
                    correlationId = CorrelationId.generate(),
                )
            ),
        )
    }

    private fun lagrePersongrunnlagForOppgave(
        omsorgsyter: String,
        annenOmsorgsyter: String,
        omsorgsmottaker: String,
        innlesingId: InnlesingId
    ) {
        persongrunnlagRepo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = omsorgsyter,
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = omsorgsyter,
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JULY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = omsorgsmottaker,
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = annenOmsorgsyter,
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.JUNE),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = omsorgsmottaker,
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = innlesingId,
                    correlationId = CorrelationId.generate(),
                )
            )
        )!!
    }
}

fun KontrollbehandlingProcessingService.processAndExpectResult(): List<FullførteBehandlinger> {
    return when (val result = this.process()) {
        is Resultat.FantIngenDataÅProsessere -> fail("Expecting result")
        is Resultat.Prosessert -> result.data
    }
}