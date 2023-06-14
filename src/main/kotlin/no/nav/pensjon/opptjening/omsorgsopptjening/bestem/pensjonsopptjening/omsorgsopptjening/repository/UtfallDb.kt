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
        value = VilkårsvurderingUtfallDb.FullOmsorgForBarnUnder6Avslag::class,
        name = "FullOmsorgForBarnUnder6Avslag",
    ),
    JsonSubTypes.Type(
        value = VilkårsvurderingUtfallDb.FullOmsorgForBarnUnder6Innvilget::class,
        name = "FullOmsorgForBarnUnder6Innvilget",
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
        value = VilkårsvurderingUtfallDb.OmsorgsyterOver16ArAvslag::class,
        name = "OmsorgsyterOver16ArAvslag",
    ),
    JsonSubTypes.Type(
        value = VilkårsvurderingUtfallDb.OmsorgsyterOver16ArInnvilget::class,
        name = "OmsorgsyterOver16ArInnvilget",
    ),
    JsonSubTypes.Type(
        value = VilkårsvurderingUtfallDb.OmsorgsyterUnder70ArAvslag::class,
        name = "OmsorgsyterUnder70ArAvslag",
    ),
    JsonSubTypes.Type(
        value = VilkårsvurderingUtfallDb.OmsorgsyterUnder70ArInnvilget::class,
        name = "OmsorgsyterUnder70ArInnvilget",
    ),
    JsonSubTypes.Type(
        value = VilkårsvurderingUtfallDb.KanKunGodskrivesEnOmsorgsyterAvslag::class,
        name = "KanKunGodskrivesEnOmsorgsyterAvslag",
    ),
    JsonSubTypes.Type(
        value = VilkårsvurderingUtfallDb.KanKunGodskrivesEnOmsorgsyterInnvilget::class,
        name = "KanKunGodskrivesEnOmsorgsyterInnvilget",
    ),
    JsonSubTypes.Type(
        value = VilkårsvurderingUtfallDb.KanKunGodskrivesEtBarnPerÅrAvslag::class,
        name = "KanKunGodskrivesEtBarnPerÅrAvslag",
    ),
    JsonSubTypes.Type(
        value = VilkårsvurderingUtfallDb.KanKunGodskrivesEtBarnPerÅrInnvilget::class,
        name = "KanKunGodskrivesEtBarnPerÅrInnvilget",
    ),
)
internal sealed class VilkårsvurderingUtfallDb {
    data class EllerAvslått(
        val årsaker: List<AvslagÅrsakDb>
    ) : VilkårsvurderingUtfallDb()

    data class EllerInnvilget(
        val årsak: String
    ) : VilkårsvurderingUtfallDb()

    data class FullOmsorgForBarnUnder6Avslag(
        val årsaker: List<AvslagÅrsakDb>
    ) : VilkårsvurderingUtfallDb()

    data class FullOmsorgForBarnUnder6Innvilget(
        val årsak: String,
        val omsorgsmottaker: PersonMedFødselsårDb
    ) : VilkårsvurderingUtfallDb()

    data class OgAvslått(
        val årsaker: List<AvslagÅrsakDb>
    ) : VilkårsvurderingUtfallDb()

    data class OgInnvilget(
        val årsak: String
    ) : VilkårsvurderingUtfallDb()

    data class OmsorgsyterOver16ArAvslag(
        val årsaker: List<AvslagÅrsakDb>
    ) : VilkårsvurderingUtfallDb()

    data class OmsorgsyterOver16ArInnvilget(
        val årsak: String
    ) : VilkårsvurderingUtfallDb()

    data class OmsorgsyterUnder70ArAvslag(
        val årsaker: List<AvslagÅrsakDb>
    ) : VilkårsvurderingUtfallDb()

    data class OmsorgsyterUnder70ArInnvilget(
        val årsak: String
    ) : VilkårsvurderingUtfallDb()

