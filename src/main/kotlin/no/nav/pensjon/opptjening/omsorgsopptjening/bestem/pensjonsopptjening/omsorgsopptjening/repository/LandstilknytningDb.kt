package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Landstilknytning

enum class LandstilknytningDb {
    EØS_NORGE_PRIMÆR,
    EØS_NORGE_SEKUNDÆR,
    NORGE,
}

internal fun Landstilknytning.toDb(): LandstilknytningDb {
    return when(this){
        is Landstilknytning.Eøs.NorgePrimærland -> LandstilknytningDb.EØS_NORGE_PRIMÆR
        is Landstilknytning.Eøs.NorgeSekundærland -> LandstilknytningDb.EØS_NORGE_SEKUNDÆR
        is Landstilknytning.Norge -> LandstilknytningDb.NORGE
    }
}

internal fun LandstilknytningDb.toDomain(): Landstilknytning {
    return when(this){
        LandstilknytningDb.EØS_NORGE_PRIMÆR -> Landstilknytning.Eøs.NorgePrimærland
        LandstilknytningDb.EØS_NORGE_SEKUNDÆR -> Landstilknytning.Eøs.NorgeSekundærland
        LandstilknytningDb.NORGE -> Landstilknytning.Norge
    }
}