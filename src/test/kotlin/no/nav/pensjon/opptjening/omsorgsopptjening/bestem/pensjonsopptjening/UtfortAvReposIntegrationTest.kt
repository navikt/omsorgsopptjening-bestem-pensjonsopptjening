package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.Brev
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.repository.BrevRepository
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ingenLøpendeAlderspensjon
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ingenLøpendeUføretrgyd
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ingenUnntaksperioderForMedlemskap
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubForPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.wiremockWithPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjening
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjeningRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.BrevÅrsak
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Status
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.BehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.Oppgave
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveDetaljer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.repository.OppgaveRepo
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.Month
import java.time.YearMonth
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding as PersongrunnlagMeldingKafka

class UtfortAvReposIntegrationTest : SpringContextTest.NoKafka() {

    @Autowired
    private lateinit var persongrunnlagRepo: PersongrunnlagRepo

    @Autowired
    private lateinit var persongrunnlagMeldingProcessingService: PersongrunnlagMeldingProcessingService

    @Autowired
    private lateinit var behandlingRepo: BehandlingRepo

    @Autowired
    private lateinit var oppgaveRepo: OppgaveRepo

    @Autowired
    private lateinit var brevRepository: BrevRepository

    @Autowired
    private lateinit var godskrivOpptjeningRepo: GodskrivOpptjeningRepo

    @MockitoBean
    private lateinit var utfortAvProvider: UtfortAvProvider

    companion object {
        private const val NAV_IDENT = "Z12345"

        @JvmField
        @RegisterExtension
        val wiremock = wiremockWithPdlTransformer()
    }

    @BeforeEach
    override fun beforeEach() {
        super.beforeEach()
        whenever(utfortAvProvider.current()).thenReturn(UtfortAvProvider.UtfortAv.Saksbehandler(NAV_IDENT))
        wiremock.stubForPdlTransformer()
        wiremock.ingenUnntaksperioderForMedlemskap()
        wiremock.ingenLøpendeAlderspensjon()
        wiremock.ingenLøpendeUføretrgyd()
    }

    @Test
    fun `setter utfortAv for persist og statusoppdatering i repoene`() {
        val meldingId = persongrunnlagRepo.lagre(persongrunnlagMelding())!!
        val lagretMelding = persongrunnlagRepo.find(meldingId)
        assertThat((lagretMelding.status as PersongrunnlagMelding.Status.Klar).utfortAv).isEqualTo(NAV_IDENT)

        val behandling = persongrunnlagMeldingProcessingService.processAndExpectResult()
            .firstOrNull { it.alle().isNotEmpty() }
            ?.alle()
            ?.first()
            ?: throw AssertionError("Forventet minst en behandling")
        val lagretBehandling = behandlingRepo.finn(behandling.id)
        assertThat((lagretBehandling.status as Status.Vilkårsvurdert).utfortAv).isEqualTo(NAV_IDENT)

        val oppgave = oppgaveRepo.persist(
            Oppgave.Transient(
                detaljer = OppgaveDetaljer.MottakerOgTekst(
                    oppgavemottaker = "12345678910",
                    oppgavetekst = setOf("Test-oppgave")
                ),
                behandlingId = lagretBehandling.id,
                meldingId = lagretBehandling.meldingId
            )
        )
        assertThat((oppgave.status as Oppgave.Status.Klar).utfortAv).isEqualTo(NAV_IDENT)

        val brev = brevRepository.persist(
            Brev.Transient(
                behandlingId = lagretBehandling.id,
                årsak = BrevÅrsak.OMSORGSYTER_INGEN_PENSJONSPOENG_FORRIGE_ÅR
            )
        )
        assertThat((brev.status as Brev.Status.Klar).utfortAv).isEqualTo(NAV_IDENT)

        val godskriving = godskrivOpptjeningRepo.persist(
            GodskrivOpptjening.Transient(behandlingId = lagretBehandling.id)
        )
        assertThat((godskriving.status as GodskrivOpptjening.Status.Klar).utfortAv).isEqualTo(NAV_IDENT)

        val stoppetMelding = lagretMelding.stoppet("stoppet")
        persongrunnlagRepo.updateStatus(stoppetMelding)
        assertThat((persongrunnlagRepo.find(meldingId).status as PersongrunnlagMelding.Status.Stoppet).utfortAv).isEqualTo(NAV_IDENT)

        val stoppetBehandling = lagretBehandling.stoppet("stoppet")
        behandlingRepo.updateStatus(stoppetBehandling)
        assertThat((behandlingRepo.finn(lagretBehandling.id).status as Status.Stoppet).utfortAv).isEqualTo(NAV_IDENT)

        val stoppetOppgave = oppgave.stoppet("stoppet")
        oppgaveRepo.updateStatus(stoppetOppgave)
        assertThat((oppgaveRepo.find(oppgave.id).status as Oppgave.Status.Stoppet).utfortAv).isEqualTo(NAV_IDENT)

        val stoppetBrev = brev.stoppet("stoppet")
        brevRepository.updateStatus(stoppetBrev)
        assertThat((brevRepository.find(brev.id).status as Brev.Status.Stoppet).utfortAv).isEqualTo(NAV_IDENT)

        val stoppetGodskriving = godskriving.stoppet("stoppet")
        godskrivOpptjeningRepo.updateStatus(stoppetGodskriving)
        assertThat((godskrivOpptjeningRepo.find(godskriving.id).status as GodskrivOpptjening.Status.Stoppet).utfortAv).isEqualTo(NAV_IDENT)
    }

    private fun persongrunnlagMelding() = PersongrunnlagMelding.Lest(
        innhold = PersongrunnlagMeldingKafka(
            omsorgsyter = "12345678910",
            persongrunnlag = listOf(
                PersongrunnlagMeldingKafka.Persongrunnlag(
                    omsorgsyter = "12345678910",
                    omsorgsperioder = listOf(
                        PersongrunnlagMeldingKafka.Omsorgsperiode(
                            fom = YearMonth.of(2020, Month.OCTOBER),
                            tom = YearMonth.of(2020, Month.DECEMBER),
                            omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                            omsorgsmottaker = "01052012345",
                            kilde = Kilde.BARNETRYGD,
                            utbetalt = 7234,
                            landstilknytning = Landstilknytning.NORGE,
                            omsorgsyterHarSelvstendigRett = false,
                        ),
                    ),
                    hjelpestønadsperioder = listOf(
                        PersongrunnlagMeldingKafka.Hjelpestønadperiode(
                            fom = YearMonth.of(2020, Month.OCTOBER),
                            tom = YearMonth.of(2020, Month.DECEMBER),
                            omsorgstype = Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_3,
                            omsorgsmottaker = "01052012345",
                            kilde = Kilde.BARNETRYGD,
                        )
                    )
                ),
            ),
            rådata = Rådata(),
            innlesingId = InnlesingId.generate(),
            correlationId = CorrelationId.generate(),
            opptjeningsAr = 2020,
        )
    )
}
