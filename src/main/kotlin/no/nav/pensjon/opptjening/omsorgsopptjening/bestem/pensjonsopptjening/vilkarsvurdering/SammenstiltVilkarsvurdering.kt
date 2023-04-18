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

        return omsorgsmottakere.minstEn { omsorgsmottaker ->
            DeltOmsorgForBarnUnder6().vilkarsVurder(
                GrunnlagDeltOmsorgForBarnUnder6(
                    omsorgsAr = omsorgsAr,
                    omsorgsyter = omsorgsyter,
                    omsorgsmottaker = omsorgsmottaker,
                    utfallPersonVilkarsvurdering = behandledeVilkarsresultat.personVilkarsvurdering!!.utfall,
                    omsorgsArbeid50Prosent = snapshot.getOmsorgsarbeidPerioderForRelevanteAr(
                        omsorgsyter = omsorgsyter,
                        omsorgsmottaker = omsorgsmottaker,
                        prosent = 50
                    ),
                    andreParter = involverteVilkarsresultat.mapToAndreParter(omsorgsmottaker),
                )
            )
        }
    }

    private fun List<Vilkarsresultat>.mapToAndreParter(omsorgsmottaker: Person) =
        map { annenPartsVilkarsresultat ->
            AnnenPart(
                omsorgsyter = annenPartsVilkarsresultat.snapshot.omsorgsyter,
                omsorgsArbeid50Prosent = annenPartsVilkarsresultat.snapshot.getOmsorgsarbeidPerioderForRelevanteAr(
                    annenPartsVilkarsresultat.snapshot.omsorgsyter,
                    omsorgsmottaker,
                    prosent = 50
                ),
                harInvilgetOmsorgForUrelaterBarn = annenPartsVilkarsresultat.individueltVilkarsVurdering!!.utfall == Utfall.INVILGET,
                utfallAbsolutteKrav = annenPartsVilkarsresultat.personVilkarsvurdering!!.utfall,
            )
        }.filter { it.omsorgsArbeid50Prosent.isNotEmpty() }
}




