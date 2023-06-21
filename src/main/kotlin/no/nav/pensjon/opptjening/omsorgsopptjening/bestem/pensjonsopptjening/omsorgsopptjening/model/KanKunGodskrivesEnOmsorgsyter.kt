package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import java.util.UUID

class KanKunGodskrivesEnOmsorgsyter : ParagrafVilkår<KanKunGodskrivesEnOmsorgsyterGrunnlag>() {
    override fun vilkarsVurder(grunnlag: KanKunGodskrivesEnOmsorgsyterGrunnlag): KanKunGodskrivesEnOmsorgsyterVurdering {
        return bestemUtfall(grunnlag).let {
            KanKunGodskrivesEnOmsorgsyterVurdering(
                henvisninger = it.henvisninger(),
                grunnlag = grunnlag,
                utfall = it,
            )
        }
    }

    override fun <T : Vilkar<KanKunGodskrivesEnOmsorgsyterGrunnlag>> T.bestemUtfall(grunnlag: KanKunGodskrivesEnOmsorgsyterGrunnlag): VilkårsvurderingUtfall {
        setOf(
            Referanse.OmsorgsopptjeningGisKunEnOmsorgsyter()
        ).let {
            return if (grunnlag.behandlingsIdUtfallListe.none { it.erInnvilget }) {
                VilkårsvurderingUtfall.Innvilget.Vilkår.from(it)
            } else {
                VilkårsvurderingUtfall.Avslag.Vilkår.from(it)
            }
        }
    }
}

data class KanKunGodskrivesEnOmsorgsyterVurdering(
    override val henvisninger: Set<Henvisning>,
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