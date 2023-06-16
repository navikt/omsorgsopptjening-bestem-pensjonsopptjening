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
        value = VilkårsvurderingDb.OmsorgsyterFylt17År::class,
        name = "OmsorgsyterFylt17År",
    ),
    JsonSubTypes.Type(
        value = VilkårsvurderingDb.OmsorgsyterIkkeEldreEnn69År::class,
        name = "OmsorgsyterIkkeEldreEnn69År",
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
        val paragrafer: Set<ParagrafDb>,
        val grunnlag: GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    internal data class OmsorgsyterFylt17År(
        val paragrafer: Set<ParagrafDb>,
        val grunnlag: GrunnlagVilkårsvurderingDb.PersonOgOmsorgsÅr,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    internal data class OmsorgsyterIkkeEldreEnn69År(
        val paragrafer: Set<ParagrafDb>,
        val grunnlag: GrunnlagVilkårsvurderingDb.PersonOgOmsorgsÅr,
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
        val paragrafer: Set<ParagrafDb>,
        val grunnlag: GrunnlagVilkårsvurderingDb.KanKunGodskrivesEnOmsorgsyter,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    internal data class KanKunGodskrivesEtBarnPerÅr(
        val paragrafer: Set<ParagrafDb>,
        val grunnlag: GrunnlagVilkårsvurderingDb.KanKunGodskrivesEtBarnPerÅr,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    internal data class OmsorgsmottakerIkkeFylt6Ar(
        val paragrafer: Set<ParagrafDb>,
        val grunnlag: GrunnlagVilkårsvurderingDb.PersonOgOmsorgsÅr,
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
                paragrafer = paragrafer.toDb(),
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

        is OmsorgsyterFylt17ÅrVurdering -> {
            VilkårsvurderingDb.OmsorgsyterFylt17År(
                paragrafer = paragrafer.toDb(),
                grunnlag = grunnlag.toDb(),
                utfall = utfall.toDb()
            )
        }

        is OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårVurdering -> {
            VilkårsvurderingDb.OmsorgsyterIkkeEldreEnn69År(
                paragrafer = paragrafer.toDb(),
                grunnlag = grunnlag.toDb(),
                utfall = utfall.toDb()
            )
        }

        is KanKunGodskrivesEnOmsorgsyterVurdering -> {
            VilkårsvurderingDb.KanKunGodskrivesEnOmsorgsyter(
                paragrafer = paragrafer.toDb(),
                grunnlag = grunnlag.toDb(),
                utfall = utfall.toDb()
            )
        }

        is KanKunGodskrivesEtBarnPerÅrVurdering -> {
            VilkårsvurderingDb.KanKunGodskrivesEtBarnPerÅr(
                paragrafer = paragrafer.toDb(),
                grunnlag = grunnlag.toDb(),
                utfall = utfall.toDb()
            )
        }

        is OmsorgsmottakerIkkeFylt6ArVurdering -> {
            VilkårsvurderingDb.OmsorgsmottakerIkkeFylt6Ar(
                paragrafer = paragrafer.toDb(),
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

enum class ParagrafDb {
    A
}

internal fun Set<Paragraf>.toDb(): Set<ParagrafDb> {
    return map { it.toDb() }.toSet()
}
internal fun Paragraf.toDb(): ParagrafDb {
    return when(this){
        Paragraf.A -> ParagrafDb.A
    }
}

internal fun ParagrafDb.toDomain(): Paragraf {
    return when(this){
        ParagrafDb.A -> Paragraf.A
    }
}

internal fun Set<ParagrafDb>.toDomain(): Set<Paragraf> {
    return map { it.toDomain() }.toSet()
}
internal fun VilkårsvurderingDb.toDomain(): VilkarsVurdering<*> {
    return when (this) {
        is Eller -> {
            EllerVurdering(
                grunnlag = eller.toDomain(),
                utfall = utfall.toDomain()
            )
        }

        is FullOmsorgForBarnUnder6 -> {
            FullOmsorgForBarnUnder6Vurdering(
                paragrafer = paragrafer.toDomain(),
                grunnlag = grunnlag.toDomain(),
                utfall = utfall.toDomain()
            )
        }

        is Og -> {
            OgVurdering(
                grunnlag = og.toDomain(),
                utfall = utfall.toDomain()
            )
        }

        is VilkårsvurderingDb.OmsorgsyterFylt17År -> {
            OmsorgsyterFylt17ÅrVurdering(
                paragrafer = paragrafer.toDomain(),
                grunnlag = grunnlag.toDomain(),
                utfall = utfall.toDomain()
            )
        }

        is VilkårsvurderingDb.OmsorgsyterIkkeEldreEnn69År -> {
            OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårVurdering(
                paragrafer = paragrafer.toDomain(),
                grunnlag = grunnlag.toDomain(),
                utfall = utfall.toDomain()
            )
        }

        is VilkårsvurderingDb.KanKunGodskrivesEnOmsorgsyter -> {
            KanKunGodskrivesEnOmsorgsyterVurdering(
                paragrafer = paragrafer.toDomain(),
                grunnlag = grunnlag.toDomain(),
                utfall = utfall.toDomain()
            )
        }
        is VilkårsvurderingDb.KanKunGodskrivesEtBarnPerÅr -> {
            KanKunGodskrivesEtBarnPerÅrVurdering(
                paragrafer = paragrafer.toDomain(),
                grunnlag = grunnlag.toDomain(),
                utfall = utfall.toDomain()

            )
        }

        is VilkårsvurderingDb.OmsorgsmottakerIkkeFylt6Ar -> {
            OmsorgsmottakerIkkeFylt6ArVurdering(
                paragrafer = paragrafer.toDomain(),
                grunnlag = grunnlag.toDomain(),
                utfall = utfall.toDomain()
            )
        }
    }
}
