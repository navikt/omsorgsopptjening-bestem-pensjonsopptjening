package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketDatagrunnlag

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketVedtaksperiode
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.Omsorgsmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.alleMåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
import java.time.Month

/**
 * Grunnlaget for vurdering av en [omsorgsyter]s rett til omsorgsopptjening basert på mottatt barnetrygd for
 * en gitt [omsorgsmottaker] i et gitt [omsorgsAr].
 */
sealed class OmsorgsopptjeningGrunnlag {

    abstract val omsorgsAr: Int
    abstract val omsorgsmottaker: Person
    abstract val grunnlag: BeriketDatagrunnlag

    val omsorgsyter: Person
        get() = grunnlag.omsorgsyter
    val innlesingId: InnlesingId
        get() = grunnlag.innlesingId
    val correlationId: CorrelationId
        get() = grunnlag.correlationId

    protected fun omsorgsytersOmsorgsmånederForOmsorgsmottaker(): Omsorgsmåneder {
        return omsorgsmånederForOmsorgsmottakerPerOmsorgsyter()[omsorgsyter]!!
    }

    private fun omsorgsmånederForOmsorgsmottakerPerOmsorgsyter(): Map<Person, Omsorgsmåneder> {
        return grunnlag.omsorgsSaker
            .associate { sak ->
                sak.omsorgsyter to sak.omsorgVedtakPerioder
                    .filter { it.omsorgsmottaker == omsorgsmottaker }
                    .let { vedtaksperioder ->
                        val barnetrygd = vedtaksperioder
                            .filter { it.omsorgstype == DomainOmsorgstype.BARNETRYGD }
                            .alleMåneder()

                        val hjelpestønad = vedtaksperioder
                            .filter { it.omsorgstype == DomainOmsorgstype.HJELPESTØNAD }
                            .alleMåneder()

                        if (forOmsorgsmottakerOgÅr().alderMottaker(Konstanter.ALDERSINTERVALL_BARNETRYGD)) {
                            Omsorgsmåneder.Barnetrygd(barnetrygd)
                        } else {
                            Omsorgsmåneder.Hjelpestønad(
                                måneder = barnetrygd.intersect(hjelpestønad),
                                barnetrygd = barnetrygd,
                                hjelpestønad = hjelpestønad
                            )
                        }
                    }
            }
    }

    fun forSummertOmsorgPerOmsorgsyter(): OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Grunnlag {
        return omsorgsmånederForOmsorgsmottakerPerOmsorgsyter().let {
            OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Grunnlag(
                omsorgsyter = omsorgsyter,
                data = it.map { (yter, antallMnd) ->
                    OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.OmsorgsmånederForMottakerOgÅr(
                        omsorgsyter = yter,
                        omsorgsmottaker = omsorgsmottaker,
                        omsorgsmåneder = antallMnd,
                        omsorgsår = omsorgsAr
                    )
                }
            )
        }
    }

    fun forOmsorgsyterOgÅr(): PersonOgOmsorgsårGrunnlag {
        return PersonOgOmsorgsårGrunnlag(
            person = omsorgsyter,
            omsorgsAr = omsorgsAr
        )
    }

    fun forOmsorgsmottakerOgÅr(): PersonOgOmsorgsårGrunnlag {
        return PersonOgOmsorgsårGrunnlag(
            person = omsorgsmottaker,
            omsorgsAr = omsorgsAr
        )
    }

    fun forFamilierelasjon(): OmsorgsyterErForelderTilMottakerAvHjelpestønad.Grunnlag {
        return OmsorgsyterErForelderTilMottakerAvHjelpestønad.Grunnlag(
            omsorgsyter = omsorgsyter,
            omsorgsmottaker = omsorgsmottaker
        )
    }

    abstract fun forTilstrekkeligOmsorgsarbeid(): OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag

