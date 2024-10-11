package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

data class Medlemskapsunntak(
    val ikkeMedlem: Set<MedlemskapsunntakPeriode>,
    val pliktigEllerFrivillig: Set<MedlemskapsunntakPeriode>,
    val rådata: String,
) {
    fun avgrensForÅr(år: Int): Medlemskapsunntak {
        return Medlemskapsunntak(
            ikkeMedlem = ikkeMedlem
                .filter { it.periode.overlapper(år) }
                .map { unntaksperiode ->
                    unntaksperiode.periode.overlappendeMåneder(år)
                        .let {
                            MedlemskapsunntakPeriode(
                                fraOgMed = it.min(),
                                tilOgMed = it.max()
                            )
                        }
                }
                .toSet(),
            pliktigEllerFrivillig = pliktigEllerFrivillig
                .filter { it.periode.overlapper(år) }
                .map { unntaksperiode ->
                    unntaksperiode.periode.overlappendeMåneder(år)
                        .let {
                            MedlemskapsunntakPeriode(
                                fraOgMed = it.min(),
                                tilOgMed = it.max()
                            )
                        }
                }
                .toSet(),
            rådata = rådata,
        )
    }

}