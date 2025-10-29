package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model


import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ingenLøpendeAlderspensjon
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ingenLøpendeUføretrgyd
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ingenUnntaksperioderForMedlemskap
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.løpendeAlderspensjon
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.løpendeUføretrygd
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubForPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.unntaksperioderUtenMedlemskap
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.wiremockWithPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.AntallMånederRegel
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.BehandlingUtfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførtBehandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførteBehandlinger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterErForelderTilMottakerAvHjelpestønad
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterErMedlemIFolketrygden
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterErikkeOmsorgsmottaker
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarGyldigOmsorgsarbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarTilstrekkeligOmsorgsarbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterMottarBarnetrgyd
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Oppgaveopplysninger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.VilkårsvurderingUtfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.erEnesteAvslag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.erEnesteUbestemt
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.erInnvilget
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.finnAlleAvslatte
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.finnVurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.Oppgave
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.august
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.desember
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.januar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.juli
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.juni
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.mai
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.oktober
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.september
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.år
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.Rådata
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.BDDMockito.willAnswer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.Month
import java.time.YearMonth
import kotlin.test.Test
import kotlin.test.assertFalse
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Landstilknytning as KafkaLandstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding as PersongrunnlagMeldingKafka

class PersongrunnlagMeldingServiceImplTest : SpringContextTest.NoKafka() {
    @Autowired
    private lateinit var repo: PersongrunnlagRepo

    @Autowired
    private lateinit var persongrunnlagMeldingProcessingService: PersongrunnlagMeldingProcessingService

    @MockitoBean
    private lateinit var gyldigOpptjeningår: GyldigOpptjeningår

    companion object {
        @JvmField
        @RegisterExtension
        val wiremock = wiremockWithPdlTransformer()
        const val OPPTJENINGSÅR = 2020
    }

    @BeforeEach
    override fun beforeEach() {
        super.beforeEach()
        wiremock.stubForPdlTransformer()
        willAnswer { true }.given(gyldigOpptjeningår).erGyldig(OPPTJENINGSÅR)
        wiremock.ingenUnntaksperioderForMedlemskap()
        wiremock.ingenLøpendeAlderspensjon()
        wiremock.ingenLøpendeUføretrgyd()
    }