    /**
     * Hvor mye omsorgsarbeid som kreves for å kunne motta omsorgsopptjening avhenger av når barnet er født på året,
     * samt hvilket [omsorgsAr] det vurderes omsorgsopptjening for.
     *
     * For spesialtilfellene hvor barn er [FødtIOmsorgsår.FødtDesember] vil det ikke eksistere utbetalinger av
     * barnetrygd for det aktuelle [omsorgsAr] i kildesystemet [DomainKilde.BARNETRYGD]. Vurderingen av disse gjøres
     * på bakgrunn av eventuell barnetrygd utbetalt i påfølgende år.
     *
     * @see Referanse.MåHaMinstHalveÅretMedOmsorgForBarnUnder6
     * @see Referanse.UnntakFraMinstHalvtÅrMedOmsorgForFødselår
     */
    sealed class FødtIOmsorgsår : OmsorgsopptjeningGrunnlag() {
        data class IkkeFødtDesember(
            override val omsorgsAr: Int,
            override val omsorgsmottaker: Person,
            override val grunnlag: BeriketDatagrunnlag
        ) : FødtIOmsorgsår() {
            init {
                require(
                    Periode(omsorgsAr).alleMåneder().containsAll(grunnlag.alleMåneder)
                ) { "Grunnlag contains months outside of the omsorgsår: $omsorgsAr" }
                require(
                    omsorgsmottaker.erFødt(omsorgsAr)
                ) { "$omsorgsmottaker er ikke født i $omsorgsAr" }
                require(
                    !omsorgsmottaker.erFødt(omsorgsAr, Month.DECEMBER)
                ) { "Forventer ikke grunnlag for fødselsåret dersom barn er født i desember" }
            }

            override fun forTilstrekkeligOmsorgsarbeid(): OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag {
                return OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtIOmsorgsår(
                    omsorgsAr = omsorgsAr,
                    omsorgsmottaker = omsorgsmottaker,
                    omsorgsmåneder = omsorgsytersOmsorgsmånederForOmsorgsmottaker()
                )
            }
        }

        data class FødtDesember(
            override val omsorgsAr: Int,
            override val omsorgsmottaker: Person,
            override val grunnlag: BeriketDatagrunnlag
        ) : FødtIOmsorgsår() {
            init {
                val årEtterOmsorgsår = omsorgsAr + 1
                require(
                    Periode(årEtterOmsorgsår).alleMåneder().containsAll(grunnlag.alleMåneder)
                ) { "Grunnlag should only contain months from: $årEtterOmsorgsår" }
                require(
                    omsorgsmottaker.erFødt(omsorgsAr)
                ) { "$omsorgsmottaker er ikke født i $omsorgsAr" }
                require(
                    omsorgsmottaker.erFødt(omsorgsAr, Month.DECEMBER)
                ) { "$omsorgsmottaker er ikke født i ${Month.DECEMBER} i $omsorgsAr" }
            }

            override fun forTilstrekkeligOmsorgsarbeid(): OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag {
                return OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtIDesemberOmsorgsår(
                    omsorgsAr = omsorgsAr,
                    omsorgsmottaker = omsorgsmottaker,
                    omsorgsmåneder = omsorgsytersOmsorgsmånederForOmsorgsmottaker()
                )
            }
        }
    }

    data class IkkeFødtIOmsorgsår(
        override val omsorgsAr: Int,
        override val omsorgsmottaker: Person,
        override val grunnlag: BeriketDatagrunnlag
    ) : OmsorgsopptjeningGrunnlag() {
        init {
            require(
                Periode(omsorgsAr).alleMåneder().containsAll(grunnlag.alleMåneder)
            ) { "Grunnlag contains months outside of the omsorgsår: $omsorgsAr" }
            require(
                !omsorgsmottaker.erFødt(omsorgsAr)
            ) { "$omsorgsmottaker er født i $omsorgsAr" }
        }

        override fun forTilstrekkeligOmsorgsarbeid(): OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag {
            return OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtUtenforOmsorgsår(
                omsorgsAr = omsorgsAr,
                omsorgsmottaker = omsorgsmottaker,
                omsorgsmåneder = omsorgsytersOmsorgsmånederForOmsorgsmottaker()
            )
        }
    }
}

