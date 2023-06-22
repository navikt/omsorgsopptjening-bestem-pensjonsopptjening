package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketDatagrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketSak
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketVedtaksperiode
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.Periode
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.PersonMedFødselsår
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
    val originaltGrunnlag: OmsorgsGrunnlag
        get() = grunnlag.originaltGrunnlag


    private fun hentFullOmsorgForMottaker(): List<BeriketVedtaksperiode> {
        return omsorgsytersSaker().omsorgVedtakPeriode
            .filter { it.prosent == 100 }
    }

    protected fun antallMånederFullOmsorgForMottaker(): Int {
        return hentFullOmsorgForMottaker()
            .flatMap { it.periode.alleMåneder() }
            .toSet()
            .count()

    }

    private fun omsorgsytersSaker(): BeriketSak {
        return omsorgsSaker.single { it.omsorgsyter == this.omsorgsyter }
    }

    fun forOmsorgsyterOgÅr(): PersonOgOmsorgsårGrunnlag {
        return PersonOgOmsorgsårGrunnlag(
            person = omsorgsyter,
            omsorgsAr = omsorgsAr
        )
    }

    fun tilnærmetLikeMyeOmsorgsarbeidBlantFlereOmsorgsytere(): OmsorgsopptjeningKanIkkeGisHvisTilnærmetLikeMyeOmsorgsarbeidBlantFlereOmsorgsytere.Grunnlag {
        return omsorgsSaker.associate { it.antallMånederOmsorgFor(omsorgsmottaker) }.let { yterTilAntallMnd ->
            OmsorgsopptjeningKanIkkeGisHvisTilnærmetLikeMyeOmsorgsarbeidBlantFlereOmsorgsytere.Grunnlag(
                OmsorgsopptjeningKanIkkeGisHvisTilnærmetLikeMyeOmsorgsarbeidBlantFlereOmsorgsytere.Grunnlag.YterMottakerManeder(
                    omsorgsyter = omsorgsyter,
                    omsorgsmottaker = omsorgsmottaker,
                    antallManeder = yterTilAntallMnd[omsorgsyter]!!
                ),
                andreOmsorgsytere = yterTilAntallMnd.filterNot { it.key == omsorgsyter }
                    .map { (annenOmsorgsyter, antMnd) ->
                        OmsorgsopptjeningKanIkkeGisHvisTilnærmetLikeMyeOmsorgsarbeidBlantFlereOmsorgsytere.Grunnlag.YterMottakerManeder(
                            omsorgsyter = annenOmsorgsyter,
                            omsorgsmottaker = omsorgsmottaker,
                            antallManeder = antMnd
                        )
                    }
            )
        }
    }


    fun forOmsorgsmottakerOgÅr(): PersonOgOmsorgsårGrunnlag {
        return PersonOgOmsorgsårGrunnlag(
            person = omsorgsmottaker,
            omsorgsAr = omsorgsAr
        )
    }

    protected fun alleMånederIGrunnlag(): Set<YearMonth> {
        return omsorgsSaker.flatMap { it.omsorgVedtakPeriode }.flatMap { it.periode.alleMåneder() }.distinct().toSet()
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
                    minstEnMånedFullOmsorg = antallMånederFullOmsorgForMottaker() > 0
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
                    minstEnMånedOmsorgÅretEtterFødsel = antallMånederFullOmsorgForMottaker() > 0
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
                minstSeksMånederFullOmsorg = antallMånederFullOmsorgForMottaker() > 6
            )
        }
    }
}
