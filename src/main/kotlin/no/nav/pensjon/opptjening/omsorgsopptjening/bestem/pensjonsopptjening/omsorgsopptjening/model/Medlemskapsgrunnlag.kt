package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
import java.time.YearMonth

data class Medlemskapsgrunnlag(
    val unntaksperioder: List<Unntaksperiode>,
    val rådata: String,
) {
    fun avgrensForÅr(år: Int): Medlemskapsgrunnlag {
        return Medlemskapsgrunnlag(
            unntaksperioder = unntaksperioder
                .filter { it.periode.overlapper(år) }
                .map { unntaksperiode ->
                    unntaksperiode.periode.overlappendeMåneder(år)
                        .let {
                            Unntaksperiode(
                                fraOgMed = it.min(),
                                tilOgMed = it.max()
                            )
                        }
                },
            rådata = rådata,
        )
    }

    data class Unntaksperiode(
        val fraOgMed: YearMonth,
        val tilOgMed: YearMonth,
    ) {
        val periode = Periode(fraOgMed, tilOgMed)
    }
}
