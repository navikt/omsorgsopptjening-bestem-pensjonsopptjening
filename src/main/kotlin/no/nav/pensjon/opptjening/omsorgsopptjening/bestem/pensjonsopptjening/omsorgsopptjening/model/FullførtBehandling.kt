package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.Oppgave
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.OppgaveDetaljer
import java.time.Instant
import java.util.UUID

data class FullførtBehandling(
    val id: UUID,
    val opprettet: Instant,
    val omsorgsAr: Int,
    val omsorgsyter: String,
    val omsorgsmottaker: String,
    val omsorgstype: DomainOmsorgstype,
    val grunnlag: BarnetrygdGrunnlag,
    val utfall: BehandlingUtfall,
    val vilkårsvurdering: VilkarsVurdering<*>,
    val kafkaMeldingId: UUID,
) {
    fun kilde(): DomainKilde {
        return grunnlag.kilde
    }

    fun erInnvilget(): Boolean {
        return utfall.erInnvilget()
    }

    fun opprettOppgave(
        oppgaveEksistererForOmsorgsyter: (omsorgsyter: String, år: Int) -> Boolean,
        oppgaveEksistererForOmsorgsmottaker: (omsorgsmottaker: String, år: Int) -> Boolean,
    ): Oppgave? {
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

    private fun lagOppgave(mottakere: VelgOppgaveForPersonOgInnhold): Oppgave {
        return when (grunnlag) {
            is BarnetrygdGrunnlag.FødtIOmsorgsår -> {
                OppgaveDetaljer.FlereOmsorgytereMedLikeMyeOmsorgIFødselsår(
                    omsorgsyter = omsorgsyter,
                    omsorgsmottaker = omsorgsmottaker,
                )
            }

            is BarnetrygdGrunnlag.IkkeFødtIOmsorgsår -> {
                OppgaveDetaljer.FlereOmsorgytereMedLikeMyeOmsorg(
                    omsorgsyter = omsorgsyter,
                    omsorgsmottaker = omsorgsmottaker,
                    annenOmsorgsyter = mottakere.annenPersonForInnhold(),
                )
            }
        }.let {
            Oppgave(
                detaljer = it,
                behandlingId = id,
                meldingId = kafkaMeldingId
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
                .sortedWith(compareBy<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.OmsorgsmånederForMottakerOgÅr> { it.haddeOmsorgIDesember() }.thenBy { it.omsorgsyter.fnr == grunnlag.omsorgsyter.fnr })
                .reversed()

        fun oppgaveForPerson(): String {
            return prioritert.first().omsorgsyter.fnr
        }

        fun annenPersonForInnhold(): String {
            return prioritert.first { it.omsorgsyter.fnr != grunnlag.omsorgsyter.fnr }.omsorgsyter.fnr
        }
    }
}
