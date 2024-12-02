package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.Oppgave
import java.time.LocalDate

object OmsorgsyterHarIkkeDødsdato : ParagrafVilkår<OmsorgsyterHarIkkeDødsdato.Grunnlag>() {
    override fun vilkarsVurder(grunnlag: Grunnlag): Vurdering {
        return Vurdering(
            grunnlag = grunnlag,
            utfall = bestemUtfall(grunnlag),
        )
    }

    override fun <T : Vilkar<Grunnlag>> T.bestemUtfall(grunnlag: Grunnlag): VilkårsvurderingUtfall {
        return when (grunnlag.harDødsdato()) {
            true -> VilkårsvurderingUtfall.Ubestemt.Vilkår(emptySet())
            false -> VilkårsvurderingUtfall.Innvilget.Vilkår(emptySet())
        }
    }

    data class Vurdering(
        override val grunnlag: Grunnlag,
        override val utfall: VilkårsvurderingUtfall
    ) : ParagrafVurdering<Grunnlag>() {
        override fun hentOppgaveopplysninger(behandling: FullførtBehandling): Oppgaveopplysninger {
            return Oppgaveopplysninger.Generell(
                oppgavemottaker = behandling.omsorgsyter,
                oppgaveTekst = Oppgave.omsorgsyterErDød(behandling.omsorgsyter, behandling.omsorgsmottaker)
            )
        }
    }

    data class Grunnlag(
        val dødsdato: LocalDate?
    ) : ParagrafGrunnlag() {

        fun harDødsdato(): Boolean {
            return dødsdato != null
        }
    }
}