    @Test
    fun `gitt et barn født i desember i opptjeningsåret skal det innvilges opptjening dersom omsorgsyter har ytt omsorg i året etter opptjeningsåret (uavhengig av antall måneder)`() {
        repo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.JANUARY),
                                    tom = YearMonth.of(2025, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01122012345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                ),
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().first().single().also { behandling ->
            behandling.assertInnvilget(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "01122012345"
            )
            assertInstanceOf(OmsorgsopptjeningGrunnlag.FødtIOmsorgsår.FødtDesember::class.java, behandling.grunnlag)
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering>()
                .also { vurdering ->
                    assertInstanceOf(
                        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag::class.java,
                        vurdering.grunnlag
                    ).also {
                        assertEquals(
                            Periode(2021).alleMåneder(),
                            it.omsorgsmåneder().alle(),
                        )
                        assertEquals(
                            AntallMånederRegel.FødtIOmsorgsår,
                            it.antallMånederRegel,
                        )
                    }
                }
        }
    }

    @Test
    fun `gitt et barn født i desember i opptjeningsåret gis det ingen opptjening dersom omsorgsyter ikke har ytt omsorg i året etter omsorgsåret`() {
        repo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2022, Month.JANUARY),
                                    tom = YearMonth.of(2025, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01122012345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                ),
            ),
        )
        assertEquals(0, persongrunnlagMeldingProcessingService.processAndExpectResult().first().antallBehandlinger())
    }

    @Test
    fun `gitt et barn født i andre måneder enn desember i opptjeningsåret skal det innvilges opptjening dersom omsorgsyter har ytt omsorg i opptjeningsåret (uavhengig av antall måneder) - 3 måneder`() {
        repo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = oktober(2020),
                                    tom = YearMonth.of(2025, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01052012345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                ),
            ),
        )


        persongrunnlagMeldingProcessingService.processAndExpectResult().first().single().also { behandling ->
            behandling.assertInnvilget(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "01052012345"
            )
            assertInstanceOf(OmsorgsopptjeningGrunnlag.FødtIOmsorgsår.IkkeFødtDesember::class.java, behandling.grunnlag)
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering>()
                .also { vurdering ->
                    assertInstanceOf(
                        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag::class.java,
                        vurdering.grunnlag
                    ).also {
                        assertEquals(
                            Periode(
                                oktober(2020),
                                desember(2020)
                            ).alleMåneder(),
                            it.omsorgsmåneder().alle()
                        )
                        assertEquals(
                            AntallMånederRegel.FødtIOmsorgsår,
                            it.antallMånederRegel,
                        )
                    }
                }
        }
    }

    @Test
    fun `gitt et barn født i andre måneder enn desember i opptjeningsåret skal det innvilges opptjening dersom omsorgsyter har ytt omsorg i opptjeningsåret (uavhengig av antall måneder) - 7 måneder`() {
        repo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = mai(2020),
                                    tom = YearMonth.of(2025, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01052012345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                ),
            ),
        )


        persongrunnlagMeldingProcessingService.processAndExpectResult().first().single().also { behandling ->
            behandling.assertInnvilget(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "01052012345"
            )
            assertInstanceOf(OmsorgsopptjeningGrunnlag.FødtIOmsorgsår.IkkeFødtDesember::class.java, behandling.grunnlag)
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering>()
                .also { vurdering ->
                    assertInstanceOf(
                        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag::class.java,
                        vurdering.grunnlag
                    ).also {
                        assertEquals(
                            Periode(
                                mai(2020),
                                desember(2020)
                            ).alleMåneder(),
                            it.omsorgsmåneder().alle()
                        )
                        assertEquals(
                            AntallMånederRegel.FødtIOmsorgsår,
                            it.antallMånederRegel,
                        )
                    }
                }
        }
    }

    @Test
    fun `gitt et barn født utenfor opptjeningsåret skal det innvilges opptjening dersom omsorgsyter har ytt et halvt år eller mer omsorg`() {
        repo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2018, Month.SEPTEMBER),
                                    tom = YearMonth.of(2025, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                ),
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().first().single().also { behandling ->
            behandling.assertInnvilget(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "07081812345"
            )
            assertInstanceOf(OmsorgsopptjeningGrunnlag.IkkeFødtIOmsorgsår::class.java, behandling.grunnlag)
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering>()
                .also { vurdering ->
                    assertInstanceOf(
                        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag::class.java,
                        vurdering.grunnlag
                    ).also {
                        assertEquals(
                            Periode(2020).alleMåneder(),
                            it.omsorgsmåneder().alle()
                        )
                        assertEquals(
                            AntallMånederRegel.FødtUtenforOmsorgsår,
                            it.antallMånederRegel,
                        )
                    }
                }
        }
    }

    @Test
    fun `gitt et barn født utenfor opptjeningsåret skal det avslås opptjening dersom omsorgsyter har ytt mindre enn et halvt år omsorg - full omsorg`() {
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
                                    tom = mai(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                ),
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().first().single().also { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "07081812345"
            )
            assertInstanceOf(OmsorgsopptjeningGrunnlag.IkkeFødtIOmsorgsår::class.java, behandling.grunnlag)
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering>()
                .also { vurdering ->
                    assertInstanceOf(
                        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag::class.java,
                        vurdering.grunnlag
                    ).also {
                        assertEquals(
                            Periode(januar(2020), mai(2020)).alleMåneder(),
                            it.omsorgsmåneder().alle()
                        )
                        assertEquals(
                            AntallMånederRegel.FødtUtenforOmsorgsår,
                            it.antallMånederRegel,
                        )
                    }
                }
            assertEquals(
                listOf(
                    behandling.vilkårsvurdering.finnVurdering<OmsorgsyterMottarBarnetrgyd.Vurdering>(),
                    behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering>(),
                    behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarGyldigOmsorgsarbeid.Vurdering>(),
                ), behandling.vilkårsvurdering.finnAlleAvslatte()
            )
        }
    }

    @Test
    fun `gitt et barn født utenfor opptjeningsåret skal det avslås opptjening dersom omsorgsyter har ytt mindre enn et halvt år omsorg - delt omsorg`() {
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
                                    tom = mai(2020),
                                    omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                ),
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().first().single().also { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "07081812345"
            )
            assertInstanceOf(OmsorgsopptjeningGrunnlag.IkkeFødtIOmsorgsår::class.java, behandling.grunnlag)
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering>()
                .also { vurdering ->
                    assertInstanceOf(
                        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag::class.java,
                        vurdering.grunnlag
                    ).also {
                        assertEquals(
                            Periode(januar(2020), mai(2020)).alleMåneder(),
                            it.omsorgsmåneder().alle()
                        )
                        assertEquals(
                            AntallMånederRegel.FødtUtenforOmsorgsår,
                            it.antallMånederRegel,
                        )
                    }
                }
            assertEquals(
                listOf(
                    behandling.vilkårsvurdering.finnVurdering<OmsorgsyterMottarBarnetrgyd.Vurdering>(),
                    behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering>(),
                    behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarGyldigOmsorgsarbeid.Vurdering>(),
                ), behandling.vilkårsvurdering.finnAlleAvslatte()
            )
        }
    }

    @Test
    fun `gitt at flere omsorgytere har ytt omsorg for samme barn, innvilges opptjening til omsorgsyter med flest måneder med full, selv om andre har like mange med delt`() {
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
                                    tom = juni(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                ),
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "04010012797",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = juli(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 0,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                ),
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().first().single().also { behandling ->
            behandling.assertInnvilget(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "07081812345"
            )
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>()
                .let { vurdering ->
                    assertTrue(vurdering.grunnlag.omsorgsyterHarFlestOmsorgsmånederMedFullOmsorg())
                    assertEquals(
                        mapOf(
                            "12345678910" to 6,
                            "04010012797" to 0,
                        ),
                        vurdering.grunnlag.data.associate { it.omsorgsyter to it.antallFull() }
                    )
                }
        }
    }

    @Test
    fun `gitt at en omsorgyter har ytt omsorg for flere barn i det samme året, innvilges opptjening for det eldste barnet hvor alle kriterer er oppfylt`() {
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
                                    omsorgsmottaker = "01052012345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.JANUARY),
                                    tom = YearMonth.of(2021, Month.JANUARY),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01122012345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                ),
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().first().also { behandlinger ->
            assertEquals(3, behandlinger.antallBehandlinger())
            behandlinger.alle()[0].also {
                it.assertInnvilget(
                    omsorgsyter = "12345678910",
                    omsorgsmottaker = "07081812345"
                )
            }
            behandlinger.alle()[1].also {
                it.assertAvslag(
                    omsorgsyter = "12345678910",
                    omsorgsmottaker = "01052012345"
                )
                assertTrue(it.vilkårsvurdering.erEnesteAvslag<OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering>())
            }
            behandlinger.alle()[2].also {
                it.assertAvslag(
                    omsorgsyter = "12345678910",
                    omsorgsmottaker = "01122012345"
                )
                assertTrue(it.vilkårsvurdering.erEnesteAvslag<OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering>())
            }
        }
    }

    @Test
    fun `gitt et barn født i desember i opptjeningsåret og flere omsorgsyter har ytt omsorg for samme barn avslås opptjening dersom en annen omsorgsyter har flere måneder med omsorg i året etter opptjeningsåret`() {
        repo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.JANUARY),
                                    tom = YearMonth.of(2021, Month.FEBRUARY),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01122012345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "04010012797",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.JANUARY),
                                    tom = YearMonth.of(2021, Month.FEBRUARY),
                                    omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                    omsorgsmottaker = "01122012345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.MARCH),
                                    tom = YearMonth.of(2021, Month.MAY),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01122012345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                ),
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().first().single().also { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "01122012345"
            )
            assertTrue(behandling.vilkårsvurdering.erEnesteAvslag<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>())
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>()
                .let { vurdering ->
                    assertFalse(vurdering.grunnlag.omsorgsyterHarFlestOmsorgsmånederMedFullOmsorg())
                    assertFalse(vurdering.grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmånederMedFullOmsorg())
                    assertEquals(
                        mapOf(
                            "04010012797" to 3,
                            "12345678910" to 2,
                        ),
                        vurdering.grunnlag.data.associate { it.omsorgsyter to it.antallFull() }
                    )
                }
        }
    }

    @Test
    fun `gitt at en omsorgsyter har fått innvilget opptjening for en omsorgsmottaker, kan ikke andre omsorgstyere innvilges for den samme omsrogsmottakeren det samme året`() {
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
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                ),
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().first().single().assertInnvilget(
            omsorgsyter = "12345678910",
            omsorgsmottaker = "07081812345"
        )

        repo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "04010012797",
                    persongrunnlag = listOf(
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
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                ),
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().first().single().also { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "04010012797",
                omsorgsmottaker = "07081812345",
            )
            assertTrue(behandling.vilkårsvurdering.erEnesteAvslag<OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Vurdering>())
        }
    }

    @Test
    fun `gitt at en omsorgsyter er innvilget opptjening for en omsorgsmottaker, kan ikke den samme omsorgsyteren innvilges opptjening for andre omsorgsmottakere samme år`() {
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
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01052012345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                ),
            ),
        )


        persongrunnlagMeldingProcessingService.processAndExpectResult().first().also { behandlinger ->
            assertEquals(2, behandlinger.antallBehandlinger())
            behandlinger.alle().first().assertInnvilget(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "07081812345"
            )
            behandlinger.alle().last().assertAvslag(
                "12345678910",
                "01052012345"
            )
            assertTrue(
                behandlinger.alle()
                    .last().vilkårsvurdering.erEnesteAvslag<OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering>()
            )
        }
    }

    @Test
    fun `gitt at flere omsorgsytere har ytt omsorg for samme omsorgsmottaker i like mange måneder i opptjeningsåret skal behandles manuelt - omsorgsmottaker født uten for opptjeningsår`() {
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
                                    tom = juli(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "04010012797",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = juni(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "01019212345",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2020),
                                    tom = juli(2020),
                                    omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                ),
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().first().single().let { behandling ->
            behandling.assertManuell(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "07081812345"
            )
            assertTrue(behandling.vilkårsvurdering.erEnesteUbestemt<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>())
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>()
                .let { vurdering ->
                    assertFalse(vurdering.grunnlag.omsorgsyterHarFlestOmsorgsmånederMedFullOmsorg())
                    kotlin.test.assertTrue(vurdering.grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmånederMedFullOmsorg())
                    assertEquals(
                        mapOf(
                            "12345678910" to 7,
                            "04010012797" to 7,
                            "01019212345" to 0,
                        ),
                        vurdering.grunnlag.data.associate { it.omsorgsyter to it.antallFull() }
                    )
                }
            assertEquals(
                listOf(
                    Oppgaveopplysninger.Ingen
                ),
                behandling.hentOppgaveopplysninger()
            )
        }
    }

    @Test
    fun `gitt at omsorgsyter har en EØS-sak og mottar pensjon og eller uføretrygd fra PEN, skal saken behandles manuelt`() {
        wiremock.løpendeAlderspensjon(
            fnr = "12345678910",
            Periode(juli(2020), desember(2020))
        )
        wiremock.løpendeUføretrygd(
            fnr = "12345678910",
            Periode(januar(2020), juni(2020))
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
                                    landstilknytning = KafkaLandstilknytning.EØS_UKJENT_PRIMÆR_OG_SEKUNDÆR_LAND
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                ),
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().first().single().let { behandling ->
            behandling.assertManuell(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "07081812345"
            )
            assertTrue(behandling.vilkårsvurdering.erEnesteUbestemt<OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.Vurdering>())
            assertEquals(
                listOf(
                    Oppgaveopplysninger.Generell(
                        oppgavemottaker = "12345678910",
                        oppgaveTekst = "Vurder omsorgsopptjening manuelt. Bruker var bosatt i EØS-land i perioden hen mottok barnetrygd for barn med fnr: 07081812345."
                    )
                ),
                behandling.hentOppgaveopplysninger()
            )
        }
    }

    @Test
    fun `gitt flere omsorgsytere men ingen har tilstrekkelig antall måneder full barnetrygd skal behandles manuelt for den med flest måneder totalt`() {
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
                                    tom = mai(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = juni(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "01019212345",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = juni(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                ),
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().first().single().let { behandling ->
            behandling.assertManuell(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "07081812345"
            )
            assertTrue(behandling.vilkårsvurdering.erEnesteUbestemt<OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering>())
            assertTrue(behandling.vilkårsvurdering.erInnvilget<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>())
            assertEquals(
                listOf(
                    Oppgaveopplysninger.Generell(
                        oppgavemottaker = "12345678910",
                        oppgaveTekst = Oppgave.kombinasjonAvFullOgDeltErTilstrekkelig("07081812345")
                    )
                ),
                behandling.hentOppgaveopplysninger()
            )
        }
    }

    @Test
    fun `gitt at flere omsorgsytere har ytt omsorg for samme omsorgsmottaker i like mange måneder i opptjeningsåret skal behandles manuelt - omsorgsmottager født i desember i opptjeningsåret`() {
        repo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.JANUARY),
                                    tom = YearMonth.of(2021, Month.JUNE),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01122012345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "04010012797",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.JULY),
                                    tom = YearMonth.of(2021, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01122012345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                ),
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                ),
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().first().single().let { behandling ->
            behandling.assertManuell(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "01122012345"
            )
            assertTrue(behandling.vilkårsvurdering.erEnesteUbestemt<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>())
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>()
                .let { vurdering ->
                    assertFalse(vurdering.grunnlag.omsorgsyterHarFlestOmsorgsmånederMedFullOmsorg())
                    assertTrue(vurdering.grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmånederMedFullOmsorg())
                    assertEquals(
                        mapOf(
                            "12345678910" to 6,
                            "04010012797" to 6,
                        ),
                        vurdering.grunnlag.data.associate { it.omsorgsyter to it.antallFull() }
                    )
                }
            assertEquals(
                listOf(
                    Oppgaveopplysninger.Generell(
                        oppgavemottaker = "12345678910",
                        oppgaveTekst = """Godskr. omsorgspoeng, flere mottakere: Flere personer som har mottatt barnetrygd samme år for barnet med fnr 01122012345 i barnets fødselsår. Vurder hvem som skal ha omsorgspoengene."""
                    )
                ),
                behandling.hentOppgaveopplysninger()
            )
        }
    }

    @Test
    fun `gitt en omsorgsmottaker som har blitt innvilget for en omsorgsyter i et år, kan innvilges for en annen omsorgsyter et annet år`() {
        willAnswer { true }.given(gyldigOpptjeningår).erGyldig(2020)
        willAnswer { true }.given(gyldigOpptjeningår).erGyldig(2021)

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
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                ),
            ),
        )


        persongrunnlagMeldingProcessingService.processAndExpectResult().first().single().assertInnvilget(
            omsorgsyter = "12345678910",
            omsorgsmottaker = "07081812345",
            år = OPPTJENINGSÅR,
        )

        repo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "04010012797",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "04010012797",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.JANUARY),
                                    tom = YearMonth.of(2021, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                ),
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().first().single().assertInnvilget(
            omsorgsyter = "04010012797",
            omsorgsmottaker = "07081812345",
            år = 2021,
        )
    }

    @Test
    fun `en omsorgsyter som mottar barnetrygd for en omsorgsmottaker over 6 år med hjelpestønad skal innvilges omsorgsopptjening dersom barnetrygd og hjelpestønad har et halvt år eller mer overlappende måneder`() {
        wiremock.givenThat(
            post(WireMock.urlPathEqualTo("$POPP_PENSJONSPOENG_PATH/hent"))
                .withRequestBody(
                    equalToJson(
                        """
                            {
                                "fnr" : "12345678910",
                                "fomAr": 2019
                            }
                        """.trimIndent(), false, true
                    )
                )
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "pensjonspoeng": [
                                    {
                                        "ar":2019,
                                        "poeng":1.5,
                                        "pensjonspoengType":"OBO6H"
                                    }
                                ]
                            }
                        """.trimIndent()
                        )
                )
        )

        wiremock.givenThat(
            post(WireMock.urlPathEqualTo("$POPP_PENSJONSPOENG_PATH/hent"))
                .withRequestBody(
                    equalToJson(
                        """
                    {
                        "fnr" : "12345678910",
                        "fomAr" : 2020
                    }
                """, false, true
                    )
                )
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "pensjonspoeng": [
                                    {
                                        "ar":2020,
                                        "poeng":1.5,
                                        "pensjonspoengType":"OBO6H"
                                    }
                                ]
                            }
                        """.trimIndent()
                        )
                )
        )

        wiremock.givenThat(
            post(WireMock.urlPathEqualTo("$POPP_PENSJONSPOENG_PATH/hent"))
                .withRequestBody(
                    equalToJson(
                        """
                    {
                        "fnr" : "04010012797"
                    }
                """.trimIndent(), false, true
                    )
                )
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "pensjonspoeng":null
                            }
                        """.trimIndent()
                        )
                )
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
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                ),
                            ),
                            hjelpestønadsperioder = listOf(
                                PersongrunnlagMeldingKafka.Hjelpestønadperiode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_3,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.INFOTRYGD,
                                )
                            )
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                ),
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().first().single().also { behandling ->
            behandling.assertInnvilget(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "03041212345",
                omsorgstype = DomainOmsorgskategori.HJELPESTØNAD,
            )
        }
    }

    @Test
    @Disabled("Ikke et kriterium i BPEN030 - se OmsorgsyterErForelderTilMottakerAvHjelpestønad og relatert bruk")
    fun `en omsorgsyter som mottar barnetrygd for en omsorgsmottaker over 6 år med hjelpestønad skal avslås omsorgsyter og omsorgsmottaker ikke har en foreldre-barn relasjon`() {
        repo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "01019212345",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "01019212345",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                ),
                            ),
                            hjelpestønadsperioder = listOf(
                                PersongrunnlagMeldingKafka.Hjelpestønadperiode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_3,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.INFOTRYGD,
                                )
                            )
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                ),
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().first().single().also { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "01019212345",
                omsorgsmottaker = "03041212345",
                omsorgstype = DomainOmsorgskategori.HJELPESTØNAD,
            ).also {
                assertTrue(it.vilkårsvurdering.erEnesteAvslag<OmsorgsyterErForelderTilMottakerAvHjelpestønad.Vurdering>())
            }
        }
    }

    @Test
    fun `en omsorgsyter som mottar barnetrygd for en omsorgsmottaker med hjelpestønad skal avslås dersom barnetrygd og hjelpestønad har et halvt år eller mindre overlappende måneder`() {
        repo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = mai(2020),
                                    tom = juni(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                ),
                            ),
                            hjelpestønadsperioder = listOf(
                                PersongrunnlagMeldingKafka.Hjelpestønadperiode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_3,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.INFOTRYGD,
                                )
                            )
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                ),
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().first().single().also { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "03041212345",
                omsorgstype = DomainOmsorgskategori.HJELPESTØNAD,
            )
        }
    }

    @Test
    fun `en omsorgsyter som ikke mottar barnetrygd for en omsorgsmottaker med hjelpestønad skal avslås`() {
        repo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.USIKKER_BARNETRYGD,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 0,
                                    landstilknytning = Landstilknytning.NORGE,
                                )
                            ),
                            hjelpestønadsperioder = listOf(
                                PersongrunnlagMeldingKafka.Hjelpestønadperiode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_3,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.INFOTRYGD,
                                )
                            )
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                ),
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().first().single().also { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "03041212345",
                omsorgstype = DomainOmsorgskategori.HJELPESTØNAD,
            )
        }
    }

    @Test
    fun `en omsorgsyter som mottar barnetrygd for en omsorgsmottaker under 6 år med hjelpestønad skal innvilges omsorgsopptjening for barnetrygd`() {
        repo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                ),
                            ),
                            hjelpestønadsperioder = listOf(
                                PersongrunnlagMeldingKafka.Hjelpestønadperiode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_3,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.INFOTRYGD,
                                )
                            )
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                ),
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().first().single().also { behandling ->
            behandling.assertInnvilget(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "07081812345",
                omsorgstype = DomainOmsorgskategori.BARNETRYGD,
            )
        }
    }

    @Test
    fun `en omsorgsyter som ikke får utbetalt barnetryd skal avslås`() {
        repo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01052012345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 0,
                                    landstilknytning = KafkaLandstilknytning.NORGE,
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                ),
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().first().single().also { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "01052012345",
                omsorgstype = DomainOmsorgskategori.BARNETRYGD,
            )
            assertEquals(
                listOf(
                    behandling.vilkårsvurdering.finnVurdering<OmsorgsyterMottarBarnetrgyd.Vurdering>(),
                    behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarGyldigOmsorgsarbeid.Vurdering>(),
                ), behandling.vilkårsvurdering.finnAlleAvslatte()
            )
        }
    }

    @Test
    fun `en omsorgsyter som ikke får utbetalt barnetryd skal innvilges dersom Norge er sekundærland`() {
        repo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01052012345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 0,
                                    landstilknytning = KafkaLandstilknytning.EØS_NORGE_SEKUNDÆR,
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                ),
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().first().single().also { behandling ->
            behandling.assertInnvilget(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "01052012345",
                omsorgstype = DomainOmsorgskategori.BARNETRYGD,
            )
        }
    }

    @Test
    fun `en omsorgsyter ikke tilstrekkelig overlapp mellom utbetaling og omsorg (ikke gyldig) skal avslås`() {
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
                                    tom = mai(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 0,
                                    landstilknytning = KafkaLandstilknytning.NORGE,
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = juni(2020),
                                    tom = juli(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 1000,
                                    landstilknytning = KafkaLandstilknytning.NORGE,
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = august(2020),
                                    tom = august(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 0,
                                    landstilknytning = KafkaLandstilknytning.EØS_NORGE_SEKUNDÆR,
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = september(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 0,
                                    landstilknytning = KafkaLandstilknytning.NORGE,
                                ),
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                ),
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().first().single().also { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "07081812345",
                omsorgstype = DomainOmsorgskategori.BARNETRYGD,
            )
            assertEquals(
                listOf(
                    behandling.vilkårsvurdering.finnVurdering<OmsorgsyterMottarBarnetrgyd.Vurdering>(),
                    behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarGyldigOmsorgsarbeid.Vurdering>(),
                ), behandling.vilkårsvurdering.finnAlleAvslatte()
            )
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarGyldigOmsorgsarbeid.Vurdering>().also { vurdering ->
                assertEquals(6, vurdering.grunnlag.antallMånederRegel.antall)
                assertEquals(år(2020).alleMåneder(), vurdering.grunnlag.omsorgsmåneder().alle())
                assertEquals(
                    setOf(
                        juni(2020),
                        juli(2020),
                        august(2020),
                    ), vurdering.grunnlag.omsorgsytersUtbetalingsmåneder.alle()
                )
            }
        }
    }

    @Test
    fun `utdaterte identer oppdateres med gjeldende ident fra PDL`() {
        val omsorgsyterGammeltFnr = "61018212345"
        val omsorgsyterNyttFnr = "01018212345"
        val omsorgsmottakerGammeltFnr = "67081812345"
        val omsorgsmottakerNyttFnr = "07081812345"

        wiremock.givenThat(
            post(urlEqualTo(PDL_PATH))
                .withRequestBody(containing(omsorgsyterGammeltFnr))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("fnr_1bruk_pluss_historisk.json")
                )
        )

        wiremock.givenThat(
            post(urlEqualTo(PDL_PATH))
                .withRequestBody(containing(omsorgsmottakerGammeltFnr))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("fnr_barn_2ar_2020.json")
                )
        )

        repo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = omsorgsyterGammeltFnr,
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = omsorgsyterGammeltFnr,
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = omsorgsmottakerGammeltFnr,
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 5001,
                                    landstilknytning = KafkaLandstilknytning.NORGE,
                                ),
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                ),
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().first().single().also { behandling ->
            behandling.assertInnvilget(
                omsorgsyter = omsorgsyterNyttFnr,
                omsorgsmottaker = omsorgsmottakerNyttFnr
            )
            assertThat(behandling.omsorgsyter).isEqualTo(omsorgsyterNyttFnr)
            assertThat(behandling.omsorgsmottaker).isEqualTo(omsorgsmottakerNyttFnr)
            assertThat(behandling.grunnlag.omsorgsyter.fnr).isEqualTo(omsorgsyterNyttFnr)
            assertThat(behandling.grunnlag.omsorgsmottaker.fnr).isEqualTo(omsorgsmottakerNyttFnr)
            assertThat(behandling.grunnlag.grunnlag.persongrunnlag.map { it.omsorgsyter.fnr }.single()).isEqualTo(
                omsorgsyterNyttFnr
            )
            assertThat(behandling.grunnlag.grunnlag.persongrunnlag.flatMap { it.omsorgsmottakere().map { it.fnr } }
                           .single()).isEqualTo(
                omsorgsmottakerNyttFnr
            )
        }
    }

    @Test
    fun `ferdigstiller meldinger uten noe nyttig innhold uten å gjøre behandling`() {
        val melding = repo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = emptyList(),
                            hjelpestønadsperioder = emptyList(),
                        ),
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "04010012797",
                            omsorgsperioder = emptyList(),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                ),
            ),
        )

        assertThat(persongrunnlagMeldingProcessingService.processAndExpectResult()).isEqualTo(
            listOf(
                FullførteBehandlinger(emptyList())
            )
        )
        assertThat(repo.find(melding!!).status).isInstanceOf(PersongrunnlagMelding.Status.Ferdig::class.java)
    }

    @Test
    fun `omsorgsyter som ikke er medlem i folketrygden gir avslag`() {
        val fom = YearMonth.of(2018, Month.SEPTEMBER)
        val tom = YearMonth.of(2025, Month.DECEMBER)

        wiremock.unntaksperioderUtenMedlemskap(
            fnr = "12345678910",
            perioder = setOf(Periode(fom, tom))
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
                                    fom = fom,
                                    tom = tom,
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                ),
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().first().single().also { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "07081812345"
            )
            assertTrue(behandling.vilkårsvurdering.erEnesteAvslag<OmsorgsyterErMedlemIFolketrygden.Vurdering>())
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterErMedlemIFolketrygden.Vurdering>()
                .also { vurdering ->
                    assertInstanceOf(VilkårsvurderingUtfall.Avslag::class.java, vurdering.utfall)
                    assertInstanceOf(
                        OmsorgsyterErMedlemIFolketrygden.Grunnlag::class.java, vurdering.grunnlag
                    ).also {
                        assertThat(it.ikkeMedlem).isNotEmpty()
                    }
                }
        }
    }

    @Test
    fun `omsorgsyter med omsorg for seg selv får avslag - feks enslig mindreårige som mottar barnetrygd og hjelpestønad for seg selv`() {
        wiremock.ingenUnntaksperioderForMedlemskap()

        repo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12340378910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12340378910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "12340378910",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                ),
                            ),
                            hjelpestønadsperioder = listOf(
                                PersongrunnlagMeldingKafka.Hjelpestønadperiode(
                                    fom = januar(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_3,
                                    omsorgsmottaker = "12340378910",
                                    kilde = Kilde.INFOTRYGD,
                                ),
                            ),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                ),
            ),
        )

        persongrunnlagMeldingProcessingService.processAndExpectResult().first().single().also { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "12340378910",
                omsorgsmottaker = "12340378910",
                omsorgstype = DomainOmsorgskategori.HJELPESTØNAD,
            )
            assertTrue(behandling.vilkårsvurdering.erEnesteAvslag<OmsorgsyterErikkeOmsorgsmottaker.Vurdering>())
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterErikkeOmsorgsmottaker.Vurdering>()
                .also { vurdering ->
                    assertInstanceOf(VilkårsvurderingUtfall.Avslag::class.java, vurdering.utfall)
                    assertInstanceOf(OmsorgsyterErikkeOmsorgsmottaker.Grunnlag::class.java, vurdering.grunnlag).also {
                        assertThat(it.omsorgsyter).isEqualTo("12340378910")
                        assertThat(it.omsorgsmottaker).isEqualTo("12340378910")
                    }
                }
        }
    }

    private fun FullførtBehandling.assertInnvilget(
        omsorgsyter: String,
        omsorgsmottaker: String,
        år: Int = OPPTJENINGSÅR,
        omsorgstype: DomainOmsorgskategori = DomainOmsorgskategori.BARNETRYGD,
    ): FullførtBehandling {
        assertEquals(år, this.omsorgsAr)
        assertEquals(omsorgsyter, this.omsorgsyter)
        assertEquals(omsorgsmottaker, this.omsorgsmottaker)
        assertEquals(omsorgstype, this.omsorgstype)
        assertInstanceOf(BehandlingUtfall.Innvilget::class.java, this.utfall)
        return this
    }

    private fun FullførtBehandling.assertAvslag(
        omsorgsyter: String,
        omsorgsmottaker: String,
        omsorgstype: DomainOmsorgskategori = DomainOmsorgskategori.BARNETRYGD,
    ): FullførtBehandling {
        assertEquals(OPPTJENINGSÅR, this.omsorgsAr)
        assertEquals(omsorgsyter, this.omsorgsyter)
        assertEquals(omsorgsmottaker, this.omsorgsmottaker)
        assertEquals(omsorgstype, this.omsorgstype)
        assertInstanceOf(BehandlingUtfall.Avslag::class.java, this.utfall)
        return this
    }

    private fun FullførtBehandling.assertManuell(
        omsorgsyter: String,
        omsorgsmottaker: String,
        omsorgstype: DomainOmsorgskategori = DomainOmsorgskategori.BARNETRYGD,
    ): FullførtBehandling {
        assertEquals(OPPTJENINGSÅR, this.omsorgsAr)
        assertEquals(omsorgsyter, this.omsorgsyter)
        assertEquals(omsorgsmottaker, this.omsorgsmottaker)
        assertEquals(omsorgstype, this.omsorgstype)
        assertInstanceOf(BehandlingUtfall.Manuell::class.java, this.utfall)
        return this
    }
}