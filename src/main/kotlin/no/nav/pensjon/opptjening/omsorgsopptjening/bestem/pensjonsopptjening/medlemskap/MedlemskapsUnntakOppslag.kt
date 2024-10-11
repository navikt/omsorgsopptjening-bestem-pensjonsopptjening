package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.medlemskap

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Medlemskapsunntak
import java.time.YearMonth

interface MedlemskapsUnntakOppslag {

    /**
     * Henter perioder fra [MEDL](https://navno.sharepoint.com/sites/fag-og-ytelser-fagsystemer/SitePages/MEDL%20(Medlemskapsregisteret).aspx)
     */
    fun hentUnntaksperioder(
        fnr: String,
        fraOgMed: YearMonth,
        tilOgMed: YearMonth,
    ): Medlemskapsunntak
}