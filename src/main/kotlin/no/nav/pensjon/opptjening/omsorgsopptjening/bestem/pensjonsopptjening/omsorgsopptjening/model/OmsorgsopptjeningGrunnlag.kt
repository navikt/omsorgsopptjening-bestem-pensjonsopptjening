package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.BeriketDatagrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgskategori
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

    private fun landstilknytningForOmsorgsmottakerPerOmsorgsyter(): Map<Person, Landstilknytningmåneder> {
        return grunnlag.landstilknytningMånederPerOmsorgsyter(omsorgsmottaker)
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
            medlemskapsgrunnlag = grunnlag.omsorgsytersPersongrunnlag.medlemskapsgrunnlag,
            omsorgsytersOmsorgsmåneder = omsorgsmånederForOmsorgsmottakerPerOmsorgsyter()[omsorgsyter]!!,
            antallMånederRegel = antallMånederRegel(),
            landstilknytningMåneder = landstilknytningForOmsorgsmottakerPerOmsorgsyter()[omsorgsyter]!!
        )
    }

    fun forOmsorgsyterErIkkeOmsorgsmottaker(): OmsorgsyterErikkeOmsorgsmottaker.Grunnlag {
        return OmsorgsyterErikkeOmsorgsmottaker.Grunnlag(
            omsorgsyter = omsorgsyter.fnr,
            omsorgsmottaker = omsorgsmottaker.fnr
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
     * @see JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_a_Første_Punktum
     * @see JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_a_Andre_Punktum
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
