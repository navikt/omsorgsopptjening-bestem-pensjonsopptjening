package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidSnapshot
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.DeltOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag.AnnenPart
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag.GrunnlagDeltOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.VilkarsVurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.operator.Eller.Companion.minstEn
import org.springframework.stereotype.Service

@Service
class SammenstiltVilkarsvurdering {
    fun vilkarsvurder(
        snapshot: OmsorgsarbeidSnapshot,
        relaterteSnapshot: List<OmsorgsarbeidSnapshot> = listOf()
    ): VilkarsVurdering<*> {
        val omsorgsAr = snapshot.omsorgsAr
        val omsorgsyter = snapshot.omsorgsyter
        val omsorgsmottakere = snapshot.getOmsorgsmottakere(omsorgsyter)

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
                    andreParter = relaterteSnapshot.createAndreParter(it),
                )
            )
        }

    }
}

private fun List<OmsorgsarbeidSnapshot>.createAndreParter(omsorgsmottaker: Person) =
    map {
        AnnenPart(
            omsorgsyter = it.omsorgsyter,
            omsorgsArbeid50Prosent = it.getOmsorgsarbeidPerioderForRelevanteAr(
                it.omsorgsyter,
                omsorgsmottaker,
                prosent = 50
            ),
            harInvilgetOmsorgForUrelaterBarn = false, // TODO
        )
    }.filter { it.omsorgsArbeid50Prosent.isNotEmpty() }