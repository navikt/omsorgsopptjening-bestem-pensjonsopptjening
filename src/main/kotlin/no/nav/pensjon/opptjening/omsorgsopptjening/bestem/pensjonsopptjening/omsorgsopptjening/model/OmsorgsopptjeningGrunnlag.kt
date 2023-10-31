package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.BeriketDatagrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Hjelpestønadperiode.Companion.omsorgsmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Omsorgsperiode
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Omsorgsperiode.Companion.omsorgsmåneder
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

    val omsorgstype: DomainOmsorgstype by lazy {
        when (forAldersvurderingOmsorgsmottaker().erOppfylltFor(OmsorgsmottakerOppfyllerAlderskravForBarnetrygd.ALDERSINTERVALL_BARNETRYGD)) {
            true -> DomainOmsorgstype.BARNETRYGD
            false -> DomainOmsorgstype.HJELPESTØNAD
        }
    }

    protected fun omsorgsytersOmsorgsmånederForOmsorgsmottaker(): Omsorgsmåneder {
        return omsorgsmånederForOmsorgsmottakerPerOmsorgsyter()[omsorgsyter]!!
    }

    protected fun omsorgsytersMedlemskapsmåneder(): Medlemskapsmåneder {
        return grunnlag.omsorgsytersMedlemskapsmåneder
    }

    protected fun omsorgsytersUtbetalingsmåneder(): Utbetalingsmåneder {
        return grunnlag.omsorgsytersUtbetalingsmåneder
    }

    private fun omsorgsmånederForOmsorgsmottakerPerOmsorgsyter(): Map<Person, Omsorgsmåneder> {
        return grunnlag.persongrunnlag
            .associate { persongrunnlag ->
                Triple(persongrunnlag.omsorgsyter,
                       persongrunnlag.omsorgsperioder.filter { it.omsorgsmottaker == omsorgsmottaker },
                       persongrunnlag.hjelpestønadperioder.filter { it.omsorgsmottaker == omsorgsmottaker }
                ).let { (omsorgsyter, omsorgsperioder, hjelpestønadperioder) ->
                    val barnetrygd = omsorgsperioder.omsorgsmåneder()
                    val hjelpestønad = hjelpestønadperioder.omsorgsmåneder(barnetrygd)

                    when (omsorgstype) {
                        DomainOmsorgstype.BARNETRYGD -> {
                            omsorgsyter to barnetrygd
                        }

                        DomainOmsorgstype.HJELPESTØNAD -> {
                            omsorgsyter to hjelpestønad
                        }
                    }
                }
            }
    }

    fun forSummertOmsorgPerOmsorgsyter(): OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Grunnlag {
        return omsorgsmånederForOmsorgsmottakerPerOmsorgsyter().let {
            OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Grunnlag(
                omsorgsyter = omsorgsyter.fnr,
                data = it.map { (yter, antallMnd) ->
                    OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.OmsorgsmånederForMottakerOgÅr(
                        omsorgsyter = yter.fnr,
                        omsorgsmottaker = omsorgsmottaker.fnr,
                        omsorgsmåneder = antallMnd,
                        omsorgsår = omsorgsAr
                    )
                }
            )
        }
    }

    fun forAldersvurderingOmsorgsyter(): AldersvurderingsGrunnlag {
        return AldersvurderingsGrunnlag(
            person = omsorgsyter,
            omsorgsAr = omsorgsAr
        )
    }

    fun forAldersvurderingOmsorgsmottaker(): AldersvurderingsGrunnlag {
        return AldersvurderingsGrunnlag(
            person = omsorgsmottaker,
            omsorgsAr = omsorgsAr
        )
    }

    fun forFamilierelasjon(): OmsorgsyterErForelderTilMottakerAvHjelpestønad.Grunnlag {
        return OmsorgsyterErForelderTilMottakerAvHjelpestønad.Grunnlag(
            omsorgsyter = omsorgsyter.fnr,
            omsorgsytersFamilierelasjoner = omsorgsyter.familierelasjoner,
            omsorgsmottaker = omsorgsmottaker.fnr,
            omsorgsmottakersFamilierelasjoner = omsorgsmottaker.familierelasjoner
        )
    }

    abstract fun forTilstrekkeligOmsorgsarbeid(): OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag

    abstract fun forMedlemskapIFolketrygden(): OmsorgsyterErMedlemAvFolketrygden.Grunnlag
    abstract fun forMottarBarnetrygd(): OmsorgsyterMottarBarnetrgyd.Grunnlag
    abstract fun forGyldigOmsorgsarbeid(): OmsorgsyterHarGyldigOmsorgsarbeid.Grunnlag

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
                    omsorgsytersOmsorgsmånederForOmsorgsmottaker = omsorgsytersOmsorgsmånederForOmsorgsmottaker()
                )
            }

            override fun forMedlemskapIFolketrygden(): OmsorgsyterErMedlemAvFolketrygden.Grunnlag {
                return OmsorgsyterErMedlemAvFolketrygden.Grunnlag.OmsorgsmottakerFødtIOmsorgsår(
                    omsorgsytersMedlemskapsmåneder = omsorgsytersMedlemskapsmåneder()
                )
            }

            override fun forMottarBarnetrygd(): OmsorgsyterMottarBarnetrgyd.Grunnlag {
                return OmsorgsyterMottarBarnetrgyd.Grunnlag.OmsorgsmottakerFødtIOmsorgsår(
                    omsorgsytersUtbetalingsmåneder = omsorgsytersUtbetalingsmåneder()
                )
            }

            override fun forGyldigOmsorgsarbeid(): OmsorgsyterHarGyldigOmsorgsarbeid.Grunnlag {
                return OmsorgsyterHarGyldigOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtIOmsorgsår(
                    omsorgsytersMedlemskapsmåneder = omsorgsytersMedlemskapsmåneder(),
                    omsorgsytersUtbetalingsmåneder = omsorgsytersUtbetalingsmåneder(),
                    omsorgsytersOmsorgsmåneder = omsorgsytersOmsorgsmånederForOmsorgsmottaker()
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
                    omsorgsytersOmsorgsmånederForOmsorgsmottaker = omsorgsytersOmsorgsmånederForOmsorgsmottaker()
                )
            }

            override fun forMedlemskapIFolketrygden(): OmsorgsyterErMedlemAvFolketrygden.Grunnlag {
                return OmsorgsyterErMedlemAvFolketrygden.Grunnlag.OmsorgsmottakerFødtIDesemberOmsorgsår(
                    omsorgsytersMedlemskapsmåneder = omsorgsytersMedlemskapsmåneder()
                )
            }

            override fun forMottarBarnetrygd(): OmsorgsyterMottarBarnetrgyd.Grunnlag {
                return OmsorgsyterMottarBarnetrgyd.Grunnlag.OmsorgsmottakerFødtIDesemberOmsorgsår(
                    omsorgsytersUtbetalingsmåneder = omsorgsytersUtbetalingsmåneder()
                )
            }

            override fun forGyldigOmsorgsarbeid(): OmsorgsyterHarGyldigOmsorgsarbeid.Grunnlag {
                return OmsorgsyterHarGyldigOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtIDesemberOmsorgsår(
                    omsorgsytersMedlemskapsmåneder = omsorgsytersMedlemskapsmåneder(),
                    omsorgsytersUtbetalingsmåneder = omsorgsytersUtbetalingsmåneder(),
                    omsorgsytersOmsorgsmåneder = omsorgsytersOmsorgsmånederForOmsorgsmottaker()
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
                omsorgsytersOmsorgsmånederForOmsorgsmottaker = omsorgsytersOmsorgsmånederForOmsorgsmottaker()
            )
        }

        override fun forMedlemskapIFolketrygden(): OmsorgsyterErMedlemAvFolketrygden.Grunnlag {
            return OmsorgsyterErMedlemAvFolketrygden.Grunnlag.OmsorgsmottakerFødtUtenforOmsorgsår(
                omsorgsytersMedlemskapsmåneder = omsorgsytersMedlemskapsmåneder()
            )
        }

        override fun forMottarBarnetrygd(): OmsorgsyterMottarBarnetrgyd.Grunnlag {
            return OmsorgsyterMottarBarnetrgyd.Grunnlag.OmsorgsmottakerFødtUtenforOmsorgsår(
                omsorgsytersUtbetalingsmåneder = omsorgsytersUtbetalingsmåneder()
            )
        }

        override fun forGyldigOmsorgsarbeid(): OmsorgsyterHarGyldigOmsorgsarbeid.Grunnlag {
            return OmsorgsyterHarGyldigOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtUtenforOmsorgsår(
                omsorgsytersMedlemskapsmåneder = omsorgsytersMedlemskapsmåneder(),
                omsorgsytersUtbetalingsmåneder = omsorgsytersUtbetalingsmåneder(),
                omsorgsytersOmsorgsmåneder = omsorgsytersOmsorgsmånederForOmsorgsmottaker()
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
            copy(persongrunnlag = persongrunnlag
                .map { persongrunnlag -> persongrunnlag.copy(omsorgsperioder = persongrunnlag.omsorgsperioder.filter { it.omsorgsmottaker == omsorgsmottaker }) }
            )
        }
}

private fun BeriketDatagrunnlag.`avgrens for omsorgsår`(): Map<Int, BeriketDatagrunnlag> {
    return omsorgsytersOmsorgsår
        .associateWith { år ->
            copy(persongrunnlag = persongrunnlag
                .map { persongrunnlag ->
                    persongrunnlag.copy(omsorgsperioder = persongrunnlag.omsorgsperioder
                        .filter { it.periode.overlapper(år) }
                        .map { barnetrygdPeriode ->
                            barnetrygdPeriode.periode.overlappendeMåneder(år)
                                .let {
                                    Omsorgsperiode(
                                        fom = it.min(),
                                        tom = it.max(),
                                        omsorgstype = barnetrygdPeriode.omsorgstype,
                                        omsorgsmottaker = barnetrygdPeriode.omsorgsmottaker,
                                        kilde = barnetrygdPeriode.kilde,
                                        medlemskap = barnetrygdPeriode.medlemskap,
                                        utbetalt = barnetrygdPeriode.utbetalt,
                                        landstilknytning = barnetrygdPeriode.landstilknytning,
                                    )
                                }
                        })
                })
        }
}
