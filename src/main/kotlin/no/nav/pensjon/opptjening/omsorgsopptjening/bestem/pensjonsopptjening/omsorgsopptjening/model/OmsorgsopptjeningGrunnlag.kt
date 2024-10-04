package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.BeriketDatagrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgskategori
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Hjelpestønadperiode
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Omsorgsperiode
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

    val omsorgstype: DomainOmsorgskategori by lazy {
        when (forAldersvurderingOmsorgsmottaker().erOppfylltFor(OmsorgsmottakerOppfyllerAlderskravForBarnetrygd.ALDERSINTERVALL_BARNETRYGD)) {
            true -> DomainOmsorgskategori.BARNETRYGD
            false -> DomainOmsorgskategori.HJELPESTØNAD
        }
    }

    private fun omsorgsmånederForOmsorgsmottakerPerOmsorgsyter(): Map<Person, Omsorgsmåneder> {
        return (grunnlag.omsorgsmånederPerOmsorgsyter(omsorgsmottaker) to grunnlag.hjelpestønadMånederPerOmsorgsyter(
            omsorgsmottaker
        )).let { (bt, hs) ->
            //disse skal i utgangspunktet være 1-1
            (bt.keys + hs.keys).distinct().associate { p ->
                Triple(p, bt[p]!!, hs[p]!!).let { (person, barnetrygd, hjelpestønad) ->
                    when (omsorgstype) {
                        DomainOmsorgskategori.BARNETRYGD -> {
                            person to barnetrygd
                        }

                        DomainOmsorgskategori.HJELPESTØNAD -> {
                            person to hjelpestønad
                        }
                    }
                }
            }
        }
    }

    private fun utbetalingsmånederForOmsorgsmottakerPerOmsorgsyter(): Map<Person, Utbetalingsmåneder> {
        return grunnlag.utbetalingsmånederPerOmsorgsyter(omsorgsmottaker)
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

    fun forTilstrekkeligOmsorgsarbeid(): OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag {
        return OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag(
            omsorgsytersOmsorgsmånederForOmsorgsmottaker = omsorgsmånederForOmsorgsmottakerPerOmsorgsyter()[omsorgsyter]!!,
            antallMånederRegel = antallMånederRegel()
        )
    }

    fun forMottarBarnetrygd(): OmsorgsyterMottarBarnetrgyd.Grunnlag {
        return OmsorgsyterMottarBarnetrgyd.Grunnlag(
            omsorgsytersUtbetalingsmåneder = utbetalingsmånederForOmsorgsmottakerPerOmsorgsyter()[omsorgsyter]!!,
            antallMånederRegel = antallMånederRegel(),
            omsorgstype = omsorgstype,
        )
    }

    fun forGyldigOmsorgsarbeid(omsorgsyter: Person): OmsorgsyterHarGyldigOmsorgsarbeid.Grunnlag {
        return OmsorgsyterHarGyldigOmsorgsarbeid.Grunnlag(
            omsorgsytersUtbetalingsmåneder = utbetalingsmånederForOmsorgsmottakerPerOmsorgsyter()[omsorgsyter]!!,
            omsorgsytersOmsorgsmåneder = omsorgsmånederForOmsorgsmottakerPerOmsorgsyter()[omsorgsyter]!!,
            antallMånederRegel = antallMånederRegel()
        )
    }

    fun forMedlemskapIFolketrygden(): OmsorgsyterErMedlemIFolketrygden.Grunnlag {
        return OmsorgsyterErMedlemIFolketrygden.Grunnlag(
            loveMEVurdering = grunnlag.omsorgsytersPersongrunnlag.medlemskapsgrunnlag.vurderingFraLoveME,
            omsorgstype = omsorgstype
        )
    }

    fun forGyldigOmsorgsarbeidPerOmsorgsyter(): OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Grunnlag {
        return grunnlag.persongrunnlag.associate {
            it.omsorgsyter to forGyldigOmsorgsarbeid(it.omsorgsyter)
        }.map {
            it.key.fnr to it.value.gyldigeOmsorgsmåneder
        }.let {
            OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Grunnlag(
                omsorgsyter = omsorgsyter.fnr,
                data = it.map { (yter, antallMnd) ->
                    OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.OmsorgsmånederForMottakerOgÅr(
                        omsorgsyter = yter,
                        omsorgsmottaker = omsorgsmottaker.fnr,
                        omsorgsmåneder = antallMnd,
                        omsorgsår = omsorgsAr
                    )
                }
            )
        }
    }

    abstract fun antallMånederRegel(): AntallMånederRegel

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
                grunnlag.valider()
            }

            override fun antallMånederRegel(): AntallMånederRegel {
                return AntallMånederRegel.FødtIOmsorgsår
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
                grunnlag.valider()
            }

            override fun antallMånederRegel(): AntallMånederRegel {
                return AntallMånederRegel.FødtIOmsorgsår
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
            grunnlag.valider()
        }

        override fun antallMånederRegel(): AntallMånederRegel {
            return AntallMånederRegel.FødtUtenforOmsorgsår
        }
    }

    protected fun BeriketDatagrunnlag.valider() {
        require(
            this.alleOmsorgsmottakere.distinct().count() == 1
        ) { "Grunnlagsdata inneholder flere omsorgsmottakere!" }
        require(
            this.alleOmsorgsmottakere.distinct().single() == omsorgsmottaker
        ) { "Grunnlagsdata inneholder ikke forventet omsorgsmottaker" }
        require(
            this.alleMåneder.distinctBy { it.year }.count() == 1
        ) { "Grunnlagsdata inneholder grunnlag for flere omsorgsår" }
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
                .map { persongrunnlag ->
                    persongrunnlag.copy(
                        omsorgsperioder = persongrunnlag.omsorgsperioder.filter { it.omsorgsmottaker == omsorgsmottaker },
                        hjelpestønadperioder = persongrunnlag.hjelpestønadperioder.filter { it.omsorgsmottaker == omsorgsmottaker }
                    )
                }
            )
        }
}

private fun BeriketDatagrunnlag.`avgrens for omsorgsår`(): Map<Int, BeriketDatagrunnlag> {
    return omsorgsytersOmsorgsår
        .associateWith { år ->
            copy(persongrunnlag = persongrunnlag
                .map { persongrunnlag ->
                    persongrunnlag.copy(
                        omsorgsperioder = persongrunnlag.omsorgsperioder
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
                                            utbetalt = barnetrygdPeriode.utbetalt,
                                            landstilknytning = barnetrygdPeriode.landstilknytning,
                                        )
                                    }
                            },
                        hjelpestønadperioder = persongrunnlag.hjelpestønadperioder
                            .filter { it.periode.overlapper(år) }
                            .map { hjelpestønadperiode ->
                                hjelpestønadperiode.periode.overlappendeMåneder(år)
                                    .let {
                                        Hjelpestønadperiode(
                                            fom = it.min(),
                                            tom = it.max(),
                                            omsorgstype = hjelpestønadperiode.omsorgstype,
                                            omsorgsmottaker = hjelpestønadperiode.omsorgsmottaker,
                                            kilde = hjelpestønadperiode.kilde
                                        )
                                    }
                            }
                    )
                })
        }
}
