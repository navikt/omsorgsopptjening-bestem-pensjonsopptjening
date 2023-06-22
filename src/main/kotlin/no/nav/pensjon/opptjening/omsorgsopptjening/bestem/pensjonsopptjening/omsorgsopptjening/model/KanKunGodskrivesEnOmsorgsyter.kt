package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import java.util.UUID

object KanKunGodskrivesEnOmsorgsyter : ParagrafVilkår<KanKunGodskrivesEnOmsorgsyter.Grunnlag>() {
    override fun vilkarsVurder(grunnlag: Grunnlag): Vurdering {
        return bestemUtfall(grunnlag).let {
            Vurdering(
                henvisninger = it.henvisninger(),
                grunnlag = grunnlag,
                utfall = it,
            )
        }
    }

    override fun <T : Vilkar<Grunnlag>> T.bestemUtfall(grunnlag: Grunnlag): VilkårsvurderingUtfall {
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

    data class Vurdering(
        override val henvisninger: Set<Henvisning>,
        override val grunnlag: Grunnlag,
        override val utfall: VilkårsvurderingUtfall
    ) : ParagrafVurdering<Grunnlag>()

    data class Grunnlag(
        val behandlingsIdUtfallListe: List<BehandlingsIdUtfall>
    ) : ParagrafGrunnlag() {
        data class BehandlingsIdUtfall(
            //TODO legg til år og mottaker
            val behandlingsId: UUID,
            val erInnvilget: Boolean
        )
    }
}