package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.AntallMånederRegel

data class AntallMånederRegelDb(
    val enum: AntallMånederRegelEnum,
    val antallMåneder: Int,
)

enum class AntallMånederRegelEnum {
    FødtIOmsorgsår,
    FødtUtenforOmsorgsår
}

internal fun AntallMånederRegel.toDb(): AntallMånederRegelDb {
    return when (this) {
        AntallMånederRegel.FødtIOmsorgsår -> AntallMånederRegelDb(
            AntallMånederRegelEnum.FødtIOmsorgsår,
            AntallMånederRegel.FødtIOmsorgsår.antall
        )

        AntallMånederRegel.FødtUtenforOmsorgsår -> AntallMånederRegelDb(
            AntallMånederRegelEnum.FødtUtenforOmsorgsår,
            AntallMånederRegel.FødtUtenforOmsorgsår.antall
        )
    }
}

internal fun AntallMånederRegelDb.toDomain(): AntallMånederRegel {
    return when (this.enum) {
        AntallMånederRegelEnum.FødtIOmsorgsår -> AntallMånederRegel.FødtIOmsorgsår
        AntallMånederRegelEnum.FødtUtenforOmsorgsår -> AntallMånederRegel.FødtUtenforOmsorgsår
    }
}