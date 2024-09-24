package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Ident
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.IdentHistorikk

internal data class IdentDb(
    val ident: String,
    val gjeldende: Boolean
)

internal fun List<IdentDb>.toDomain(): IdentHistorikk {
    return IdentHistorikk(map { it.toDomain() }.filterIsInstance<Ident.FolkeregisterIdent>().toSet())
}

internal fun IdentDb.toDomain(): Ident {
    return when {
        ident != Ident.IDENT_UKJENT && gjeldende -> {
            Ident.FolkeregisterIdent.Gjeldende(ident)
        }

        ident != Ident.IDENT_UKJENT && !gjeldende -> {
            Ident.FolkeregisterIdent.Historisk(ident)
        }

        else -> {
            Ident.Ukjent
        }
    }
}

internal fun IdentHistorikk.toDb(): List<IdentDb> {
    return historikk().map { it.toDb() }
}

internal fun Ident.toDb(): IdentDb {
    return when (this) {
        is Ident.FolkeregisterIdent.Gjeldende -> {
            IdentDb(ident = ident, gjeldende = true)
        }

        is Ident.FolkeregisterIdent.Historisk -> {
            IdentDb(ident = ident, gjeldende = false)
        }

        Ident.Ukjent -> {
            IdentDb(ident = Ident.IDENT_UKJENT, gjeldende = false)
        }
    }
}