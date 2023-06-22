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
        val paragrafer: Set<HenvisningDb>,
        val grunnlag: GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    internal data class OmsorgsyterFylt17År(
        val paragrafer: Set<HenvisningDb>,
        val grunnlag: GrunnlagVilkårsvurderingDb.PersonOgOmsorgsÅr,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    internal data class OmsorgsyterIkkeEldreEnn69År(
        val paragrafer: Set<HenvisningDb>,
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
        val paragrafer: Set<HenvisningDb>,
        val grunnlag: GrunnlagVilkårsvurderingDb.KanKunGodskrivesEnOmsorgsyter,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    internal data class KanKunGodskrivesEtBarnPerÅr(
        val paragrafer: Set<HenvisningDb>,
        val grunnlag: GrunnlagVilkårsvurderingDb.KanKunGodskrivesEtBarnPerÅr,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    internal data class OmsorgsmottakerIkkeFylt6Ar(
        val paragrafer: Set<HenvisningDb>,
        val grunnlag: GrunnlagVilkårsvurderingDb.PersonOgOmsorgsÅr,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    internal data class LiktAntallMåneder(
        val paragrafer: Set<HenvisningDb>,
        val grunnlag: GrunnlagVilkårsvurderingDb.LiktAntallMåneder,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    internal data class OmsorgBarnUnder6OgIngenLikeMangeMåneder(
        val paragrafer: Set<HenvisningDb>,
        val grunnlag: GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6OgIngenHarLikeMangeMåneder,
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
                paragrafer = henvisninger.toDb(),
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
                paragrafer = henvisninger.toDb(),
                grunnlag = grunnlag.toDb(),
                utfall = utfall.toDb()
            )
        }

        is OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårVurdering -> {
            VilkårsvurderingDb.OmsorgsyterIkkeEldreEnn69År(
                paragrafer = henvisninger.toDb(),
                grunnlag = grunnlag.toDb(),
                utfall = utfall.toDb()
            )
        }

        is KanKunGodskrivesEnOmsorgsyterVurdering -> {
            VilkårsvurderingDb.KanKunGodskrivesEnOmsorgsyter(
                paragrafer = henvisninger.toDb(),
                grunnlag = grunnlag.toDb(),
                utfall = utfall.toDb()
            )
        }

        is KanKunGodskrivesEtBarnPerÅrVurdering -> {
            VilkårsvurderingDb.KanKunGodskrivesEtBarnPerÅr(
                paragrafer = henvisninger.toDb(),
                grunnlag = grunnlag.toDb(),
                utfall = utfall.toDb()
            )
        }

        is OmsorgsmottakerIkkeFylt6ArVurdering -> {
            VilkårsvurderingDb.OmsorgsmottakerIkkeFylt6Ar(
                paragrafer = henvisninger.toDb(),
                grunnlag = grunnlag.toDb(),
                utfall = utfall.toDb()
            )
        }

        is LiktAntallMånederOmsorgVurdering -> {
            VilkårsvurderingDb.LiktAntallMåneder(
                paragrafer = henvisninger.toDb(),
                grunnlag = grunnlag.toDb(),
                utfall = utfall.toDb()
            )
        }

        is FullOmsorgForBarnUnder6OgIngenHarLiktAntallMånederVurdering -> {
            VilkårsvurderingDb.OmsorgBarnUnder6OgIngenLikeMangeMåneder(
                paragrafer = henvisninger.toDb(),
                grunnlag = GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6OgIngenHarLikeMangeMåneder(
                    barnUnder6 = grunnlag.fullOmsorgForBarnUnder6Vurdering.toDb(),
                    likeMangeMåneder = grunnlag.liktAntallMånederOmsorgVurdering.toDb()
                ),
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

enum class HenvisningDb {
    FTRL_K20_P8_L1_Ba_pkt1,
    FTRL_K20_P8_L1_Ba_pkt2,
    FTRL_K20_P8_L1_Ba_pkt3,
    FTRL_K20_P8_L2,
    FOR_OMSORGSPOENG_K3_P4_L1_pkt1,
    FOR_OMSORGSPOENG_K2_P4_L3,
}

internal fun Set<Henvisning>.toDb(): Set<HenvisningDb> {
    return map { it.toDb() }.toSet()
}

internal fun Henvisning.toDb(): HenvisningDb {
    return when (this) {
        is Forskrift -> toDb()
        is Lovparagraf -> toDb()
    }
}

internal fun Forskrift.toDb(): HenvisningDb {
    return when (this) {
        Forskrift.FOR_OMSORGSPOENG_K3_P4_L1_pkt1 -> HenvisningDb.FOR_OMSORGSPOENG_K3_P4_L1_pkt1
        Forskrift.FOR_OMSORGSPOENG_K2_P4_L3 -> HenvisningDb.FOR_OMSORGSPOENG_K2_P4_L3
    }
}

internal fun Lovparagraf.toDb(): HenvisningDb {
    return when (this) {
        Lovparagraf.FTRL_K20_P8_L1_Ba_pkt1 -> HenvisningDb.FTRL_K20_P8_L1_Ba_pkt1
        Lovparagraf.FTRL_K20_P8_L1_Ba_pkt2 -> HenvisningDb.FTRL_K20_P8_L1_Ba_pkt2
        Lovparagraf.FTRL_K20_P8_L1_Ba_pkt3 -> HenvisningDb.FTRL_K20_P8_L1_Ba_pkt3
        Lovparagraf.FTRL_K20_P8_L2 -> HenvisningDb.FTRL_K20_P8_L2
    }
}

internal fun HenvisningDb.toDomain(): Henvisning {
    return when (this) {
        HenvisningDb.FTRL_K20_P8_L1_Ba_pkt1 -> Lovparagraf.FTRL_K20_P8_L1_Ba_pkt1
        HenvisningDb.FTRL_K20_P8_L1_Ba_pkt2 -> Lovparagraf.FTRL_K20_P8_L1_Ba_pkt2
        HenvisningDb.FTRL_K20_P8_L1_Ba_pkt3 -> Lovparagraf.FTRL_K20_P8_L1_Ba_pkt3
        HenvisningDb.FTRL_K20_P8_L2 -> Lovparagraf.FTRL_K20_P8_L2
        HenvisningDb.FOR_OMSORGSPOENG_K3_P4_L1_pkt1 -> Forskrift.FOR_OMSORGSPOENG_K3_P4_L1_pkt1
        HenvisningDb.FOR_OMSORGSPOENG_K2_P4_L3 -> Forskrift.FOR_OMSORGSPOENG_K2_P4_L3
    }
}

internal fun Set<HenvisningDb>.toDomain(): Set<Henvisning> {
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
                henvisninger = paragrafer.toDomain(),
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
                henvisninger = paragrafer.toDomain(),
                grunnlag = grunnlag.toDomain(),
                utfall = utfall.toDomain()
            )
        }

        is VilkårsvurderingDb.OmsorgsyterIkkeEldreEnn69År -> {
            OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårVurdering(
                henvisninger = paragrafer.toDomain(),
                grunnlag = grunnlag.toDomain(),
                utfall = utfall.toDomain()
            )
        }

        is VilkårsvurderingDb.KanKunGodskrivesEnOmsorgsyter -> {
            KanKunGodskrivesEnOmsorgsyterVurdering(
                henvisninger = paragrafer.toDomain(),
                grunnlag = grunnlag.toDomain(),
                utfall = utfall.toDomain()
            )
        }

        is VilkårsvurderingDb.KanKunGodskrivesEtBarnPerÅr -> {
            KanKunGodskrivesEtBarnPerÅrVurdering(
                henvisninger = paragrafer.toDomain(),
                grunnlag = grunnlag.toDomain(),
                utfall = utfall.toDomain()

            )
        }

        is VilkårsvurderingDb.OmsorgsmottakerIkkeFylt6Ar -> {
            OmsorgsmottakerIkkeFylt6ArVurdering(
                henvisninger = paragrafer.toDomain(),
                grunnlag = grunnlag.toDomain(),
                utfall = utfall.toDomain()
            )
        }

        is VilkårsvurderingDb.LiktAntallMåneder -> {
            LiktAntallMånederOmsorgVurdering(
                henvisninger = paragrafer.toDomain(),
                grunnlag = grunnlag.toDomain(),
                utfall = utfall.toDomain()
            )
        }

        is VilkårsvurderingDb.OmsorgBarnUnder6OgIngenLikeMangeMåneder -> {
            FullOmsorgForBarnUnder6OgIngenHarLiktAntallMånederVurdering(
                henvisninger = paragrafer.toDomain(),
                grunnlag = grunnlag.toDomain(),
                utfall = utfall.toDomain()
            )
        }
    }
}
