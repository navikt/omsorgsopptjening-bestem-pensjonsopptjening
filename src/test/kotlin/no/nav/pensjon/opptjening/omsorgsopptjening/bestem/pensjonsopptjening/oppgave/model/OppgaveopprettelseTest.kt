package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ingenLøpendeAlderspensjon
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ingenLøpendeUføretrgyd
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ingenUnntaksperioderForMedlemskap
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubForPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.unntaksperioderMedPliktigEllerFrivilligMedlemskap
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.wiremockWithPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.repository.OppgaveRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.GyldigOpptjeningår
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMeldingProcessingService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.processAndExpectResult
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.januar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.juli
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.juni
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.Rådata
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Feilinformasjon
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.IdentRolle
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode.Companion.desember
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode.Companion.mars
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode.Companion.september
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.BDDMockito.willAnswer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.Month
import java.time.YearMonth
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding as PersongrunnlagMeldingKafka


class OppgaveopprettelseTest : SpringContextTest.NoKafka() {

    @Autowired
    private lateinit var persongrunnlagRepo: PersongrunnlagRepo

    @Autowired
    private lateinit var persongrunnlagMeldingProcessingService: PersongrunnlagMeldingProcessingService

    @MockBean
    private lateinit var gyldigOpptjeningår: GyldigOpptjeningår

    @Autowired
    private lateinit var oppgaveRepo: OppgaveRepo

    companion object {
        @JvmField
        @RegisterExtension
        val wiremock = wiremockWithPdlTransformer()
    }

