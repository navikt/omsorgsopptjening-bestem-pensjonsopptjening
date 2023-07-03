package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketDatagrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketSak
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainOmsorgstype

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
import java.time.YearMonth

sealed class BarnetrygdGrunnlag {

    abstract val omsorgsAr: Int
    abstract val grunnlag: BeriketDatagrunnlag

    val omsorgsyter: PersonMedFødselsår
        get() = grunnlag.omsorgsyter
    val omsorgsmottaker: PersonMedFødselsår
        get() = grunnlag.omsorgsmottakere().single()
    val omsorgstype: DomainOmsorgstype
        get() = grunnlag.omsorgstype
    val kjoreHash: String
        get() = grunnlag.kjoreHash
    val kilde: DomainKilde
        get() = grunnlag.kilde
    val omsorgsSaker: List<BeriketSak>
        get() = grunnlag.omsorgsSaker

    protected fun antallMånederOmsorgForOmsorgsyter(): Int {
        return omsorgsytersSak().antallMånederOmsorgFor(omsorgsmottaker).second
    }

    fun summertAntallMånederPerOmsorgsyter(): OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Grunnlag {
        return OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Grunnlag(
            omsorgsyter = omsorgsyter,
            summert = omsorgsSaker.map {
                it.antallMånederOmsorgFor(omsorgsmottaker).let { (omsorgsyter, antallMåneder) ->
                    OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.SummertOmsorgForMottakerOgÅr(
                        omsorgsyter = omsorgsyter,
                        omsorgsmottaker = omsorgsmottaker,
                        antallMåneder = antallMåneder,
                        år = omsorgsAr

                    )
                }
            }
        )

    }

    private fun omsorgsytersSak(): BeriketSak {
        return omsorgsSaker.single { it.omsorgsyter == this.omsorgsyter }
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

    protected fun alleMånederIGrunnlag(): Set<YearMonth> {
        return omsorgsSaker.flatMap { it.omsorgVedtakPerioder }.flatMap { it.periode.alleMåneder() }.distinct().toSet()
    }

    abstract fun tilstrekkeligOmsorgsarbeid(): OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag
    sealed class FødtIOmsorgsår : BarnetrygdGrunnlag() {
        data class IkkeFødtDesember(
            override val omsorgsAr: Int,
            override val grunnlag: BeriketDatagrunnlag
        ) : FødtIOmsorgsår() {
            init {
                require(
                    Periode(omsorgsAr).alleMåneder().containsAll(alleMånederIGrunnlag())
                ) { "Grunnlag contains months outside of the omsorgsår: $omsorgsAr" }
            }

            override fun tilstrekkeligOmsorgsarbeid(): OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag {
                return OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtIOmsorgsår(
                    omsorgsAr = omsorgsAr,
                    omsorgsmottaker = omsorgsmottaker,
                    antallMåneder = antallMånederOmsorgForOmsorgsyter()
                )
            }
        }

        data class FødtDesember(
            override val omsorgsAr: Int,
            override val grunnlag: BeriketDatagrunnlag
        ) : FødtIOmsorgsår() {
            init {
                val årEtterOmsorgsår = omsorgsAr + 1
                require(
                    Periode(årEtterOmsorgsår).alleMåneder().containsAll(alleMånederIGrunnlag())
                ) { "Grunnlag should only contain months from: $årEtterOmsorgsår" }
            }

            override fun tilstrekkeligOmsorgsarbeid(): OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag {
                return OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtIDesemberOmsorgsår(
                    omsorgsAr = omsorgsAr,
                    omsorgsmottaker = omsorgsmottaker,
                    antallMåneder = antallMånederOmsorgForOmsorgsyter()
                )
            }
        }
    }

    data class IkkeFødtIOmsorgsår(
        override val omsorgsAr: Int,
        override val grunnlag: BeriketDatagrunnlag
    ) : BarnetrygdGrunnlag() {
        init {
            require(
                Periode(omsorgsAr).alleMåneder().containsAll(alleMånederIGrunnlag())
            ) { "Grunnlag contains months outside of the omsorgsår: $omsorgsAr" }
        }

        override fun tilstrekkeligOmsorgsarbeid(): OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag {
            return OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtUtenforOmsorgsår(
                omsorgsAr = omsorgsAr,
                omsorgsmottaker = omsorgsmottaker,
                antallMåneder = antallMånederOmsorgForOmsorgsyter()
            )
        }
    }
}
