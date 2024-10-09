package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.medlemskap

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Medlemskapsgrunnlag
import java.time.YearMonth

interface MedlemskapOppslag {
    fun hentMedlemskapsgrunnlag(
        fnr: String,
        fraOgMed: YearMonth,
        tilOgMed: YearMonth,
    ): Medlemskapsgrunnlag
}