    data class KanKunGodskrivesEnOmsorgsyterAvslag(
        val årsaker: List<AvslagÅrsakDb>
    ) : VilkårsvurderingUtfallDb()

    data class KanKunGodskrivesEnOmsorgsyterInnvilget(
        val årsak: String
    ) : VilkårsvurderingUtfallDb()

    data class KanKunGodskrivesEtBarnPerÅrAvslag(
        val årsaker: List<AvslagÅrsakDb>
    ) : VilkårsvurderingUtfallDb()

    data class KanKunGodskrivesEtBarnPerÅrInnvilget(
        val årsak: String
    ) : VilkårsvurderingUtfallDb()
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
        val årsaker: List<AvslagÅrsakDb>,
        val omsorgsmottaker: PersonMedFødselsårDb,
    ) : BehandlingsutfallDb()

    data class AutomatiskGodskrivingInnvilget(
        val årsak: String,
        val omsorgsmottaker: PersonMedFødselsårDb
    ) : BehandlingsutfallDb()
}

internal fun BehandlingUtfall.toDb(): BehandlingsutfallDb {
    return when (this) {
        is AutomatiskGodskrivingUtfall.Avslag -> {
            BehandlingsutfallDb.AutomatiskGodskrivingAvslag(
                årsaker = årsaker.toDb(),
                omsorgsmottaker = omsorgsmottaker.toDb()
            )
        }

        is AutomatiskGodskrivingUtfall.Innvilget -> {
            BehandlingsutfallDb.AutomatiskGodskrivingInnvilget(
                årsak = "",
                omsorgsmottaker = omsorgsmottaker.toDb()
            )
        }
    }
}

internal fun VilkårsvurderingUtfall.toDb(): VilkårsvurderingUtfallDb {
    return when (this) {
        is EllerAvslått -> {
            VilkårsvurderingUtfallDb.EllerAvslått(årsaker = årsaker.toDb())
        }

        is EllerInnvilget -> {
            VilkårsvurderingUtfallDb.EllerInnvilget(årsak = årsak)
        }

        is FullOmsorgForBarnUnder6Avslag -> {
            VilkårsvurderingUtfallDb.FullOmsorgForBarnUnder6Avslag(årsaker = årsaker.toDb())
        }

        is FullOmsorgForBarnUnder6Innvilget -> {
            VilkårsvurderingUtfallDb.FullOmsorgForBarnUnder6Innvilget(
                årsak = årsak,
                omsorgsmottaker = omsorgsmottaker.toDb()
            )
        }

        is OgAvslått -> {
            VilkårsvurderingUtfallDb.OgAvslått(årsaker = årsaker.toDb())
        }

        is OgInnvilget -> {
            VilkårsvurderingUtfallDb.OgInnvilget(årsak = årsak)
        }

        is OmsorgsyterOver16ArAvslag -> {
            VilkårsvurderingUtfallDb.OmsorgsyterOver16ArAvslag(årsaker = årsaker.toDb())
        }

        is OmsorgsyterOver16ArInnvilget -> {
            VilkårsvurderingUtfallDb.OmsorgsyterOver16ArInnvilget(årsak = årsak)
        }

        is OmsorgsyterUnder70ArAvslag -> {
            VilkårsvurderingUtfallDb.OmsorgsyterUnder70ArAvslag(årsaker = årsaker.toDb())
        }

        is OmsorgsyterUnder70ArInnvilget -> {
            VilkårsvurderingUtfallDb.OmsorgsyterUnder70ArInnvilget(årsak = årsak)
        }

        is KanKunGodskrivesEnOmsorgsyterAvslag -> {
            VilkårsvurderingUtfallDb.KanKunGodskrivesEnOmsorgsyterAvslag(årsaker = årsaker.toDb())
        }

        is KanKunGodskrivesEnOmsorgsyterInnvilget -> {
            VilkårsvurderingUtfallDb.KanKunGodskrivesEnOmsorgsyterInnvilget(årsak = årsak)
        }

        is KanKunGodskrivesEtBarnPerÅrAvslag -> {
            VilkårsvurderingUtfallDb.KanKunGodskrivesEtBarnPerÅrAvslag(årsaker = årsaker.toDb())
        }

        is KanKunGodskrivesEtBarnPerÅrInnvilget -> {
            VilkårsvurderingUtfallDb.KanKunGodskrivesEtBarnPerÅrInnvilget(årsak = årsak)
        }
    }
}

