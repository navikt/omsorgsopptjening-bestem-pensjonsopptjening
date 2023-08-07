package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.*


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
internal sealed class VilkårsvurderingUtfallDb {
    object EllerAvslått : VilkårsvurderingUtfallDb()

    object EllerInnvilget : VilkårsvurderingUtfallDb()

    object OgAvslått : VilkårsvurderingUtfallDb()

    object OgInnvilget : VilkårsvurderingUtfallDb()
    data class VilkårAvslag(val henvisning: Set<JuridiskHenvisningDb>) : VilkårsvurderingUtfallDb()
    data class VilkårInnvilget(val henvisning: Set<JuridiskHenvisningDb>) : VilkårsvurderingUtfallDb()

}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
internal sealed class BehandlingsutfallDb {

    object AutomatiskGodskrivingAvslag : BehandlingsutfallDb()

    object AutomatiskGodskrivingInnvilget : BehandlingsutfallDb()
}

internal fun BehandlingUtfall.toDb(): BehandlingsutfallDb {
    return when (this) {
        AutomatiskGodskrivingUtfall.Avslag -> {
            BehandlingsutfallDb.AutomatiskGodskrivingAvslag
        }

        AutomatiskGodskrivingUtfall.Innvilget -> {
            BehandlingsutfallDb.AutomatiskGodskrivingInnvilget
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

        is VilkårsvurderingUtfall.Innvilget.Vilkår -> {
            VilkårsvurderingUtfallDb.VilkårInnvilget(henvisning = henvisninger.toDb())
        }

        is VilkårsvurderingUtfall.Avslag.Vilkår -> {
            VilkårsvurderingUtfallDb.VilkårAvslag(henvisning = henvisninger.toDb())
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

        is VilkårsvurderingUtfallDb.VilkårAvslag -> {
            VilkårsvurderingUtfall.Avslag.Vilkår(henvisninger = henvisning.toDomain())
        }

        is VilkårsvurderingUtfallDb.VilkårInnvilget -> {
            VilkårsvurderingUtfall.Innvilget.Vilkår(henvisninger = henvisning.toDomain())
        }
    }
}

internal fun BehandlingsutfallDb.toDomain(): BehandlingUtfall {
    return when (this) {
        is BehandlingsutfallDb.AutomatiskGodskrivingInnvilget -> {
            AutomatiskGodskrivingUtfall.Innvilget
        }

        is BehandlingsutfallDb.AutomatiskGodskrivingAvslag -> {
            AutomatiskGodskrivingUtfall.Avslag
        }
    }
}
