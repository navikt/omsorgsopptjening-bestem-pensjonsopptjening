package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Landstilknytning

enum class LandstilknytningDb {
    EØS_UKJENT_PRIMÆR_SEKUNDÆR_LAND,
    EØS_NORGE_SEKUNDÆR,
    NORGE,
}

internal fun Landstilknytning.toDb(): LandstilknytningDb {
    return when(this){
        is Landstilknytning.Eøs.UkjentPrimærOgSekundærLand -> LandstilknytningDb.EØS_UKJENT_PRIMÆR_SEKUNDÆR_LAND
        is Landstilknytning.Eøs.NorgeSekundærland -> LandstilknytningDb.EØS_NORGE_SEKUNDÆR
        is Landstilknytning.Norge -> LandstilknytningDb.NORGE
    }
}

internal fun LandstilknytningDb.toDomain(): Landstilknytning {
    return when(this){
        LandstilknytningDb.EØS_UKJENT_PRIMÆR_SEKUNDÆR_LAND -> Landstilknytning.Eøs.UkjentPrimærOgSekundærLand
        LandstilknytningDb.EØS_NORGE_SEKUNDÆR -> Landstilknytning.Eøs.NorgeSekundærland
        LandstilknytningDb.NORGE -> Landstilknytning.Norge
    }
}