fun BeriketDatagrunnlag.tilOmsorgsopptjeningsgrunnlag(): List<OmsorgsopptjeningGrunnlag> {
    return grunnlagPerMottakerPerÅr()
}

private fun BeriketDatagrunnlag.grunnlagPerMottakerPerÅr(): List<OmsorgsopptjeningGrunnlag> {
    return `opprett grunnlag per omsorgsmottaker per år`()
        .fold(emptyList()) { acc, (mottaker, år, grunnlag) ->
            acc + when (mottaker.erFødt(år)) {
                true -> {
                    listOf(
                        OmsorgsopptjeningGrunnlag.FødtIOmsorgsår.IkkeFødtDesember(
                            omsorgsAr = år,
                            omsorgsmottaker = mottaker,
                            grunnlag = grunnlag
                        )
                    )
                }

                false -> {
                    when (mottaker.erFødt(år - 1, Month.DECEMBER)) {
                        true -> {
                            listOf(
                                OmsorgsopptjeningGrunnlag.FødtIOmsorgsår.FødtDesember(
                                    omsorgsAr = år - 1,
                                    omsorgsmottaker = mottaker,
                                    grunnlag = grunnlag
                                ),
                                OmsorgsopptjeningGrunnlag.IkkeFødtIOmsorgsår(
                                    omsorgsAr = år,
                                    omsorgsmottaker = mottaker,
                                    grunnlag = grunnlag
                                ),
                            )
                        }

                        false -> {
                            listOf(
                                OmsorgsopptjeningGrunnlag.IkkeFødtIOmsorgsår(
                                    omsorgsAr = år,
                                    omsorgsmottaker = mottaker,
                                    grunnlag = grunnlag
                                )
                            )
                        }
                    }
                }
            }
        }
}

private fun BeriketDatagrunnlag.`opprett grunnlag per omsorgsmottaker per år`(): List<Triple<Person, Int, BeriketDatagrunnlag>> {
    return `avgrens grunnlagsdata per omsorgsmottaker`()
        .flatMap { (omsorgsmottaker, grunnlagPerMottaker) ->
            grunnlagPerMottaker.`avgrens for omsorgsår`()
                .map { (år, grunnlagPerÅrPerMottaker) ->
                    Triple(omsorgsmottaker, år, grunnlagPerÅrPerMottaker)
                }
        }
}

private fun BeriketDatagrunnlag.`avgrens grunnlagsdata per omsorgsmottaker`(): Map<Person, BeriketDatagrunnlag> {
    return omsorgsytersOmsorgsmottakere
        .sortedBy { it.fødselsdato } //eldste barn først
        .associateWith { omsorgsmottaker ->
            copy(omsorgsSaker = omsorgsSaker
                .map { sak -> sak.copy(omsorgVedtakPerioder = sak.omsorgVedtakPerioder.filter { it.omsorgsmottaker == omsorgsmottaker }) }
            )
        }
}

private fun BeriketDatagrunnlag.`avgrens for omsorgsår`(): Map<Int, BeriketDatagrunnlag> {
    return omsorgsytersOmsorgsår
        .associateWith { år ->
            copy(omsorgsSaker = omsorgsSaker
                .map { sak ->
                    sak.copy(omsorgVedtakPerioder = sak.omsorgVedtakPerioder
                        .filter { it.periode.overlapper(år) }
                        .map { barnetrygdPeriode ->
                            barnetrygdPeriode.periode.overlappendeMåneder(år)
                                .let {
                                    BeriketVedtaksperiode(
                                        fom = it.min(),
                                        tom = it.max(),
                                        omsorgstype = barnetrygdPeriode.omsorgstype,
                                        omsorgsmottaker = barnetrygdPeriode.omsorgsmottaker
                                    )
                                }
                        })
                })
        }
}
