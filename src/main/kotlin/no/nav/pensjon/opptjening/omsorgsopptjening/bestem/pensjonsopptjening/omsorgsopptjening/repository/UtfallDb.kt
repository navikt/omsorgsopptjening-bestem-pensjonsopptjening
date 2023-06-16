package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.*


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(
        value = VilkårsvurderingUtfallDb.EllerAvslått::class,
        name = "EllerAvslått",
    ),
    JsonSubTypes.Type(
        value = VilkårsvurderingUtfallDb.EllerInnvilget::class,
        name = "EllerInnvilget",
    ),
    JsonSubTypes.Type(
        value = VilkårsvurderingUtfallDb.OgAvslått::class,
        name = "OgAvslått",
    ),
    JsonSubTypes.Type(
        value = VilkårsvurderingUtfallDb.OgInnvilget::class,
        name = "OgInnvilget",
    ),
    JsonSubTypes.Type(
        value = VilkårsvurderingUtfallDb.EnkeltParagrafAvslag::class,
        name = "EnkeltParagrafAvslag",
    ),
    JsonSubTypes.Type(
        value = VilkårsvurderingUtfallDb.EnkeltParagrafInnvilget::class,
        name = "EnkeltParagrafInnvilget",
    ),
)
internal sealed class VilkårsvurderingUtfallDb {
    object EllerAvslått : VilkårsvurderingUtfallDb()

    object EllerInnvilget : VilkårsvurderingUtfallDb()

    object OgAvslått : VilkårsvurderingUtfallDb()

    object OgInnvilget : VilkårsvurderingUtfallDb()

    data class EnkeltParagrafInnvilget(val paragraf: Set<LovhenvisningDb>) : VilkårsvurderingUtfallDb()

    data class EnkeltParagrafAvslag(val paragraf: Set<LovhenvisningDb>) : VilkårsvurderingUtfallDb()
}

data class BehandlingsoppsummeringDb(
    val paragrafOppsummering: List<Pair<LovhenvisningDb, Boolean>>
)

internal fun Behandlingsoppsummering.toDb(): BehandlingsoppsummeringDb {
    return BehandlingsoppsummeringDb(paragrafOppsummering.map { it.paragrafUtfall.first.toDb() to it.paragrafUtfall.second })
}

internal fun BehandlingsoppsummeringDb.toDomain(): Behandlingsoppsummering {
    return Behandlingsoppsummering(paragrafOppsummering = paragrafOppsummering.map { it.first.toDomain() to it.second }
        .map { ParagrafOppsummering(it) })
}


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(
        value = BehandlingsutfallDb.AutomatiskGodskrivingAvslag::class,
        name = "AutomatiskGodskrivingAvslag",
    ),
    JsonSubTypes.Type(
        value = BehandlingsutfallDb.AutomatiskGodskrivingInnvilget::class,
        name = "AutomatiskGodskrivingInnvilget",
    )
)
internal sealed class BehandlingsutfallDb {
    data class AutomatiskGodskrivingAvslag(
        val oppsummering: BehandlingsoppsummeringDb,
    ) : BehandlingsutfallDb()

    data class AutomatiskGodskrivingInnvilget(
        val oppsummering: BehandlingsoppsummeringDb,
    ) : BehandlingsutfallDb()
}

internal fun BehandlingUtfall.toDb(): BehandlingsutfallDb {
    return when (this) {
        is AutomatiskGodskrivingUtfall.Avslag -> {
            BehandlingsutfallDb.AutomatiskGodskrivingAvslag(oppsummering = oppsummering.toDb())
        }

        is AutomatiskGodskrivingUtfall.Innvilget -> {
            BehandlingsutfallDb.AutomatiskGodskrivingInnvilget(oppsummering = oppsummering.toDb())
        }
    }
}

internal fun VilkårsvurderingUtfall.toDb(): VilkårsvurderingUtfallDb {
    return when (this) {
        is EllerAvslått -> {
            VilkårsvurderingUtfallDb.EllerAvslått
        }

        is EllerInnvilget -> {
            VilkårsvurderingUtfallDb.EllerInnvilget
        }

        is OgAvslått -> {
            VilkårsvurderingUtfallDb.OgAvslått
        }

        is OgInnvilget -> {
            VilkårsvurderingUtfallDb.OgInnvilget
        }

        is VilkårsvurderingUtfall.Innvilget.EnkeltParagraf -> {
            VilkårsvurderingUtfallDb.EnkeltParagrafInnvilget(paragraf = lovhenvisning.toDb())
        }

        is VilkårsvurderingUtfall.Avslag.EnkeltParagraf -> {
            VilkårsvurderingUtfallDb.EnkeltParagrafAvslag(paragraf = lovhenvisning.toDb())
        }
    }
}

internal fun VilkårsvurderingUtfallDb.toDomain(): VilkårsvurderingUtfall {
    return when (this) {
        is VilkårsvurderingUtfallDb.EllerAvslått -> {
            EllerAvslått
        }

        is VilkårsvurderingUtfallDb.EllerInnvilget -> {
            EllerInnvilget
        }

        is VilkårsvurderingUtfallDb.OgAvslått -> {
            OgAvslått
        }

        is VilkårsvurderingUtfallDb.OgInnvilget -> {
            OgInnvilget
        }

        is VilkårsvurderingUtfallDb.EnkeltParagrafAvslag -> {
            VilkårsvurderingUtfall.Avslag.EnkeltParagraf(lovhenvisning = paragraf.toDomain())
        }

        is VilkårsvurderingUtfallDb.EnkeltParagrafInnvilget -> {
            VilkårsvurderingUtfall.Innvilget.EnkeltParagraf(lovhenvisning = paragraf.toDomain())
        }
    }
}

internal fun BehandlingsutfallDb.toDomain(): BehandlingUtfall {
    return when (this) {
        is BehandlingsutfallDb.AutomatiskGodskrivingAvslag -> {
            AutomatiskGodskrivingUtfall.Avslag(oppsummering.toDomain())

        }

        is BehandlingsutfallDb.AutomatiskGodskrivingInnvilget -> {
            AutomatiskGodskrivingUtfall.Innvilget(oppsummering.toDomain())
        }
    }
}
