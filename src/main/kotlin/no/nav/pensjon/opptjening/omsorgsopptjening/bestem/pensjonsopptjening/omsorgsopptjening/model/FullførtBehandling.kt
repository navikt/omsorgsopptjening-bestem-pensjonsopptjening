package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjening
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgskategori
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Pensjonspoeng
import java.time.Instant
import java.util.UUID

data class FullførtBehandling(
    val id: UUID,
    val opprettet: Instant,
    val omsorgsAr: Int,
    val omsorgsyter: String,
    val omsorgsmottaker: String,
    val omsorgstype: DomainOmsorgskategori,
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

    fun omsorgsmottakerFødtIOmsorgsår(): Boolean {
        return when (this.grunnlag) {
            is OmsorgsopptjeningGrunnlag.FødtIOmsorgsår.FødtDesember -> true
            is OmsorgsopptjeningGrunnlag.FødtIOmsorgsår.IkkeFødtDesember -> true
            is OmsorgsopptjeningGrunnlag.IkkeFødtIOmsorgsår -> false
        }
    }

    fun hentOppgaveopplysninger(): List<Oppgaveopplysninger> {
        require(utfall is BehandlingUtfall.Manuell) { "Kan kun opprette oppgave for manuell behandling" }
        return vilkårsvurdering.finnAlleUbestemte().map { it.hentOppgaveopplysninger(this) }
    }

    fun hentBrevopplysninger(
        hentPensjonspoengForOmsorgsopptjening: (fnr: String, år: Int, type: DomainOmsorgskategori) -> Pensjonspoeng.Omsorg,
        hentPensjonspoengForInntekt: (fnr: String, år: Int) -> Pensjonspoeng.Inntekt,
    ): Brevopplysninger {
        require(utfall is BehandlingUtfall.Innvilget) { " Kan kun opprette brev for innvilget behandling" }
        return HentBrevopplysningForInfobrevOmsorgsyterForHjelpestønadsmottaker(
            hentPensjonspoengForOmsorgsopptjening,
            hentPensjonspoengForInntekt
        ).get(
            omsorgsyter = grunnlag.omsorgsyter,
            omsorgsmottaker = grunnlag.omsorgsmottaker,
            omsorgstype = omsorgstype,
            omsorgsAr = omsorgsAr,
        )
    }
}
