package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidSnapshot
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.DeltOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag.AnnenPart
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag.GrunnlagDeltOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.*
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.operator.Eller.Companion.minstEn
import org.springframework.stereotype.Component

@Component
class SammenstiltVilkarsvurdering {
    fun vilkarsvurder(
        behandledeVilkarsresultat: Vilkarsresultat,
        involverteVilkarsresultat: List<Vilkarsresultat>
    ): VilkarsVurdering<*> {
        val snapshot: OmsorgsarbeidSnapshot = behandledeVilkarsresultat.snapshot
        val omsorgsAr: Int = snapshot.omsorgsAr
        val omsorgsyter: Person = snapshot.omsorgsyter
        val omsorgsmottakere: List<Person> = snapshot.getOmsorgsmottakere(omsorgsyter)

        return omsorgsmottakere.minstEn {
            DeltOmsorgForBarnUnder6().vilkarsVurder(
                GrunnlagDeltOmsorgForBarnUnder6(
                    omsorgsAr = omsorgsAr,
                    omsorgsyter = snapshot.omsorgsyter,
                    omsorgsmottaker = it,
                    utfallAbsolutteKrav = behandledeVilkarsresultat.personVilkarsvurdering!!.utfall,
                    omsorgsArbeid50Prosent = snapshot.getOmsorgsarbeidPerioderForRelevanteAr(
                        omsorgsyter = omsorgsyter,
                        omsorgsmottaker = it,
                        prosent = 50
                    ),
                    andreParter = involverteVilkarsresultat.mapToAndreParter(it),
                )
            )
        }
    }

    private fun List<Vilkarsresultat>.mapToAndreParter(omsorgsmottaker: Person) =
        map {
            AnnenPart(
                omsorgsyter = it.snapshot.omsorgsyter,
                omsorgsArbeid50Prosent = it.snapshot.getOmsorgsarbeidPerioderForRelevanteAr(
                    it.snapshot.omsorgsyter,
                    omsorgsmottaker,
                    prosent = 50
                ),
                harInvilgetOmsorgForUrelaterBarn = it.individueltVilkarsVurdering!!.utfall == Utfall.INVILGET,
                utfallAbsolutteKrav = it.personVilkarsvurdering!!.utfall,
            )
        }.filter { it.omsorgsArbeid50Prosent.isNotEmpty() }
}



