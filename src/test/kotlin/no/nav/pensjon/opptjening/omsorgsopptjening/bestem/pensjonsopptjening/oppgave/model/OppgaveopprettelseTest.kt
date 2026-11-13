package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ingenLøpendeAlderspensjon
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ingenLøpendeUføretrgyd
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ingenUnntaksperioderForMedlemskap
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubForPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.unntaksperioderMedPliktigEllerFrivilligMedlemskap
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.wiremockWithPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.repository.OppgaveRepo
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
import org.springframework.beans.factory.annotation.Autowired
import java.time.Month
import java.time.YearMonth
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding as PersongrunnlagMeldingKafka

class OppgaveopprettelseTest : SpringContextTest.NoKafka() {

    @Autowired
    private lateinit var persongrunnlagRepo: PersongrunnlagRepo

    @Autowired
    private lateinit var persongrunnlagMeldingProcessingService: PersongrunnlagMeldingProcessingService

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
                                    landstilknytning = Landstilknytning.NORGE,
                                    omsorgsyterHarSelvstendigRett = false,
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
                                    landstilknytning = Landstilknytning.NORGE,
                                    omsorgsyterHarSelvstendigRett = false,
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                    opptjeningsAr = 2020,
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
                                    landstilknytning = Landstilknytning.NORGE,
                                    omsorgsyterHarSelvstendigRett = false,
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
                                    landstilknytning = Landstilknytning.NORGE,
                                    omsorgsyterHarSelvstendigRett = false,
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                    opptjeningsAr = 2020,
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
                        oppgavetekst = setOf("""Flere barnetrygdmottakere. Flere brukere mottar like mye barnetrygd for samme barn 07081812345. Bruker 12345678910 mottok også barnetrygd""")
                    )
                )
            }
        }
    }

    @Test
    fun `gitt to omsorgsytere med like mange omsorgsmåneder for samme barn i samme omsorgsår kan det opprettes oppgave for begge dersom en av foreldrene inngår i en annen familiekonstellasjon hvor oppgave også skal opprettes`() {
        wiremock.stubForPdlTransformer()
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
                                    landstilknytning = Landstilknytning.NORGE,
                                    omsorgsyterHarSelvstendigRett = false,
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
                                    landstilknytning = Landstilknytning.NORGE,
                                    omsorgsyterHarSelvstendigRett = false,
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01052012345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE,
                                    omsorgsyterHarSelvstendigRett = false,
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                    opptjeningsAr = 2020,
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
                        oppgavetekst = setOf("""Flere barnetrygdmottakere. Flere brukere mottar like mye barnetrygd for samme barn 07081812345. Bruker 04010012797 mottok også barnetrygd""")
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
                                    landstilknytning = Landstilknytning.NORGE,
                                    omsorgsyterHarSelvstendigRett = false,
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
                                    landstilknytning = Landstilknytning.NORGE,
                                    omsorgsyterHarSelvstendigRett = false,
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = juni(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01052012345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE,
                                    omsorgsyterHarSelvstendigRett = false,
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
                                    landstilknytning = Landstilknytning.NORGE,
                                    omsorgsyterHarSelvstendigRett = false,
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                    opptjeningsAr = 2020,
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
                            "Flere barnetrygdmottakere. Flere brukere mottar barnetrygd for samme barn 01052012345 i fødselsåret",
                            "Ulike felles barn. Foreldrene mottar barnetrygd for disse barna 07081812345,01052012345. Den andre forelderen 12345678910 har egen oppgave."
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `gitt to omsorgsytere med like mange omsorgsmåneder for samme barn i samme omsorgsår skal det opprettes oppgave for den som mottok i desember - hvis begge mottok i desember opprettes det for en av partene`() {
        wiremock.stubForPdlTransformer()
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
                                    landstilknytning = Landstilknytning.NORGE,
                                    omsorgsyterHarSelvstendigRett = false,
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
                                    landstilknytning = Landstilknytning.NORGE,
                                    omsorgsyterHarSelvstendigRett = false,
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                    opptjeningsAr = 2020,
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
                            "Delt barnetrygd. Bruker mottar barnetrygd i minst 6 måneder, men hele eller deler er delt barnetrygd for barnet 07081812345",
                            "Flere barnetrygdmottakere. Flere brukere mottar like mye barnetrygd for samme barn 07081812345. Bruker 04010012797 mottok også barnetrygd"
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
                                    landstilknytning = Landstilknytning.NORGE,
                                    omsorgsyterHarSelvstendigRett = false,
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
                                    landstilknytning = Landstilknytning.NORGE,
                                    omsorgsyterHarSelvstendigRett = false,
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                    opptjeningsAr = 2020,
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
    fun `gitt at to omsorgsytere har like mange omsorgsmåneder for flere barn innenfor samme omsorgsår opprettes det bare oppgave for det eldste barnet`() {
        wiremock.stubForPdlTransformer()
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
                                    landstilknytning = Landstilknytning.NORGE,
                                    omsorgsyterHarSelvstendigRett = false,
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE,
                                    omsorgsyterHarSelvstendigRett = false,
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
                                    landstilknytning = Landstilknytning.NORGE,
                                    omsorgsyterHarSelvstendigRett = false,
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE,
                                    omsorgsyterHarSelvstendigRett = false,
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                    opptjeningsAr = 2020,
                )
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().first().let { result ->
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
                            oppgavetekst = setOf("""Flere barnetrygdmottakere. Flere brukere mottar like mye barnetrygd for samme barn 07081812345. Bruker 04010012797 mottok også barnetrygd""")
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
                                    landstilknytning = Landstilknytning.NORGE,
                                    omsorgsyterHarSelvstendigRett = false,
                                ),
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                    opptjeningsAr = 2020,
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
                            oppgavetekst = setOf("Medlemskap. Bruker er pliktig eller frivillig medlem i utlandet i perioden hen mottar barnetrygd for barnet 07081812345")
                        )
                    )
                }
            }
        }
    }

    @Test
    fun `manuell behandling med oppgave dersom delt omsorg for hjelpestønadsmottaker`() {
        wiremock.stubForPdlTransformer()
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
                                    landstilknytning = Landstilknytning.NORGE,
                                    omsorgsyterHarSelvstendigRett = false,
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
                                    landstilknytning = Landstilknytning.NORGE,
                                    omsorgsyterHarSelvstendigRett = false,
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
                    opptjeningsAr = 2020,
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
                                "Delt barnetrygd. Bruker mottar barnetrygd i minst 6 måneder, men hele eller deler er delt barnetrygd for barnet 03041212345",
                                "Flere barnetrygdmottakere. Flere brukere mottar like mye barnetrygd for samme barn 03041212345. Bruker 04010012797 mottok også barnetrygd"
                            )
                        )
                    )
                }
            }
        }
    }

    @Test
    fun `manuell behandling med oppgave for begge foreldre dersom det er innvilget opptjening for fellesbarn omsorgsyter ikke mottar barnetrygd for`() {
        wiremock.stubForPdlTransformer()
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
                                    landstilknytning = Landstilknytning.NORGE,
                                    omsorgsyterHarSelvstendigRett = false,
                                ),
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                    opptjeningsAr = 2020,
                )
            ),
        )

        val first = persongrunnlagMeldingProcessingService.processAndExpectResult().single().let { result ->
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
                                    landstilknytning = Landstilknytning.NORGE,
                                    omsorgsyterHarSelvstendigRett = false,
                                ),
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                    opptjeningsAr = 2020,
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
                            oppgavetekst = setOf("Ulike felles barn. Foreldrene mottar barnetrygd for disse barna 07081812345,01122012345. Den andre forelderen 12345678910 har egen oppgave.")
                        )
                    )
                }
            }
        }

        persongrunnlagMeldingProcessingService.processAndExpectResult().single().let { result ->
            result.alle().single().also { behandling ->
                assertThat(behandling.erManuell()).isTrue()
                assertThat(behandling.omsorgsyter).isEqualTo("12345678910")

                oppgaveRepo.findForBehandling(behandling.id).single().also { oppgave ->
                    assertThat(oppgave.detaljer).isEqualTo(
                        OppgaveDetaljer.MottakerOgTekst(
                            oppgavemottaker = "12345678910",
                            oppgavetekst = setOf("Ulike felles barn. Foreldrene mottar barnetrygd for disse barna 01122012345,07081812345. Den andre forelderen 04010012797 har egen oppgave.")
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
                    opptjeningsAr = 2020,
                )
            ),
        )!!

        persongrunnlagMeldingProcessingService.process()

        persongrunnlagRepo.find(melding).also {
            assertThat(it.status).isInstanceOf(PersongrunnlagMelding.Status.Ferdig::class.java)
        }

        oppgaveRepo.findForMelding(melding).also {
            assertThat(it).hasSize(1)
            it.single().also {
                assertThat(it.mottaker).isEqualTo("12345678910")
                assertThat(it.oppgavetekst).containsAll(
                    setOf(
                        "Teknisk. Kunne ikke behandle automatisk. Motstridende opplysninger for hjelpestønadsperiodene tilhørende et av barna",
                        "Teknisk. Kunne ikke behandle automatisk på grunn av feil i datagrunnlaget",
                        "Teknisk. Kunne ikke behandle automatisk. Motstridende opplysninger for barnetrygdperiodene tilhørende et av barna",
                        "Teknisk. Kunne ikke behandle automatisk. Det mangler gjeldende fødselsnummer for barnetrygdmottaker med ident 12345678910"
                    )
                )
            }
        }
    }
}