internal fun VilkårsvurderingUtfallDb.toDomain(): VilkårsvurderingUtfall {
    return when (this) {
        is VilkårsvurderingUtfallDb.EllerAvslått -> {
            EllerAvslått(årsaker = årsaker.toDomain())
        }

        is VilkårsvurderingUtfallDb.EllerInnvilget -> {
            EllerInnvilget(årsak = årsak)
        }

        is VilkårsvurderingUtfallDb.FullOmsorgForBarnUnder6Avslag -> {
            FullOmsorgForBarnUnder6Avslag(årsaker = årsaker.toDomain())
        }

        is VilkårsvurderingUtfallDb.FullOmsorgForBarnUnder6Innvilget -> {
            FullOmsorgForBarnUnder6Innvilget(årsak = årsak, omsorgsmottaker = omsorgsmottaker.toDomain())
        }

        is VilkårsvurderingUtfallDb.OgAvslått -> {
            OgAvslått(årsaker = årsaker.toDomain())
        }

        is VilkårsvurderingUtfallDb.OgInnvilget -> {
            OgInnvilget(årsak = årsak)
        }

        is VilkårsvurderingUtfallDb.OmsorgsyterOver16ArAvslag -> {
            OmsorgsyterOver16ArAvslag(årsaker = årsaker.toDomain())
        }

        is VilkårsvurderingUtfallDb.OmsorgsyterOver16ArInnvilget -> {
            OmsorgsyterOver16ArInnvilget(årsak = årsak)
        }

        is VilkårsvurderingUtfallDb.OmsorgsyterUnder70ArAvslag -> {
            OmsorgsyterUnder70ArAvslag(årsaker = årsaker.toDomain())
        }

        is VilkårsvurderingUtfallDb.OmsorgsyterUnder70ArInnvilget -> {
            OmsorgsyterUnder70ArInnvilget(årsak = årsak)
        }

        is VilkårsvurderingUtfallDb.KanKunGodskrivesEnOmsorgsyterAvslag -> {
            KanKunGodskrivesEnOmsorgsyterAvslag(årsaker = årsaker.toDomain())
        }

        is VilkårsvurderingUtfallDb.KanKunGodskrivesEnOmsorgsyterInnvilget -> {
            KanKunGodskrivesEnOmsorgsyterInnvilget(årsak = årsak)
        }

        is VilkårsvurderingUtfallDb.KanKunGodskrivesEtBarnPerÅrAvslag -> {
            KanKunGodskrivesEtBarnPerÅrAvslag(årsaker = årsaker.toDomain())
        }

        is VilkårsvurderingUtfallDb.KanKunGodskrivesEtBarnPerÅrInnvilget -> {
            KanKunGodskrivesEtBarnPerÅrInnvilget(årsak = årsak)
        }
    }
}

internal fun BehandlingsutfallDb.toDomain(): BehandlingUtfall {
    return when (this) {
        is BehandlingsutfallDb.AutomatiskGodskrivingAvslag -> {
            AutomatiskGodskrivingUtfall.Avslag(
                omsorgsmottaker = omsorgsmottaker.toDomain(),
                årsaker = årsaker.toDomain()
            )

        }

        is BehandlingsutfallDb.AutomatiskGodskrivingInnvilget -> {
            AutomatiskGodskrivingUtfall.Innvilget(
                omsorgsmottaker = omsorgsmottaker.toDomain()
            )
        }
    }
}
