package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidSnapshot
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.DeltOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.FullOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.OmsorgsgiverOver16Ar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.OmsorgsgiverUnder70Ar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag.AnnenPart
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag.GrunnlagDeltOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag.GrunnlagOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag.OmsorgsGiverOgOmsorgsAr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.VilkarsVurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.operator.Eller.Companion.eller
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.operator.Eller.Companion.minstEn
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.operator.Og.Companion.og
import org.springframework.stereotype.Service

@Service
class VilkarsvurderingService {

    fun vilkarsvurder(
        snapshot: OmsorgsarbeidSnapshot,
        relaterteSnapshot: List<OmsorgsarbeidSnapshot> = listOf()
    ): VilkarsVurdering<*> {
        val omsorgsAr = snapshot.omsorgsAr
        val omsorgsyter = snapshot.omsorgsyter

        val omsorgsmottakere = snapshot.getOmsorgsmottakere(omsorgsyter)

        return og(
            OmsorgsgiverOver16Ar().vilkarsVurder(
                OmsorgsGiverOgOmsorgsAr(
                    omsorgsAr = omsorgsAr,
                    omsorgsgiver = omsorgsyter
                )
            ),
            OmsorgsgiverUnder70Ar().vilkarsVurder(
                OmsorgsGiverOgOmsorgsAr(
                    omsorgsAr = omsorgsAr,
                    omsorgsgiver = omsorgsyter
                )
            ),
            eller(
                omsorgsmottakere.minstEn {
                    FullOmsorgForBarnUnder6().vilkarsVurder(
                        GrunnlagOmsorgForBarnUnder6(
                            omsorgsAr = omsorgsAr,
                            omsorgsmottaker = it,
                            omsorgsArbeid100Prosent = snapshot.getOmsorgsarbeidPerioder(omsorgsyter, it, prosent = 100),
                        )
                    )
                },
                omsorgsmottakere.minstEn {
                    DeltOmsorgForBarnUnder6().vilkarsVurder(
                        GrunnlagDeltOmsorgForBarnUnder6(
                            omsorgsAr = omsorgsAr,
                            omsorgsyter = snapshot.omsorgsyter,
                            omsorgsmottaker = it,
                            omsorgsArbeid50Prosent = snapshot.getOmsorgsarbeidPerioder(omsorgsyter, it, prosent = 50),
                            andreParter = relaterteSnapshot.createAndreParter(it),
                        )
                    )
                }
            )
        )
    }
}

private fun List<OmsorgsarbeidSnapshot>.createAndreParter(omsorgsmottaker: Person) =
    map {
        AnnenPart(
            omsorgsyter = it.omsorgsyter,
            omsorgsArbeid50Prosent = it.getOmsorgsarbeidPerioder(it.omsorgsyter, omsorgsmottaker, prosent = 50),
            harInvilgetOmsorgForUrelaterBarn = false, // TODO
        )
    }.filter { it.omsorgsArbeid50Prosent.isNotEmpty() }

//TODO Hent ut getOmsorgsarbeidPerioder for gitt år