package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kontroll

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ingenUnntaksperioderForMedlemskap
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubForPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.wiremockWithPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kontrollbehandling.KontrollbehandlingProcessingService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kontrollbehandling.KontrollbehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.BehandlingUtfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.erAvslått
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMeldingProcessingService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.Rådata
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DuplicateKeyException
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

        val behandling = persongrunnlagProcessingService.process()!!.single().single()

        kontrollbehandlingRepo.bestillKontroll(innlesingId, "vil gjerne")

        val kontroll = kontrollbehandlingProcessingService.process()!!.single().single()

        assertThat(behandling.id).isNotEqualTo(kontroll.id)
        assertThat(behandling.opprettet).isNotEqualTo(kontroll.vilkårsvurdering)
        assertThat(behandling.meldingId).isEqualTo(kontroll.meldingId)
        assertThat(behandling.omsorgsAr).isEqualTo(kontroll.omsorgsAr)
        assertThat(behandling.omsorgstype).isEqualTo(kontroll.omsorgstype)
        assertThat(behandling.omsorgsmottaker).isEqualTo(kontroll.omsorgsmottaker)
        assertThat(behandling.omsorgsyter).isEqualTo(kontroll.omsorgsyter)
        assertThat(behandling.grunnlag).isEqualTo(kontroll.grunnlag)
        assertThat(behandling.vilkårsvurdering).isEqualTo(kontroll.vilkårsvurdering)
        assertThat(behandling.utfall).isEqualTo(kontroll.utfall)
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

        persongrunnlagProcessingService.process()!!.also {
            assertThat(it).hasSize(2)
            //påvirker disse hverandre siden man her sjekker om det allerede er innvilget for omsorgsyter/omsorgsmottaker for gitt år
            val (innvilget, avslag) = it.first().single() to it.last().single()
            assertThat(innvilget.utfall).isEqualTo(BehandlingUtfall.Innvilget)
            assertThat(avslag.utfall).isEqualTo(BehandlingUtfall.Avslag)
            assertThat(avslag.vilkårsvurdering.erAvslått<OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering>()).isTrue()
            assertThat(avslag.vilkårsvurdering.erAvslått<OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Vurdering>()).isTrue()
        }

        kontrollbehandlingRepo.bestillKontroll(innlesingId, "forventer samme resultat som normalt")

        kontrollbehandlingProcessingService.process()!!.also {
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

        persongrunnlagProcessingService.process()!!

        kontrollbehandlingRepo.bestillKontroll(innlesingId, "første")
        kontrollbehandlingRepo.bestillKontroll(innlesingId, "andre")
        kontrollbehandlingRepo.bestillKontroll(innlesingId, "tredje")

        kontrollbehandlingProcessingService.process()!!.also {
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

        persongrunnlagProcessingService.process()!!

        kontrollbehandlingRepo.bestillKontroll(innlesingId, "første")
        assertThrows<DuplicateKeyException> { kontrollbehandlingRepo.bestillKontroll(innlesingId, "første") }
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

        persongrunnlagProcessingService.process()!!.also {
            assertThat(it).hasSize(2)
            //påvirker disse hverandre siden man her sjekker om det allerede er innvilget for omsorgsyter/omsorgsmottaker for gitt år
            val (innvilget, avslag) = it.first().single() to it.last().single()
            assertThat(innvilget.utfall).isEqualTo(BehandlingUtfall.Innvilget)
            assertThat(avslag.utfall).isEqualTo(BehandlingUtfall.Avslag)
            assertThat(avslag.vilkårsvurdering.erAvslått<OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering>()).isTrue()
            assertThat(avslag.vilkårsvurdering.erAvslått<OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Vurdering>()).isTrue()
        }

        kontrollbehandlingRepo.bestillKontroll(innlesingId, "forventer at denne innvilges")
        kontrollbehandlingRepo.bestillKontroll(annenInnlesingId, "forventer at denne også innvilges")

        kontrollbehandlingProcessingService.process()!!.also {
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

        val behandlinger = persongrunnlagProcessingService.process()!!

        kontrollbehandlingRepo.bestillKontroll(innlesingId, "test")

        kontrollbehandlingProcessingService.process().also {
            assertThat(it).hasSize(3)
            assertThat(it).hasSize(behandlinger.count())
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
}