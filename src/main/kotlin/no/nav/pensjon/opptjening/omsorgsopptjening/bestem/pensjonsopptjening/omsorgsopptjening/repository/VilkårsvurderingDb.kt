package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.*
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.VilkårsvurderingDb.Eller
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.VilkårsvurderingDb.FullOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.VilkårsvurderingDb.Og
import java.util.LinkedList
import java.util.Queue

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(
        value = VilkårsvurderingDb.FullOmsorgForBarnUnder6::class,
        name = "FullOmsorgForBarnUnder6",
    ),
    JsonSubTypes.Type(
        value = VilkårsvurderingDb.OmsorgsyterOver16Ar::class,
        name = "OmsorgsyterOver16Ar",
    ),
    JsonSubTypes.Type(
        value = VilkårsvurderingDb.OmsorgsyterUnder70Ar::class,
        name = "OmsorgsyterUnder70Ar",
    ),
    JsonSubTypes.Type(
        value = VilkårsvurderingDb.Eller::class,
        name = "Eller",
    ),
    JsonSubTypes.Type(
        value = VilkårsvurderingDb.Og::class,
        name = "Og",
    ),
)
sealed class VilkårsvurderingDb {
    internal data class FullOmsorgForBarnUnder6(
        val vilkar: String,
        val grunnlag: GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    internal data class OmsorgsyterOver16Ar(
        val vilkar: String,
        val grunnlag: GrunnlagVilkårsvurderingDb.OmsorgsyterOgOmsorgsÅr,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    internal data class OmsorgsyterUnder70Ar(
        val vilkar: String,
        val grunnlag: GrunnlagVilkårsvurderingDb.OmsorgsyterOgOmsorgsÅr,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    internal data class Eller(
        val eller: List<VilkårsvurderingDb>,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    internal data class Og(
        val og: List<VilkårsvurderingDb>,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    internal data class KanKunGodskrivesEnOmsorgsyter(
        val vilkar: String,
        val grunnlag: GrunnlagVilkårsvurderingDb.KanKunGodskrivesEnOmsorgsyter,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    internal data class KanKunGodskrivesEtBarnPerÅr(
        val vilkar: String,
        val grunnlag: GrunnlagVilkårsvurderingDb.KanKunGodskrivesEtBarnPerÅr,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()
}

internal fun VilkarsVurdering<*>.toDb(): VilkårsvurderingDb {
    return when (this) {
        is EllerVurdering -> {
            Eller(
                eller = mapRecursive(LinkedList(grunnlag), emptyList()),
                utfall = utfall.toDb()
            )
        }

        is FullOmsorgForBarnUnder6Vurdering -> {
            FullOmsorgForBarnUnder6(
                vilkar = vilkar.vilkarsInformasjon.beskrivelse,
                grunnlag = grunnlag.toDb(),
                utfall = utfall.toDb()
            )
        }

        is OgVurdering -> {
            Og(
                og = mapRecursive(LinkedList(grunnlag), emptyList()),
                utfall = utfall.toDb()
            )
        }

        is OmsorgsyterOver16ArVurdering -> {
            VilkårsvurderingDb.OmsorgsyterOver16Ar(
                vilkar = vilkar.vilkarsInformasjon.beskrivelse,
                grunnlag = GrunnlagVilkårsvurderingDb.OmsorgsyterOgOmsorgsÅr(
                    omsorgsyter = grunnlag.omsorgsyter.toDb(),
                    omsorgsAr = grunnlag.omsorgsAr
                ),
                utfall = utfall.toDb()
            )
        }

        is OmsorgsyterUnder70Vurdering -> {
            VilkårsvurderingDb.OmsorgsyterUnder70Ar(
                vilkar = vilkar.vilkarsInformasjon.beskrivelse,
                grunnlag = GrunnlagVilkårsvurderingDb.OmsorgsyterOgOmsorgsÅr(
                    omsorgsyter = grunnlag.omsorgsyter.toDb(),
                    omsorgsAr = grunnlag.omsorgsAr
                ),
                utfall = utfall.toDb()
            )
        }

        is KanKunGodskrivesEnOmsorgsyterVurdering -> {
            VilkårsvurderingDb.KanKunGodskrivesEnOmsorgsyter(
                vilkar = vilkar.vilkarsInformasjon.beskrivelse,
                grunnlag = grunnlag.toDb(),
                utfall = utfall.toDb()
            )
        }

        is KanKunGodskrivesEtBarnPerÅrVurdering -> {
            VilkårsvurderingDb.KanKunGodskrivesEtBarnPerÅr(
                vilkar = vilkar.vilkarsInformasjon.beskrivelse,
                grunnlag = grunnlag.toDb(),
                utfall = utfall.toDb()
            )
        }
    }
}

private fun mapRecursive(
    items: Queue<VilkarsVurdering<*>>,
    result: List<VilkårsvurderingDb>
): List<VilkårsvurderingDb> {
    return if (items.isEmpty()) {
        result
    } else {
        return mapRecursive(items, result + items.poll().toDb())
    }
}

internal fun List<VilkårsvurderingDb>.toDomain(): List<VilkarsVurdering<*>> {
    return map { it.toDomain() }
}

internal fun VilkårsvurderingDb.toDomain(): VilkarsVurdering<*> {
    return when (this) {
        is Eller -> {
            EllerVurdering(
                vilkar = Eller(),
                grunnlag = eller.toDomain(),
                utfall = utfall.toDomain()
            )
        }

        is FullOmsorgForBarnUnder6 -> {
            FullOmsorgForBarnUnder6Vurdering(
                vilkar = FullOmsorgForBarnUnder6(),
                grunnlag = grunnlag.toDomain(),
                utfall = utfall.toDomain()
            )
        }

        is Og -> {
            OgVurdering(
                vilkar = Og(),
                grunnlag = og.toDomain(),
                utfall = utfall.toDomain()
            )
        }

        is VilkårsvurderingDb.OmsorgsyterOver16Ar -> {
            OmsorgsyterUnder70Vurdering(
                vilkar = OmsorgsYterOver16Ar(),
                grunnlag = grunnlag.toDomain(),
                utfall = utfall.toDomain()
            )
        }

        is VilkårsvurderingDb.OmsorgsyterUnder70Ar -> {
            OmsorgsyterUnder70Vurdering(
                vilkar = OmsorgsyterUnder70Ar(),
                grunnlag = grunnlag.toDomain(),
                utfall = utfall.toDomain()
            )
        }

        is VilkårsvurderingDb.KanKunGodskrivesEnOmsorgsyter -> {
            KanKunGodskrivesEnOmsorgsyterVurdering(
                vilkar = KanKunGodskrivesEnOmsorgsyter(),
                grunnlag = grunnlag.toDomain(),
                utfall = utfall.toDomain()
            )
        }
        is VilkårsvurderingDb.KanKunGodskrivesEtBarnPerÅr -> {
            KanKunGodskrivesEtBarnPerÅrVurdering(
                vilkar = KanKunGodskrivesEtBarnPerÅr(),
                grunnlag = grunnlag.toDomain(),
                utfall = utfall.toDomain()

            )
        }
    }
}
