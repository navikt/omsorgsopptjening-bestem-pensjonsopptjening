package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model


import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubForPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.wiremockWithPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.AntallMånederRegel
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.BehandlingUtfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførtBehandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførteBehandlinger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterErForelderTilMottakerAvHjelpestønad
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarGyldigOmsorgsarbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarTilstrekkeligOmsorgsarbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterMottarBarnetrgyd
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Oppgaveopplysninger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.erEnesteAvslag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.finnAlleAvslatte
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.finnVurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
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
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.Month
import java.time.YearMonth
import kotlin.test.Test
import kotlin.test.assertFalse
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Landstilknytning as KafkaLandstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding as PersongrunnlagMeldingKafka

class PersongrunnlagMeldingServiceTest : SpringContextTest.NoKafka() {
    @Autowired
    private lateinit var repo: PersongrunnlagRepo

    @Autowired
    private lateinit var handler: PersongrunnlagMeldingService

    @MockBean
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
                )
            ),
        )

        handler.process()!!.first().single().also { behandling ->
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
                            it.omsorgsytersOmsorgsmånederForOmsorgsmottaker.alle(),
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
                )
            ),
        )
        assertEquals(0, handler.process()!!.first().antallBehandlinger())
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
                                    fom = YearMonth.of(2020, Month.OCTOBER),
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
                )
            ),
        )


        handler.process()!!.first().single().also { behandling ->
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
                                YearMonth.of(2020, Month.OCTOBER),
                                YearMonth.of(2020, Month.DECEMBER)
                            ).alleMåneder(),
                            it.omsorgsytersOmsorgsmånederForOmsorgsmottaker.alle()
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
                                    fom = YearMonth.of(2020, Month.MAY),
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
                )
            ),
        )


        handler.process()!!.first().single().also { behandling ->
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
                                YearMonth.of(2020, Month.MAY),
                                YearMonth.of(2020, Month.DECEMBER)
                            ).alleMåneder(),
                            it.omsorgsytersOmsorgsmånederForOmsorgsmottaker.alle()
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
                )
            ),
        )

        handler.process()!!.first().single().also { behandling ->
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
                            it.omsorgsytersOmsorgsmånederForOmsorgsmottaker.alle()
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
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.MAY),
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
                )
            ),
        )

        handler.process()!!.first().single().also { behandling ->
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
                            Periode(YearMonth.of(2020, Month.JANUARY), YearMonth.of(2020, Month.MAY)).alleMåneder(),
                            it.omsorgsytersOmsorgsmånederForOmsorgsmottaker.alle()
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
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.MAY),
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
                )
            ),
        )

        handler.process()!!.first().single().also { behandling ->
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
                            Periode(YearMonth.of(2020, Month.JANUARY), YearMonth.of(2020, Month.MAY)).alleMåneder(),
                            it.omsorgsytersOmsorgsmånederForOmsorgsmottaker.alle()
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
    fun `gitt at flere omsorgytere har ytt omsorg for samme barn, innvilges opptjening til omsorgsyter med flest gyldige måneder omsorg i opptjeningsåret - innvilget for omsorgsyter under behandling`() {
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
                                    tom = YearMonth.of(2020, Month.MAY),
                                    omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JUNE),
                                    tom = YearMonth.of(2020, Month.AUGUST),
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
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.MAY),
                                    omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.SEPTEMBER),
                                    tom = YearMonth.of(2020, Month.OCTOBER),
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
                                    fom = YearMonth.of(2020, Month.NOVEMBER),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
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
                )
            ),
        )

        handler.process()!!.first().single().also { behandling ->
            behandling.assertInnvilget(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "07081812345"
            )
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>()
                .let { vurdering ->
                    assertTrue(vurdering.grunnlag.omsorgsyterHarFlestOmsorgsmåneder())
                    assertFalse(vurdering.grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmåneder())
                    assertEquals(
                        mapOf(
                            "12345678910" to 8,
                            "04010012797" to 7,
                            "01019212345" to 2,
                        ),
                        vurdering.grunnlag.data.associate { it.omsorgsyter to it.antall() }
                    )
                }
        }
    }

    @Test
    fun `gitt at flere omsorgytere har ytt omsorg for samme barn, innvilges opptjening til omsorgsyter med flest gyldige måneder, selv om andre har flere måneder med ugyldig barnetrygd`() {
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
                                    omsorgstype = Omsorgstype.DELT_BARNETRYGD,
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
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.MAY),
                                    omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JUNE),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
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
                )
            ),
        )

        handler.process()!!.first().single().also { behandling ->
            behandling.assertInnvilget(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "07081812345"
            )
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>()
                .let { vurdering ->
                    assertTrue(vurdering.grunnlag.omsorgsyterHarFlestOmsorgsmåneder())
                    assertFalse(vurdering.grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmåneder())
                    assertEquals(
                        mapOf(
                            "12345678910" to 6,
                            "04010012797" to 5,
                        ),
                        vurdering.grunnlag.data.associate { it.omsorgsyter to it.antall() }
                    )
                }
        }
    }

    @Test
    fun `gitt at flere omsorgytere har ytt omsorg for samme barn, innvilges opptjening til omsorgsyter med flest måneder omsorg i opptjeningsåret - avslag for omsorgsyter under behandling`() {
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
                                    tom = YearMonth.of(2020, Month.MAY),
                                    omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.SEPTEMBER),
                                    tom = YearMonth.of(2020, Month.OCTOBER),
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
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.MAY),
                                    omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JUNE),
                                    tom = YearMonth.of(2020, Month.AUGUST),
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
                                    fom = YearMonth.of(2020, Month.NOVEMBER),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
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
                )
            ),
        )

        handler.process()!!.first().single().also { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "07081812345"
            )
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>()
                .let { vurdering ->
                    assertFalse(vurdering.grunnlag.omsorgsyterHarFlestOmsorgsmåneder())
                    assertFalse(vurdering.grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmåneder())
                    assertEquals(
                        mapOf(
                            "04010012797" to 8,
                            "12345678910" to 7,
                            "01019212345" to 2,
                        ),
                        vurdering.grunnlag.data.associate { it.omsorgsyter to it.antall() }
                    )
                    assertTrue(vurdering.erEnesteAvslag<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>())
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
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01052012345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
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
                )
            ),
        )

        handler.process()!!.first().also { behandlinger ->
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
                                    omsorgstype = Omsorgstype.DELT_BARNETRYGD,
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
                )
            ),
        )

        handler.process()!!.first().single().also { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "01122012345"
            )
            assertTrue(behandling.vilkårsvurdering.erEnesteAvslag<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>())
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>()
                .let { vurdering ->
                    assertFalse(vurdering.grunnlag.omsorgsyterHarFlestOmsorgsmåneder())
                    assertFalse(vurdering.grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmåneder())
                    assertEquals(
                        mapOf(
                            "04010012797" to 5,
                            "12345678910" to 2,
                        ),
                        vurdering.grunnlag.data.associate { it.omsorgsyter to it.antall() }
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
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
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
                )
            ),
        )

        handler.process()!!.first().single().assertInnvilget(
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
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
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
                )
            ),
        )

        handler.process()!!.first().single().also { behandling ->
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
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = KafkaLandstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
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
                )
            ),
        )


        handler.process()!!.first().also { behandlinger ->
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
    fun `gitt at flere omsorgsytere har ytt omsorg for samme omsorgsmottaker i like mange måneder i opptjeningsåret skal opptjening avslås - omsorgsmottaker født uten for opptjeningsår`() {
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
                                    tom = YearMonth.of(2020, Month.JULY),
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
                                    fom = YearMonth.of(2020, Month.JUNE),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
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
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.JULY),
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
                )
            ),
        )

        handler.process()!!.first().single().let { behandling ->
            behandling.assertManuell(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "07081812345"
            )
            assertTrue(behandling.vilkårsvurdering.erEnesteAvslag<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>())
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>()
                .let { vurdering ->
                    assertFalse(vurdering.grunnlag.omsorgsyterHarFlestOmsorgsmåneder())
                    kotlin.test.assertTrue(vurdering.grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmåneder())
                    assertEquals(
                        mapOf(
                            "12345678910" to 7,
                            "04010012797" to 7,
                            "01019212345" to 7,
                        ),
                        vurdering.grunnlag.data.associate { it.omsorgsyter to it.antall() }
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
    fun `gitt at flere omsorgsytere har ytt omsorg for samme omsorgsmottaker i like mange måneder i opptjeningsåret skal opptjening avslås - omsorgsmottager født i desember i opptjeningsåret`() {
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
                )
            ),
        )

        handler.process()!!.first().single().let { behandling ->
            behandling.assertManuell(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "01122012345"
            )
            assertTrue(behandling.vilkårsvurdering.erEnesteAvslag<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>())
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>()
                .let { vurdering ->
                    assertFalse(vurdering.grunnlag.omsorgsyterHarFlestOmsorgsmåneder())
                    assertTrue(vurdering.grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmåneder())
                    assertEquals(
                        mapOf(
                            "12345678910" to 6,
                            "04010012797" to 6,
                        ),
                        vurdering.grunnlag.data.associate { it.omsorgsyter to it.antall() }
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
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
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
                )
            ),
        )


        handler.process()!!.first().single().assertInnvilget(
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
                )
            ),
        )

        handler.process()!!.first().single().assertInnvilget(
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
                )
            ),
        )

        handler.process()!!.first().single().also { behandling ->
            behandling.assertInnvilget(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "03041212345",
                omsorgstype = DomainOmsorgstype.HJELPESTØNAD,
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
                )
            ),
        )

        handler.process()!!.first().single().also { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "01019212345",
                omsorgsmottaker = "03041212345",
                omsorgstype = DomainOmsorgstype.HJELPESTØNAD,
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
                                    fom = YearMonth.of(2020, Month.MAY),
                                    tom = YearMonth.of(2020, Month.JUNE),
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
                )
            ),
        )

        handler.process()!!.first().single().also { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "03041212345",
                omsorgstype = DomainOmsorgstype.HJELPESTØNAD,
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
                )
            ),
        )

        handler.process()!!.first().single().also { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "03041212345",
                omsorgstype = DomainOmsorgstype.HJELPESTØNAD,
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
                )
            ),
        )

        handler.process()!!.first().single().also { behandling ->
            behandling.assertInnvilget(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "07081812345",
                omsorgstype = DomainOmsorgstype.BARNETRYGD,
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
                )
            ),
        )

        handler.process()!!.first().single().also { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "01052012345",
                omsorgstype = DomainOmsorgstype.BARNETRYGD,
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
                )
            ),
        )

        handler.process()!!.first().single().also { behandling ->
            behandling.assertInnvilget(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "01052012345",
                omsorgstype = DomainOmsorgstype.BARNETRYGD,
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
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.MAY),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 0,
                                    landstilknytning = KafkaLandstilknytning.NORGE,
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JUNE),
                                    tom = YearMonth.of(2020, Month.JULY),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 1000,
                                    landstilknytning = KafkaLandstilknytning.NORGE,
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.AUGUST),
                                    tom = YearMonth.of(2020, Month.AUGUST),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 0,
                                    landstilknytning = KafkaLandstilknytning.EØS_NORGE_SEKUNDÆR,
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.SEPTEMBER),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
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
                )
            ),
        )

        handler.process()!!.first().single().also { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "07081812345",
                omsorgstype = DomainOmsorgstype.BARNETRYGD,
            )
            assertEquals(
                listOf(
                    behandling.vilkårsvurdering.finnVurdering<OmsorgsyterMottarBarnetrgyd.Vurdering>(),
                    behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarGyldigOmsorgsarbeid.Vurdering>(),
                ), behandling.vilkårsvurdering.finnAlleAvslatte()
            )
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarGyldigOmsorgsarbeid.Vurdering>().also { vurdering ->
                assertEquals(6, vurdering.grunnlag.antallMånederRegel.antall)
                assertEquals(år(2020).alleMåneder(), vurdering.grunnlag.omsorgsytersOmsorgsmåneder.alle())
                assertEquals(
                    setOf(
                        YearMonth.of(2020, Month.JUNE),
                        YearMonth.of(2020, Month.JULY),
                        YearMonth.of(2020, Month.AUGUST),
                    ), vurdering.grunnlag.omsorgsytersUtbetalingsmåneder.alle()
                )
                assertEquals(
                    setOf(
                        YearMonth.of(2020, Month.JUNE),
                        YearMonth.of(2020, Month.JULY),
                        YearMonth.of(2020, Month.AUGUST),
                    ), vurdering.grunnlag.gyldigeOmsorgsmåneder.alleMåneder()
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
            WireMock.post(WireMock.urlEqualTo(PDL_PATH))
                .withRequestBody(containing(omsorgsyterGammeltFnr))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("fnr_1bruk_pluss_historisk.json")
                )
        )

        wiremock.givenThat(
            WireMock.post(WireMock.urlEqualTo(PDL_PATH))
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
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
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
                )
            ),
        )

        handler.process()!!.first().single().also { behandling ->
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
                )
            ),
        )

        assertThat(handler.process()).isEqualTo(listOf(FullførteBehandlinger(emptyList())))
        assertThat(repo.find(melding!!).status).isInstanceOf(PersongrunnlagMelding.Status.Ferdig::class.java)
    }


    @Test
    fun `Flere personnummer tilhørende samme person skal behandles som ett persongrunnlag`() {
        val omsorgsyterGammeltFnr = "61018212345"
        val omsorgsyterNyttFnr = "01018212345"
        val omsorgsmottakerFnr = "07081812345"

        wiremock.givenThat(
            WireMock.post(WireMock.urlEqualTo(PDL_PATH))
                .withRequestBody(containing(omsorgsyterGammeltFnr))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("fnr_1bruk_pluss_historisk.json")
                )
        )

        wiremock.givenThat(
            WireMock.post(WireMock.urlEqualTo(PDL_PATH))
                .withRequestBody(containing(omsorgsmottakerFnr))
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
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.APRIL),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = omsorgsmottakerFnr,
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 5001,
                                    landstilknytning = KafkaLandstilknytning.NORGE,
                                ),
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = omsorgsyterNyttFnr,
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.MAY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = omsorgsmottakerFnr,
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
                )
            ),
        )

        handler.process()!!.first().single().also { behandling ->
            behandling.assertInnvilget(
                omsorgsyter = omsorgsyterNyttFnr,
                omsorgsmottaker = omsorgsmottakerFnr
            )
            assertThat(behandling.omsorgsyter).isEqualTo(omsorgsyterNyttFnr)
            assertThat(behandling.omsorgsmottaker).isEqualTo(omsorgsmottakerFnr)
            assertThat(behandling.grunnlag.omsorgsyter.fnr).isEqualTo(omsorgsyterNyttFnr)
            assertThat(behandling.grunnlag.omsorgsmottaker.fnr).isEqualTo(omsorgsmottakerFnr)
            assertThat(behandling.grunnlag.grunnlag.persongrunnlag.map { it.omsorgsyter.fnr }.single()).isEqualTo(
                omsorgsyterNyttFnr
            )
        }
    }

    @Test
    fun `Flere personnummer tilhørende samme person med overlappende perioder håndteres`() {
        val omsorgsyterGammeltFnr = "61018212345"
        val omsorgsyterNyttFnr = "01018212345"
        val omsorgsmottakerFnr = "07081812345"

        wiremock.givenThat(
            WireMock.post(WireMock.urlEqualTo(PDL_PATH))
                .withRequestBody(containing(omsorgsyterGammeltFnr))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("fnr_1bruk_pluss_historisk.json")
                )
        )

        wiremock.givenThat(
            WireMock.post(WireMock.urlEqualTo(PDL_PATH))
                .withRequestBody(containing(omsorgsmottakerFnr))
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
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = omsorgsmottakerFnr,
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 5001,
                                    landstilknytning = KafkaLandstilknytning.NORGE,
                                ),
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = omsorgsyterNyttFnr,
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = omsorgsmottakerFnr,
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
                )
            ),
        )

        handler.process()!!.first().single().also { behandling ->
            behandling.assertInnvilget(
                omsorgsyter = omsorgsyterNyttFnr,
                omsorgsmottaker = omsorgsmottakerFnr
            )
            assertThat(behandling.omsorgsyter).isEqualTo(omsorgsyterNyttFnr)
            assertThat(behandling.omsorgsmottaker).isEqualTo(omsorgsmottakerFnr)
            assertThat(behandling.grunnlag.omsorgsyter.fnr).isEqualTo(omsorgsyterNyttFnr)
            assertThat(behandling.grunnlag.omsorgsmottaker.fnr).isEqualTo(omsorgsmottakerFnr)
            assertThat(behandling.grunnlag.grunnlag.persongrunnlag.map { it.omsorgsyter.fnr }.single()).isEqualTo(
                omsorgsyterNyttFnr
            )
        }
    }

    private fun FullførtBehandling.assertInnvilget(
        omsorgsyter: String,
        omsorgsmottaker: String,
        år: Int = OPPTJENINGSÅR,
        omsorgstype: DomainOmsorgstype = DomainOmsorgstype.BARNETRYGD,
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
        omsorgstype: DomainOmsorgstype = DomainOmsorgstype.BARNETRYGD,
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
        omsorgstype: DomainOmsorgstype = DomainOmsorgstype.BARNETRYGD,
    ): FullførtBehandling {
        assertEquals(OPPTJENINGSÅR, this.omsorgsAr)
        assertEquals(omsorgsyter, this.omsorgsyter)
        assertEquals(omsorgsmottaker, this.omsorgsmottaker)
        assertEquals(omsorgstype, this.omsorgstype)
        assertInstanceOf(BehandlingUtfall.Manuell::class.java, this.utfall)
        return this
    }
}