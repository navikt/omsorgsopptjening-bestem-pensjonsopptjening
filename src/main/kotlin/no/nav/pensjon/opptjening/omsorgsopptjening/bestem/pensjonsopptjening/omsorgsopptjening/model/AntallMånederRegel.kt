package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

sealed class AntallMånederRegel {
    abstract val antall: Int

    data object FødtIOmsorgsår : AntallMånederRegel() {
        override val antall: Int get() = 1
    }

    data object FødtUtenforOmsorgsår : AntallMånederRegel() {
        override val antall: Int get() = 6
    }
}

fun Int.oppfyller(antallMånederRegel: AntallMånederRegel): Boolean {
    return this >= antallMånederRegel.antall
}

fun Set<*>.oppfyller(antallMånederRegel: AntallMånederRegel): Boolean {
    return count().oppfyller(antallMånederRegel)
}