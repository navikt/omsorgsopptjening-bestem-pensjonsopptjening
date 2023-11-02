package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Og.Companion.og
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.BehandlingsutfallDb
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.Oppgave
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveDetaljer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype
import java.util.UUID

/**
 * En automatisk vurdering av en [omsorgsyter]s rett til omsorgsopptjening på bakgrunn av omsorgsarbeid
 * ytt for [omsorgsmottaker] i et gitt [omsorgsår].
 */
data class Behandling(
    private val grunnlag: OmsorgsopptjeningGrunnlag,
    private val vurderVilkår: VurderVilkår,
    private val meldingId: UUID
) {
    val vilkårsvurdering = vilkårsvurdering()

    fun omsorgsår() = grunnlag.omsorgsAr
    fun omsorgsmottaker() = grunnlag.omsorgsmottaker
    fun omsorgsyter() = grunnlag.omsorgsyter
    fun omsorgstype() = grunnlag.omsorgstype
    fun grunnlag() = grunnlag

    fun meldingId() = meldingId

    fun utfall(): BehandlingUtfall {
        return vilkårsvurdering.let { vilkårsvurdering ->
            when (vilkårsvurdering.utfall.erInnvilget()) {
                true -> BehandlingUtfall.Innvilget
                false -> {
                    opprettOppgave()?.let { oppgave ->
                        BehandlingUtfall.Manuell(oppgave)
                    }?:BehandlingUtfall.Avslag
                }
            }
        }
    }

    fun oppgave() : Oppgave.Transient? {
        return when (val u = utfall()) {
            is BehandlingUtfall.Manuell -> u.oppgave
            else -> null
        }
    }

    fun vilkårsvurdering(): VilkarsVurdering<*> {
        return og(
            vilkårsvurderOmsorgsyter(),
            when (omsorgstype()) {
                DomainOmsorgstype.BARNETRYGD -> {
                    vilkårsvurderBarnetrygd()
                }

                DomainOmsorgstype.HJELPESTØNAD -> {
                    vilkårsurderHjelpestønad()
                }
            }
        )
    }

    private fun vilkårsvurderBarnetrygd(): VilkarsVurdering<*> {
        return vurderVilkår.OmsorgsmottakerOppfyllerAlderskravForBarnetryg()
    }

    private fun vilkårsurderHjelpestønad(): VilkarsVurdering<*> {
        return og(
            vurderVilkår.OmsorgsmottakerOppfyllerAlderskravForHjelpestønad(),
            //vurderVilkår.OmsorgsyterErForelderTilMottakerAvHjelpestønad(), dette var ikke et kriterium i batch
        )
    }

    private fun vilkårsvurderOmsorgsyter(): VilkarsVurdering<*> {
        return og(
            vurderVilkår.OmsorgsyterOppfyllerAlderskrav(),
            vurderVilkår.OmsorgsyterErMedlemAvFolketrygden(),
            vurderVilkår.OmsorgsyterMottarBarnetrgyd(),
            vurderVilkår.OmsorgsyterHarTilstrekkeligOmsorgsarbeid(),
            vurderVilkår.OmsorgsyterHarGyldigOmsorgsarbeid(),
            vurderVilkår.OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere(),
            vurderVilkår.OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr(),
            vurderVilkår.OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr()
        )
    }

    // TODO rydde
    private fun opprettOppgave(): Oppgave.Transient? {
        return vilkårsvurdering.finnVurdering<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>().let { vurdering ->
            if (vilkårsvurdering.erEnesteAvslag<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>()
                && vurdering.grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmåneder()
            )
                VelgOppgaveForPersonOgInnhold(grunnlag = vurdering.grunnlag).let { oppgaveOgInnhold ->
                    val oppgaveGjelderOmsorgsyter = oppgaveOgInnhold.oppgaveForPerson() == omsorgsyter().fnr
                    if (oppgaveGjelderOmsorgsyter) {
                        lagOppgave(mottakere = oppgaveOgInnhold)
                    } else {
                        null
                    }
                }
            else {
                null
            }
        }
    }

    private fun lagOppgave(mottakere: VelgOppgaveForPersonOgInnhold): Oppgave.Transient {
        return when (grunnlag) {
            is OmsorgsopptjeningGrunnlag.FødtIOmsorgsår -> {
                OppgaveDetaljer.FlereOmsorgytereMedLikeMyeOmsorgIFødselsår(
                    omsorgsyter = omsorgsyter().fnr,
                    omsorgsmottaker = omsorgsmottaker().fnr,
                )
            }

            is OmsorgsopptjeningGrunnlag.IkkeFødtIOmsorgsår -> {
                OppgaveDetaljer.FlereOmsorgytereMedLikeMyeOmsorg(
                    omsorgsyter = omsorgsyter().fnr,
                    omsorgsmottaker = omsorgsmottaker().fnr,
                    annenOmsorgsyter = mottakere.annenPersonForInnhold(),
                )
            }
        }.let {
            Oppgave.Transient(
                detaljer = it,
                behandlingId = null,
                meldingId = meldingId,
            )
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

}


