package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.Brev
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjening
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Pensjonspoeng
import java.time.Instant
import java.util.UUID

data class FullførtBehandling(
    val id: UUID,
    val opprettet: Instant,
    val omsorgsAr: Int,
    val omsorgsyter: String,
    val omsorgsmottaker: String,
    val omsorgstype: DomainOmsorgstype,
    val grunnlag: OmsorgsopptjeningGrunnlag,
    val utfall: BehandlingUtfall,
    val vilkårsvurdering: VilkarsVurdering<*>,
    val meldingId: UUID,
) {
    fun erInnvilget(): Boolean {
        return utfall.erInnvilget()
    }

    fun erManuell(): Boolean {
        return utfall.erManuell()
    }

    fun erAvslag(): Boolean {
        return utfall.erAvslag()
    }

    fun avslåtteVilkår(): List<VilkarsVurdering<*>> {
        return vilkårsvurdering.finnAlleAvslatte()
    }

    fun godskrivOpptjening(): GodskrivOpptjening.Transient {
        require(erInnvilget()) { "Kan kun godskrive opptjening for innvilget behandling!" }
        return GodskrivOpptjening.Transient(behandlingId = id)
    }

    private fun omsorgsmottakerFødtIOmsorgsår(): Boolean {
        return when(this.grunnlag){
            is OmsorgsopptjeningGrunnlag.FødtIOmsorgsår.FødtDesember -> true
            is OmsorgsopptjeningGrunnlag.FødtIOmsorgsår.IkkeFødtDesember -> true
            is OmsorgsopptjeningGrunnlag.IkkeFødtIOmsorgsår -> false
        }
    }

    fun sendBrev(
        hentPensjonspoengForOmsorgsopptjening: (fnr: String, år: Int, type: DomainOmsorgstype) -> Pensjonspoeng,
        hentPensjonspoengForInntekt: (fnr: String, år: Int) -> Pensjonspoeng,
    ): Brev.Transient? {
        require(erInnvilget()) { "Kan bare sende brev for innvilget behandling!" }
        fun omsorgspoeng(): Pensjonspoeng.Omsorg {
            return godskrivOpptjening().let {
                Pensjonspoeng.Omsorg(
                    år = omsorgsAr,
                    poeng = GodskrivOpptjening.OMSORGSPOENG_GODSKRIVES,
                    type = omsorgstype
                )
            }
        }

        return when (omsorgstype) {
            DomainOmsorgstype.BARNETRYGD -> {
                null
            }

            DomainOmsorgstype.HJELPESTØNAD -> {
                val foreldre = grunnlag.omsorgsmottaker.finnForeldre()
                //TODO føles som man kanskje burde sjekke begge omsorgstyper for å unngå brev for tilfeller hvor omsorgsyter har poeng fra barnetryg for året før?
                val omsorgsytersOmsorgspoengForegåendeÅr = hentPensjonspoengForOmsorgsopptjening(
                    omsorgsyter,
                    omsorgsAr - 1,
                    omsorgstype
                )
                val (omsorgsytersOmsorgspoengForOmsorgsår, annenForeldersInntektspoengOmsorgsår) = when {
                    foreldre.farEllerMedmor == omsorgsyter -> {
                        omsorgspoeng() to hentPensjonspoengForInntekt(
                            foreldre.mor,
                            omsorgsAr,
                        )
                    }

                    foreldre.mor == omsorgsyter -> {
                        omsorgspoeng() to hentPensjonspoengForInntekt(
                            foreldre.farEllerMedmor,
                            omsorgsAr,
                        )
                    }

                    else -> {
                        throw RuntimeException("Uventet familiekonstellasjon, vilkår at foreldre er mott")
                    }
                }

                return if (
                    !grunnlag.omsorgsyter.erForelderAv(omsorgsmottaker) ||
                    omsorgsytersOmsorgspoengForegåendeÅr.poeng == 0.0 ||
                    omsorgsytersOmsorgspoengForOmsorgsår > annenForeldersInntektspoengOmsorgsår
                ) {
                    return Brev.Transient(behandlingId = id)
                } else {
                    null
                }
            }
        }
    }

    fun hentOppgaveopplysninger(): List<Oppgaveopplysninger> {
        require(utfall is BehandlingUtfall.Manuell) { "Kan kun opprette oppgave for manuell behandling" }
        return vilkårsvurdering.finnAlleUbestemte().map { it.hentOppgaveopplysninger(omsorgsmottakerFødtIOmsorgsår()) }
    }
}
