package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidSnapshot
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.DeltOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag.AnnenPart
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag.GrunnlagDeltOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.SamletVilkarsresultat
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Utfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.VilkarsVurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.operator.Eller.Companion.minstEn
import org.springframework.stereotype.Service

@Service
class SammenstiltVilkarsvurdering {
    fun vilkarsvurder(
        aktuelleSamletVilkarsresultat: SamletVilkarsresultat,
        involverteSamletVilkarsresultat: List<SamletVilkarsresultat>
    ): VilkarsVurdering<*> {
        val snapshot: OmsorgsarbeidSnapshot = aktuelleSamletVilkarsresultat.snapshot
        val omsorgsAr: Int = snapshot.omsorgsAr
        val omsorgsyter: Person = snapshot.omsorgsyter
        val omsorgsmottakere: List<Person> = snapshot.getOmsorgsmottakere(omsorgsyter)

        return omsorgsmottakere.minstEn {
            DeltOmsorgForBarnUnder6().vilkarsVurder(
                GrunnlagDeltOmsorgForBarnUnder6(
                    omsorgsAr = omsorgsAr,
                    omsorgsyter = snapshot.omsorgsyter,
                    omsorgsmottaker = it,
                    omsorgsArbeid50Prosent = snapshot.getOmsorgsarbeidPerioderForRelevanteAr(
                        omsorgsyter,
                        it,
                        prosent = 50
                    ),
                    andreParter = involverteSamletVilkarsresultat.createAndreParter(it),
                )
            )
        }
    }

    private fun List<SamletVilkarsresultat>.createAndreParter(omsorgsmottaker: Person) =
        map {
            AnnenPart(
                omsorgsyter = it.snapshot.omsorgsyter,
                omsorgsArbeid50Prosent = it.snapshot.getOmsorgsarbeidPerioderForRelevanteAr(
                    it.snapshot.omsorgsyter,
                    omsorgsmottaker,
                    prosent = 50
                ),
                harInvilgetOmsorgForUrelaterBarn = it.individueltVilkarsresultat!!.utfall == Utfall.INVILGET,
            )
        }.filter { it.omsorgsArbeid50Prosent.isNotEmpty() }
}




