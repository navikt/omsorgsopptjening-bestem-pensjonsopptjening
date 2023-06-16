package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import java.util.UUID

class KanKunGodskrivesEnOmsorgsyter : ParagrafVilkår<KanKunGodskrivesEnOmsorgsyterGrunnlag>() {
    override fun vilkarsVurder(grunnlag: KanKunGodskrivesEnOmsorgsyterGrunnlag): KanKunGodskrivesEnOmsorgsyterVurdering {
        return bestemUtfall(grunnlag).let { KanKunGodskrivesEnOmsorgsyterVurdering(
            lovhenvisninger = it.lovhenvisning(),
            grunnlag = grunnlag,
            utfall = it,
        ) }
    }

    override fun <T : Vilkar<KanKunGodskrivesEnOmsorgsyterGrunnlag>> T.bestemUtfall(grunnlag: KanKunGodskrivesEnOmsorgsyterGrunnlag): VilkårsvurderingUtfall {
        val lovhenvisning = setOf(Lovhenvisning.OMSORGSOPPTJENING_GIS_KUN_EN_OMSORGSYTER)
        return if (grunnlag.behandlingsIdUtfallListe.none { it.erInnvilget }) {
            VilkårsvurderingUtfall.Innvilget.EnkeltParagraf(lovhenvisning = lovhenvisning)
        } else {
            VilkårsvurderingUtfall.Avslag.EnkeltParagraf(lovhenvisning = lovhenvisning)
        }
    }
}

data class KanKunGodskrivesEnOmsorgsyterVurdering(
    override val lovhenvisninger: Set<Lovhenvisning>,
    override val grunnlag: KanKunGodskrivesEnOmsorgsyterGrunnlag,
    override val utfall: VilkårsvurderingUtfall
) : ParagrafVurdering<KanKunGodskrivesEnOmsorgsyterGrunnlag>()

data class KanKunGodskrivesEnOmsorgsyterGrunnlag(
    val behandlingsIdUtfallListe: List<BehandlingsIdUtfall>
) : ParagrafGrunnlag()

data class BehandlingsIdUtfall(
    //TODO legg til år og mottaker
    val behandlingsId: UUID,
    val erInnvilget: Boolean
)