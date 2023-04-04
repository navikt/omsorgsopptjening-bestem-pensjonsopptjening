package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.getAntallUtbetalingMoneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag.GrunnlagOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Utfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Vilkar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.VilkarsInformasjon

/**
 * For barn fra 1 til og med 5 år må omsorgsgiver minst ha 7 måneder med omsorgsarbeid for barnet
 *
 * For barn som ikke har fylt ett år kreves ikke 7 måneder for å oppnå omsorgsopptjening
 *
 * Barn som ikke har fylt ett år og er født i desember vil ikke ha utbetalt barnetrygd og har ikke omsorgsarbeid for året.
 * De har alikevel rett til full omsorgsopptjening det første året.
 * Det betyr at vi må sjekke om omsorgsgiver har fått barnetrygd i året etter for å vite om omsorgsyter har rett til omsorgsopptjening
 *
 */
class FullOmsorgForBarnUnder6 : Vilkar<GrunnlagOmsorgForBarnUnder6>(
    vilkarsInformasjon = VilkarsInformasjon(
        beskrivelse = "Medlemmet har minst halve året hatt den daglige omsorgen for et barn",
        begrunnesleForAvslag = "Medlemmet har ikke et halve år med daglig omsorgen for et barn",
        begrunnelseForInnvilgelse = "Medlemmet har et halve år med daglig omsorgen for et barn",
    ),
    utfallsFunksjon = `Minst 7 moneder omsorg for barn under 6 ar`,
) {
    companion object {
        private val `Minst 7 moneder omsorg for barn under 6 ar` = fun(grunnlag: GrunnlagOmsorgForBarnUnder6) =
            grunnlag.run {
                if (sjuMonederDeltOmsorg(ar = omsorgsAr) && alderMottaker(mellom = 0..5)) {
                    Utfall.INVILGET
                } else if (enMonedDeltOmsorg(ar = omsorgsAr) && alderMottaker(mellom = 0..0)) {
                    Utfall.INVILGET
                } else if (enMonedDeltOmsorg(ar = omsorgsAr + 1) && alderMottaker(mellom = 0..0)) {
                    Utfall.INVILGET
                } else {
                    Utfall.AVSLAG
                }
            }


        private fun GrunnlagOmsorgForBarnUnder6.sjuMonederDeltOmsorg(ar: Int) =
            omsorgsArbeid100Prosent.getAntallUtbetalingMoneder(ar) >= 7

        private fun GrunnlagOmsorgForBarnUnder6.enMonedDeltOmsorg(ar: Int) =
            omsorgsArbeid100Prosent.getAntallUtbetalingMoneder(ar) >= 1

        private fun GrunnlagOmsorgForBarnUnder6.alderMottaker(mellom: IntRange) =
            (omsorgsAr - omsorgsmottaker.fodselsAr) in mellom
    }
}
