package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.ytelse

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Ytelseinformasjon
import java.time.YearMonth

interface YtelseOppslag {
    fun hentLøpendeAlderspensjon(fnr: String, fraOgMed: YearMonth, tilOgMed: YearMonth): Ytelseinformasjon
    fun hentLøpendeUføretrygd(fnr: String, fraOgMed: YearMonth, tilOgMed: YearMonth): Ytelseinformasjon
}