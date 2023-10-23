package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Landstilknytning as LandstilknytningKafka

sealed class Landstilknytning {
    sealed class Eøs : Landstilknytning() {
        /**
         * Norge er hovedansvarlig for utbetaling av ytelse og utbetaling skjer på samme måte som for [Norge]
         */
        object NorgePrimærland : Eøs()

        /**
         * Norge er medansvarlig for utbetaling av ytelse og utbetaler differansen mellom annet lands ytelse og
         * ytelse man har rett til i Norge dersom denne er større. Dersom annet lands ytelse er større enn eller lik
         * Norges ytelse utbetales det ingenting fra Norge.
         */
        object NorgeSekundærland : Eøs()
    }

    /**
     * Norge er hovedansvarlig for utbetaling av yelse.
     */
    object Norge : Landstilknytning()
}

internal fun LandstilknytningKafka.toDomain(): Landstilknytning {
    return when (this) {
        LandstilknytningKafka.EØS_NORGE_PRIMÆR -> Landstilknytning.Eøs.NorgePrimærland
        LandstilknytningKafka.EØS_NORGE_SEKUNDÆR -> Landstilknytning.Eøs.NorgeSekundærland
        LandstilknytningKafka.NORGE -> Landstilknytning.Norge
    }
}