    @Test
    fun `gitt to omsorgsytere med like mange omsorgsmåneder for samme barn i samme omsorgsår skal det opprettes oppgave for den som mottok i desember`() {
        wiremock.stubForPdlTransformer()
        willAnswer { true }.given(gyldigOpptjeningår).erGyldig(2020)
        wiremock.ingenUnntaksperioderForMedlemskap()
        wiremock.ingenLøpendeAlderspensjon()
        wiremock.ingenLøpendeUføretrgyd()

        persongrunnlagRepo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2020),
                                    tom = juni(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "04010012797",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = juli(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().first().single().also { behandling ->
            assertThat(behandling.erInnvilget()).isFalse()
            assertThat(oppgaveRepo.findForMelding(behandling.meldingId)).isEmpty()
            assertThat(oppgaveRepo.findForBehandling(behandling.id)).isEmpty()
        }

        persongrunnlagRepo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "04010012797",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2020),
                                    tom = juni(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "04010012797",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = juli(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().first().single().also { behandling ->
            assertThat(behandling.erInnvilget()).isFalse()
            oppgaveRepo.findForMelding(behandling.meldingId).single().also { oppgave ->
                assertThat(oppgaveRepo.findForBehandling(behandling.id)).containsOnly(oppgave)
                assertThat(
                    oppgave.detaljer
                ).isEqualTo(
                    OppgaveDetaljer.MottakerOgTekst(
                        oppgavemottaker = "04010012797",
                        oppgavetekst = setOf("""Godskr. omsorgspoeng, flere mottakere: Flere personer har mottatt barnetrygd samme år for barnet under 6 år med fnr 07081812345. Den bruker som oppgaven gjelder mottok barnetrygd i minst seks måneder, og hadde barnetrygd i desember måned. Bruker med fnr 12345678910 mottok også barnetrygd for 6 måneder i samme år. Vurder hvem som skal ha omsorgspoengene.""")
                    )
                )
            }
        }
    }

    @Test
    fun `gitt to omsorgsytere med like mange omsorgsmåneder for samme barn i samme omsorgsår kan det opprettes oppgave for begge dersom en av foreldrene inngår i en annen familiekonstellasjon hvor oppgave også skal opprettes`() {
        wiremock.stubForPdlTransformer()
        willAnswer { true }.given(gyldigOpptjeningår).erGyldig(2020)
        wiremock.ingenUnntaksperioderForMedlemskap()
        wiremock.ingenLøpendeAlderspensjon()
        wiremock.ingenLøpendeUføretrgyd()

        persongrunnlagRepo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "04010012797",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01052012345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().first().single().also { behandling ->
            assertThat(behandling.erInnvilget()).isFalse()
            oppgaveRepo.findForMelding(behandling.meldingId).single().also { oppgave ->
                assertThat(oppgaveRepo.findForBehandling(behandling.id)).containsOnly(oppgave)
                assertThat(
                    oppgave.detaljer
                ).isEqualTo(
                    OppgaveDetaljer.MottakerOgTekst(
                        oppgavemottaker = "12345678910",
                        oppgavetekst = setOf("""Godskr. omsorgspoeng, flere mottakere: Flere personer har mottatt barnetrygd samme år for barnet under 6 år med fnr 07081812345. Den bruker som oppgaven gjelder mottok barnetrygd i minst seks måneder, og hadde barnetrygd i desember måned. Bruker med fnr 04010012797 mottok også barnetrygd for 6 måneder i samme år. Vurder hvem som skal ha omsorgspoengene.""")
                    )
                )
            }
        }

        persongrunnlagRepo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "04010012797",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "04010012797",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = juni(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01052012345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "01018212345",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = juni(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01052012345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().first().also { behandlinger ->
            assertThat(behandlinger.antallBehandlinger()).isEqualTo(2)
            oppgaveRepo.findForMelding(behandlinger.alle()[0].meldingId).single().also { oppgave ->
                assertThat(oppgaveRepo.findForBehandling(behandlinger.finnBehandlingsId()[0])).isEmpty()
                assertThat(oppgaveRepo.findForBehandling(behandlinger.finnBehandlingsId()[1])).containsOnly(oppgave)
                assertThat(
                    oppgave.detaljer
                ).isEqualTo(
                    OppgaveDetaljer.MottakerOgTekst(
                        oppgavemottaker = "04010012797",
                        oppgavetekst = setOf(
                            "Godskriving omsorgspoeng: Manuell behandling. Godskriving for barn med fnr: 01052012345 må vurderes manuelt pga at andre foreldre: 12345678910 mottar barnetrygd for felles barn: 07081812345",
                            "Godskr. omsorgspoeng, flere mottakere: Flere personer som har mottatt barnetrygd samme år for barnet med fnr 01052012345 i barnets fødselsår. Vurder hvem som skal ha omsorgspoengene."
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `gitt to omsorgsytere med like mange omsorgsmåneder for samme barn i samme omsorgsår skal det opprettes oppgave for den som mottok i desember - hvis begge mottok i desember opprettes det for en av partene`() {
        wiremock.stubForPdlTransformer()
        willAnswer { true }.given(gyldigOpptjeningår).erGyldig(2020)
        wiremock.ingenUnntaksperioderForMedlemskap()
        wiremock.ingenLøpendeAlderspensjon()
        wiremock.ingenLøpendeUføretrgyd()

        persongrunnlagRepo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "04010012797",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().first().single().also { behandling ->
            assertThat(behandling.erInnvilget()).isFalse()
            oppgaveRepo.findForMelding(behandling.meldingId).single().also { oppgave ->
                assertThat(oppgaveRepo.findForBehandling(behandling.id)).containsOnly(oppgave)
                assertThat(
                    oppgave.detaljer
                ).isEqualTo(
                    OppgaveDetaljer.MottakerOgTekst(
                        oppgavemottaker = "12345678910",
                        oppgavetekst = setOf(
                            "Godskr. omsorgspoeng, flere mottakere: Flere personer har mottatt barnetrygd samme år for barnet under 6 år med fnr 07081812345. Den bruker som oppgaven gjelder mottok barnetrygd i minst seks måneder, og hadde barnetrygd i desember måned. Bruker med fnr 04010012797 mottok også barnetrygd for 6 måneder i samme år. Vurder hvem som skal ha omsorgspoengene.",
                            "Bruker mottok barnetrygd i minst 6 måneder, men hele eller deler av perioden var delt barnetrygd for barn med fnr: 07081812345. Vurder retten til omsorgsopptjening."
                        )
                    )
                )
            }
        }

        persongrunnlagRepo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "04010012797",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "04010012797",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )
        persongrunnlagMeldingProcessingService.processAndExpectResult().first().single().also { behandling ->
            assertThat(behandling.erInnvilget()).isFalse()
            assertThat(oppgaveRepo.findForMelding(behandling.meldingId)).isEmpty()
            assertThat(oppgaveRepo.findForBehandling(behandling.id)).isEmpty()
        }
    }

    @Test
    fun `gitt at to omsorgsytere har like mange omsorgsmåneder for flere barn innenfor samme omsorgsår opprettes det bare en oppgave, men oppgaven inneholder informasjon om begge tilfellene`() {
        wiremock.stubForPdlTransformer()
        willAnswer { true }.given(gyldigOpptjeningår).erGyldig(2020)
        wiremock.ingenUnntaksperioderForMedlemskap()
        wiremock.ingenLøpendeAlderspensjon()
        wiremock.ingenLøpendeUføretrgyd()

        persongrunnlagRepo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.JULY),
                                    tom = YearMonth.of(2021, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01122012345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "04010012797",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.JANUARY),
                                    tom = YearMonth.of(2021, Month.JUNE),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01122012345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().first().let { result ->
            assertThat(result.antallBehandlinger()).isEqualTo(2)
            result.alle().first().also { behandling ->
                assertThat(behandling.erInnvilget()).isFalse()
                oppgaveRepo.findForMelding(behandling.meldingId).single().also { oppgave ->
                    assertThat(oppgave.detaljer).isEqualTo(
                        OppgaveDetaljer.MottakerOgTekst(
                            oppgavemottaker = "12345678910",
                            oppgavetekst = setOf(
                                "Godskr. omsorgspoeng, flere mottakere: Flere personer har mottatt barnetrygd samme år for barnet under 6 år med fnr 07081812345. Den bruker som oppgaven gjelder mottok barnetrygd i minst seks måneder, og hadde barnetrygd i desember måned. Bruker med fnr 04010012797 mottok også barnetrygd for 6 måneder i samme år. Vurder hvem som skal ha omsorgspoengene.",
                                "Godskr. omsorgspoeng, flere mottakere: Flere personer som har mottatt barnetrygd samme år for barnet med fnr 01122012345 i barnets fødselsår. Vurder hvem som skal ha omsorgspoengene."
                            )
                        )
                    )
                }
            }
            result.alle().last().also { behandling ->
                assertThat(behandling.erInnvilget()).isFalse()
                assertThat(oppgaveRepo.findForBehandling(behandling.id)).isEmpty()
                assertThat(oppgaveRepo.findForMelding(behandling.meldingId)).hasSize(1)
            }
        }
    }

    @Test
    fun `manuell behandling med oppgave dersom bruker kan få innvilget omsorgsopptjening hvis perioder med pliktig eller frivillig medlemskap telles med`() {
        wiremock.stubForPdlTransformer()
        willAnswer { true }.given(gyldigOpptjeningår).erGyldig(2020)
        wiremock.unntaksperioderMedPliktigEllerFrivilligMedlemskap(
            "12345678910",
            setOf(Periode(mars(2020), september(2020)))
        )
        wiremock.ingenLøpendeAlderspensjon()
        wiremock.ingenLøpendeUføretrgyd()

        persongrunnlagRepo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().single().let { result ->
            result.alle().single().also { behandling ->
                assertThat(behandling.erManuell()).isTrue()
                oppgaveRepo.findForMelding(behandling.meldingId).single().also { oppgave ->
                    assertThat(oppgaveRepo.findForBehandling(behandling.id)).containsOnly(oppgave)
                    assertThat(oppgave.detaljer).isEqualTo(
                        OppgaveDetaljer.MottakerOgTekst(
                            oppgavemottaker = "12345678910",
                            oppgavetekst = setOf("""Godskriving omsorgspoeng: Manuell behandling. Godskriving for barn med fnr: 07081812345 må vurderes manuelt pga perioder i MEDL""")
                        )
                    )
                }
            }
        }
    }

    @Test
    fun `manuell behandling med oppgave dersom delt omsorg for hjelpestønadsmottaker`() {
        wiremock.stubForPdlTransformer()
        willAnswer { true }.given(gyldigOpptjeningår).erGyldig(2020)
        wiremock.ingenUnntaksperioderForMedlemskap()
        wiremock.ingenLøpendeAlderspensjon()
        wiremock.ingenLøpendeUføretrgyd()

        persongrunnlagRepo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                            ),
                            hjelpestønadsperioder = listOf(
                                PersongrunnlagMeldingKafka.Hjelpestønadperiode(
                                    fom = januar(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_4,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.INFOTRYGD
                                )
                            ),
                        ),
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "04010012797",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                            ),
                            hjelpestønadsperioder = listOf(
                                PersongrunnlagMeldingKafka.Hjelpestønadperiode(
                                    fom = januar(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_4,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.INFOTRYGD
                                )
                            ),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().single().let { result ->
            result.alle().single().also { behandling ->
                assertThat(behandling.erManuell()).isTrue()
                oppgaveRepo.findForMelding(behandling.meldingId).single().also { oppgave ->
                    assertThat(oppgaveRepo.findForBehandling(behandling.id)).containsOnly(oppgave)
                    assertThat(oppgave.detaljer).isEqualTo(
                        OppgaveDetaljer.MottakerOgTekst(
                            oppgavemottaker = "12345678910",
                            oppgavetekst = setOf(
                                "Godskr. omsorgspoeng, flere mottakere: Flere personer har mottatt barnetrygd samme år for barnet under 6 år med fnr 03041212345. Den bruker som oppgaven gjelder mottok barnetrygd i minst seks måneder, og hadde barnetrygd i desember måned. Bruker med fnr 04010012797 mottok også barnetrygd for 6 måneder i samme år. Vurder hvem som skal ha omsorgspoengene.",
                                "Bruker mottok barnetrygd i minst 6 måneder, men hele eller deler av perioden var delt barnetrygd for barn med fnr: 03041212345. Vurder retten til omsorgsopptjening."
                            )
                        )
                    )
                }
            }
        }
    }

    @Test
    fun `manuell behandling med oppgave dersom det er innvilget opptjening for fellesbarn omsorgsyter ikke mottar barnetrygd for`() {
        wiremock.stubForPdlTransformer()
        willAnswer { true }.given(gyldigOpptjeningår).erGyldig(2020)
        wiremock.ingenUnntaksperioderForMedlemskap()
        wiremock.ingenLøpendeAlderspensjon()
        wiremock.ingenLøpendeUføretrgyd()

        persongrunnlagRepo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().single().let { result ->
            result.alle().single().also { behandling -> assertThat(behandling.erInnvilget()).isTrue() }
        }

        persongrunnlagRepo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "04010012797",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "04010012797",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2021),
                                    tom = desember(2021),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01122012345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().single().let { result ->
            result.alle().single().also { behandling ->
                assertThat(behandling.erManuell()).isTrue()
                oppgaveRepo.findForBehandling(behandling.id).single().also { oppgave ->
                    assertThat(oppgave.detaljer).isEqualTo(
                        OppgaveDetaljer.MottakerOgTekst(
                            oppgavemottaker = "04010012797",
                            oppgavetekst = setOf("""Godskriving omsorgspoeng: Manuell behandling. Godskriving for barn med fnr: 01122012345 må vurderes manuelt pga at andre foreldre: 12345678910 mottar barnetrygd for felles barn: 07081812345""")
                        )
                    )
                }
            }
        }
    }

    @Test
    fun `oppretter oppgaver for meldinger som inneholder feil oversendt fra start`() {
        val melding = persongrunnlagRepo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = emptyList(),
                    feilinfo = listOf(
                        Feilinformasjon.OverlappendeBarnetrygdperioder(
                            message = "obt",
                            exceptionType = "et",
                            exceptionMessage = "em",
                            omsorgsmottaker = "om"
                        ),
                        Feilinformasjon.OverlappendeHjelpestønadperioder(
                            message = "ohs",
                            exceptionType = "et",
                            exceptionMessage = "em",
                            omsorgsmottaker = "om"
                        ),
                        Feilinformasjon.FeilIDataGrunnlag(
                            message = "fidg",
                            exceptionType = "et",
                            exceptionMessage = "em"
                        ),
                        Feilinformasjon.UgyldigIdent(
                            message = "ugyldig ident",
                            exceptionType = "RuntimeException",
                            exceptionMessage = "error",
                            ident = "12345678910",
                            identRolle = IdentRolle.BARNETRYGDMOTTAKER
                        )
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )!!

        persongrunnlagMeldingProcessingService.process()

        persongrunnlagRepo.find(melding).also {
            assertThat(it.status).isInstanceOf(PersongrunnlagMelding.Status.Ferdig::class.java)
        }

        oppgaveRepo.findForMelding(melding).also {
            assertThat(it).hasSize(1)
            assertThat(it.single().oppgavetekst).containsAll(
                setOf(
                    "Kunne ikke behandle godskriving av omsorgsopptjening automatisk for 12345678910 på grunn av motstridende opplysninger for barnetrygdperiodene tilhørende et av barna. Vurder omsorgsopptjening manuelt.",
                    "Kunne ikke behandle godskriving av omsorgsopptjening automatisk for 12345678910 på grunn av motstridende opplysninger for hjelpestønadsperiodene tilhørende et av barna. Vurder omsorgsopptjening manuelt.",
                    "Kunne ikke behandle godskriving av omsorgsopptjening automatisk for 12345678910 på grunn av feil i datagrunnlaget. Vurder omsorgsopptjening manuelt.",
                    "Kunne ikke behandle godskriving av omsorgsopptjening automatisk for 12345678910 på grunn av at det ikke eksisterer et gjeldende fnr for barnetrygdmottaker med ident: 12345678910"
                )
            )
        }
    }
}