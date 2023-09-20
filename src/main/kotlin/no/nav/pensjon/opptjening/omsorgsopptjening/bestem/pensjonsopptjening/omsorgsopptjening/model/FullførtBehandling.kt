package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjening
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.Pensjonspoeng
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.Brev
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.Oppgave
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveDetaljer
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

    fun godskrivOpptjening(): GodskrivOpptjening.Transient {
        require(erInnvilget()) { "Kan kun godskrive opptjening for innvilget behandling!" }
        return GodskrivOpptjening.Transient(
            behandlingId = id,
            meldingId = meldingId,
            correlationId = grunnlag.correlationId,
            innlesingId = grunnlag.innlesingId
        )
    }

    fun opprettOppgave(
        oppgaveEksistererForOmsorgsyter: (omsorgsyter: String, år: Int) -> Boolean,
        oppgaveEksistererForOmsorgsmottaker: (omsorgsmottaker: String, år: Int) -> Boolean,
    ): Oppgave.Transient? {
        require(!erInnvilget()) { "Kan kun opprette oppgave for avslått behandling!" }
        return avslagSkyldesFlereOmsorgsytereMedLikeMangeOmsorgsmåneder()?.let { vurdering ->
            VelgOppgaveForPersonOgInnhold(grunnlag = vurdering.grunnlag).let { oppgaveOgInnhold ->
                val oppgaveGjelderOmsorgsyter = oppgaveOgInnhold.oppgaveForPerson() == omsorgsyter
                val omsorgsyterHarOppgaveForÅr = oppgaveEksistererForOmsorgsyter(omsorgsyter, omsorgsAr)
                val omsorgsMottakerHarOppgaveForÅr = oppgaveEksistererForOmsorgsmottaker(omsorgsmottaker, omsorgsAr)

                if (oppgaveGjelderOmsorgsyter && !omsorgsyterHarOppgaveForÅr && !omsorgsMottakerHarOppgaveForÅr) {
                    lagOppgave(mottakere = oppgaveOgInnhold)
                } else {
                    null
                }
            }
        }
    }

    private fun lagOppgave(mottakere: VelgOppgaveForPersonOgInnhold): Oppgave.Transient {
        return when (grunnlag) {
            is OmsorgsopptjeningGrunnlag.FødtIOmsorgsår -> {
                OppgaveDetaljer.FlereOmsorgytereMedLikeMyeOmsorgIFødselsår(
                    omsorgsyter = omsorgsyter,
                    omsorgsmottaker = omsorgsmottaker,
                )
            }

            is OmsorgsopptjeningGrunnlag.IkkeFødtIOmsorgsår -> {
                OppgaveDetaljer.FlereOmsorgytereMedLikeMyeOmsorg(
                    omsorgsyter = omsorgsyter,
                    omsorgsmottaker = omsorgsmottaker,
                    annenOmsorgsyter = mottakere.annenPersonForInnhold(),
                )
            }
        }.let {
            Oppgave.Transient(
                detaljer = it,
                behandlingId = id,
                meldingId = meldingId,
                correlationId = grunnlag.correlationId,
                innlesingId = grunnlag.innlesingId,
            )
        }
    }

    /**
     * Det er bare aktuelt å lage oppgave i tilfeller hvor det ikke kan godskrives oppgjening som følge av at flere
     * omsorgsyter har like mange omsorgsmåneder for det samme barnet i løpet av omsorgsåret.
     */
    private fun avslagSkyldesFlereOmsorgsytereMedLikeMangeOmsorgsmåneder(): OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering? {
        return vilkårsvurdering.finnVurdering<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>().let {
            if (vilkårsvurdering.erEnesteAvslag<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>() && it.grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmåneder()) {
                it
            } else {
                null
            }
        }
    }

    /**
     * Prioriterer oppgavemottakere etter kriteriene:
     * 1. Flest omsorgsmåneder
     * 2. Hadde omsorg i desember måned
     * 3. Er personen [grunnlag] gjelder for
     *    Dette sørger for at vi alltid prioriterer å sende oppgave for omsorgsyteren behandlingen gjelder.
     */
    private data class VelgOppgaveForPersonOgInnhold(
        private val grunnlag: OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Grunnlag,
    ) {
        private val prioritert: List<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.OmsorgsmånederForMottakerOgÅr> =
            grunnlag
                .omsorgsytereMedFlestOmsorgsmåneder()
                .sortedWith(compareBy<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.OmsorgsmånederForMottakerOgÅr> { it.haddeOmsorgIDesember() }.thenBy { it.omsorgsyter == grunnlag.omsorgsyter })
                .reversed()

        fun oppgaveForPerson(): String {
            return prioritert.first().omsorgsyter
        }

        fun annenPersonForInnhold(): String {
            return prioritert.first { it.omsorgsyter != grunnlag.omsorgsyter }.omsorgsyter
        }
    }

    fun sendBrev(
        hentPensjonspoengForOmsorgsopptjening: (fnr: String, år: Int, type: DomainOmsorgstype) -> Pensjonspoeng,
        hentPensjonspoengForInntekt: (fnr: String, år: Int) -> Pensjonspoeng,
    ): Brev? {
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
                    !grunnlag.omsorgsyter.erForelderAv(omsorgsmottaker) || //TODO dette henger ikke sammen med vilkårsvurderingen atm
                    omsorgsytersOmsorgspoengForegåendeÅr.poeng == 0.0 ||
                    omsorgsytersOmsorgspoengForOmsorgsår > annenForeldersInntektspoengOmsorgsår
                ) {
                    return Brev(
                        omsorgsyter = omsorgsyter, //TODO slapp modell, hentes fra behandling i basen inntil videre, håndtering av verge/annen mottaker - annen app?
                        behandlingId = id,
                        meldingId = meldingId,
                        correlationId = grunnlag.correlationId,
                        innlesingId = grunnlag.innlesingId
                    )
                } else {
                    null
                }
            }
        }
    }
}
