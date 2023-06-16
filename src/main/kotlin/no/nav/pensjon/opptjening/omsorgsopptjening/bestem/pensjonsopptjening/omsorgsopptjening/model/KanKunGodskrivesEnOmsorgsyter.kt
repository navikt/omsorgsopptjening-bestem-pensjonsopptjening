package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import java.util.UUID

class KanKunGodskrivesEnOmsorgsyter : ParagrafVilkår<KanKunGodskrivesEnOmsorgsyterGrunnlag>(
    paragrafer = setOf(Paragraf.A),
) {
    override fun vilkarsVurder(grunnlag: KanKunGodskrivesEnOmsorgsyterGrunnlag): KanKunGodskrivesEnOmsorgsyterVurdering {
        return KanKunGodskrivesEnOmsorgsyterVurdering(
            paragrafer = paragrafer,
            grunnlag = grunnlag,
            utfall = bestemUtfall(grunnlag),
        )
    }

    override fun <T : Vilkar<KanKunGodskrivesEnOmsorgsyterGrunnlag>> T.bestemUtfall(grunnlag: KanKunGodskrivesEnOmsorgsyterGrunnlag): VilkårsvurderingUtfall {
        return if (grunnlag.behandlingsIdUtfallListe.none { it.erInnvilget }) {
            VilkårsvurderingUtfall.Innvilget.EnkeltParagraf(paragraf = paragrafer.single())
        } else {
            VilkårsvurderingUtfall.Avslag.EnkeltParagraf(paragraf = paragrafer.single())
        }
    }
}

data class KanKunGodskrivesEnOmsorgsyterVurdering(
    override val paragrafer: Set<Paragraf>,
    override val grunnlag: KanKunGodskrivesEnOmsorgsyterGrunnlag,
    override val utfall: VilkårsvurderingUtfall
) : ParagrafVurdering<KanKunGodskrivesEnOmsorgsyterGrunnlag>()

data class KanKunGodskrivesEnOmsorgsyterGrunnlag(
    val behandlingsIdUtfallListe: List<BehandlingsIdUtfall>
) : ParagrafGrunnlag()

data class BehandlingsIdUtfall(
    val behandlingsId: UUID,
    val erInnvilget: Boolean
)