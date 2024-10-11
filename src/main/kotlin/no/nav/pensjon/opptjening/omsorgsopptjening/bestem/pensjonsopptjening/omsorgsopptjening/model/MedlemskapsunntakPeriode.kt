package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
import java.time.YearMonth

data class MedlemskapsunntakPeriode(
    val fraOgMed: YearMonth,
    val tilOgMed: YearMonth,
) {
    val periode = Periode(fraOgMed, tilOgMed)
}
