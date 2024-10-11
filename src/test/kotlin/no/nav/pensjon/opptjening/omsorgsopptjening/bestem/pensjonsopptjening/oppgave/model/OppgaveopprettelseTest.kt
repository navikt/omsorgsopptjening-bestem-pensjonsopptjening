package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ingenUnntaksperioderForMedlemskap
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubForPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.unntaksperioderMedPliktigEllerFrivilligMedlemskap
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.wiremockWithPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.repository.OppgaveRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.GyldigOpptjeningår
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMeldingService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.desember
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.januar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.mars
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.september
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.Rådata
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
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
    private lateinit var repo: PersongrunnlagRepo

    @Autowired
    private lateinit var handler: PersongrunnlagMeldingService

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

        repo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.JUNE),
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
                                    fom = YearMonth.of(2020, Month.JULY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
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

        handler.process()!!.first().single().also { behandling ->
            assertThat(behandling.erInnvilget()).isFalse()
            assertThat(oppgaveRepo.findForMelding(behandling.meldingId)).isEmpty()
            assertThat(oppgaveRepo.findForBehandling(behandling.id)).isEmpty()
        }

        repo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "04010012797",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.JUNE),
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
                                    fom = YearMonth.of(2020, Month.JULY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
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

        handler.process()!!.first().single().also { behandling ->
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

        repo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
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
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
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

        handler.process()!!.first().single().also { behandling ->
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

        repo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "04010012797",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
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
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JUNE),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
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
                                    fom = YearMonth.of(2020, Month.JUNE),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
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

        handler.process()!!.first().also { behandlinger ->
            assertThat(behandlinger.antallBehandlinger()).isEqualTo(2)
            oppgaveRepo.findForMelding(behandlinger.alle()[0].meldingId).single().also { oppgave ->
                assertThat(oppgaveRepo.findForBehandling(behandlinger.finnBehandlingsId()[0])).isEmpty()
                assertThat(oppgaveRepo.findForBehandling(behandlinger.finnBehandlingsId()[1])).containsOnly(oppgave)
                assertThat(
                    oppgave.detaljer
                ).isEqualTo(
                    OppgaveDetaljer.MottakerOgTekst(
                        oppgavemottaker = "04010012797",
                        oppgavetekst = setOf("""Godskr. omsorgspoeng, flere mottakere: Flere personer som har mottatt barnetrygd samme år for barnet med fnr 01052012345 i barnets fødselsår. Vurder hvem som skal ha omsorgspoengene.""")
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

        repo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
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
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
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

        handler.process()!!.first().single().also { behandling ->
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

        repo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "04010012797",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
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
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
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
        handler.process()!!.first().single().also { behandling ->
            assertThat(behandling.erInnvilget()).isFalse()
            assertThat(oppgaveRepo.findForMelding(behandling.meldingId)).isEmpty()
            assertThat(oppgaveRepo.findForBehandling(behandling.id)).isEmpty()
        }
    }

    @Test
    fun `gitt at to omsorgsytere har like mange omsorgsmåneder for flere barn innenfor samme omsorgsår opprettes det bare oppgave for det eldste barnet`() {
        wiremock.stubForPdlTransformer()
        willAnswer { true }.given(gyldigOpptjeningår).erGyldig(2020)
        wiremock.ingenUnntaksperioderForMedlemskap()

        repo.lagre(
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
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
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
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
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

        handler.process()!!.first().let { result ->
            assertThat(result.antallBehandlinger()).isEqualTo(2)
            result.alle().first().also { behandling ->
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

        repo.lagre(
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

        handler.process()!!.single().let { result ->
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
}