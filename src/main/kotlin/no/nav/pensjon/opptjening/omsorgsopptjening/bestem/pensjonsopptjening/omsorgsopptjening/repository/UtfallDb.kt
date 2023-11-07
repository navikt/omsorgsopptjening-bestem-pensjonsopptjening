package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.BehandlingUtfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.EllerAvslått
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.EllerInnvilget
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OgAvslått
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OgInnvilget
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.VilkårsvurderingUtfall


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
internal sealed class VilkårsvurderingUtfallDb {
    @JsonTypeName("EllerAvslått")
    object EllerAvslått : VilkårsvurderingUtfallDb()

    @JsonTypeName("EllerInnvilget")
    object EllerInnvilget : VilkårsvurderingUtfallDb()

    @JsonTypeName("OgAvslått")
    object OgAvslått : VilkårsvurderingUtfallDb()

    @JsonTypeName("OgInnvilget")
    object OgInnvilget : VilkårsvurderingUtfallDb()

    @JsonTypeName("VilkårAvslag")
    data class VilkårAvslag(val henvisning: Set<JuridiskHenvisningDb>) : VilkårsvurderingUtfallDb()

    @JsonTypeName("VilkårInnvilget")
    data class VilkårInnvilget(val henvisning: Set<JuridiskHenvisningDb>) : VilkårsvurderingUtfallDb()

    @JsonTypeName("Manuell")
    data class Manuell(val henvisning: Set<JuridiskHenvisningDb>) : VilkårsvurderingUtfallDb()
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
internal sealed class BehandlingsutfallDb {

    @JsonTypeName("Avslag")
    data object Avslag : BehandlingsutfallDb()

    @JsonTypeName("Innvilget")
    data object Innvilget : BehandlingsutfallDb()

    @JsonTypeName("Manuell")
    data object Manuell : BehandlingsutfallDb()
}

internal fun BehandlingUtfall.toDb(): BehandlingsutfallDb {
    return when (this) {
        BehandlingUtfall.Avslag -> {
            BehandlingsutfallDb.Avslag
        }

        BehandlingUtfall.Innvilget -> {
            BehandlingsutfallDb.Innvilget
        }

        BehandlingUtfall.Manuell -> {
            BehandlingsutfallDb.Manuell
        }
    }
}


internal fun BehandlingsutfallDb.toDomain(): BehandlingUtfall {
    return when (this) {
        is BehandlingsutfallDb.Innvilget -> {
            BehandlingUtfall.Innvilget
        }

        is BehandlingsutfallDb.Avslag -> {
            BehandlingUtfall.Avslag
        }

        is BehandlingsutfallDb.Manuell -> {
            BehandlingUtfall.Manuell
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

        is VilkårsvurderingUtfall.Ubestemt -> {
            VilkårsvurderingUtfallDb.Manuell(henvisning = henvisninger.toDb())
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

        is VilkårsvurderingUtfallDb.Manuell -> {
            VilkårsvurderingUtfall.Ubestemt(henvisninger = henvisning.toDomain())
        }
    }
}