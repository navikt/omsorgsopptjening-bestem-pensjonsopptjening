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
        val paragrafer: Set<LovhenvisningDb>,
        val grunnlag: GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    internal data class OmsorgsyterFylt17År(
        val paragrafer: Set<LovhenvisningDb>,
        val grunnlag: GrunnlagVilkårsvurderingDb.PersonOgOmsorgsÅr,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    internal data class OmsorgsyterIkkeEldreEnn69År(
        val paragrafer: Set<LovhenvisningDb>,
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
        val paragrafer: Set<LovhenvisningDb>,
        val grunnlag: GrunnlagVilkårsvurderingDb.KanKunGodskrivesEnOmsorgsyter,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    internal data class KanKunGodskrivesEtBarnPerÅr(
        val paragrafer: Set<LovhenvisningDb>,
        val grunnlag: GrunnlagVilkårsvurderingDb.KanKunGodskrivesEtBarnPerÅr,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    internal data class OmsorgsmottakerIkkeFylt6Ar(
        val paragrafer: Set<LovhenvisningDb>,
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
                paragrafer = lovhenvisninger.toDb(),
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
                paragrafer = lovhenvisninger.toDb(),
                grunnlag = grunnlag.toDb(),
                utfall = utfall.toDb()
            )
        }

        is OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårVurdering -> {
            VilkårsvurderingDb.OmsorgsyterIkkeEldreEnn69År(
                paragrafer = lovhenvisninger.toDb(),
                grunnlag = grunnlag.toDb(),
                utfall = utfall.toDb()
            )
        }

        is KanKunGodskrivesEnOmsorgsyterVurdering -> {
            VilkårsvurderingDb.KanKunGodskrivesEnOmsorgsyter(
                paragrafer = lovhenvisninger.toDb(),
                grunnlag = grunnlag.toDb(),
                utfall = utfall.toDb()
            )
        }

        is KanKunGodskrivesEtBarnPerÅrVurdering -> {
            VilkårsvurderingDb.KanKunGodskrivesEtBarnPerÅr(
                paragrafer = lovhenvisninger.toDb(),
                grunnlag = grunnlag.toDb(),
                utfall = utfall.toDb()
            )
        }

        is OmsorgsmottakerIkkeFylt6ArVurdering -> {
            VilkårsvurderingDb.OmsorgsmottakerIkkeFylt6Ar(
                paragrafer = lovhenvisninger.toDb(),
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
enum class LovhenvisningDb {
    MINST_HALVT_AR_OMSORG,
    OMSORGSMOTTAKER_IKKE_FYLT_6_AR,
    IKKE_KRAV_OM_MINST_HALVT_AR_I_FODSELSAR,
    OPPTJENING_GIS_BARNETRYGDMOTTAKER,
    FULL_OMSORG_KAN_GODSKRIVES_AUTOMATISK,
    FYLLER_17_AR,
    FYLLER_69_AR,
    OMSORGSOPPTJENING_GIS_KUN_EN_OMSORGSYTER,
    KAN_KUN_GODSKRIVES_ET_BARN
}

internal fun Set<Lovhenvisning>.toDb(): Set<LovhenvisningDb> {
    return map { it.toDb() }.toSet()
}
internal fun Lovhenvisning.toDb(): LovhenvisningDb {
    return when(this){
        Lovhenvisning.MINST_HALVT_AR_OMSORG -> LovhenvisningDb.MINST_HALVT_AR_OMSORG
        Lovhenvisning.OMSORGSMOTTAKER_IKKE_FYLT_6_AR -> LovhenvisningDb.OMSORGSMOTTAKER_IKKE_FYLT_6_AR
        Lovhenvisning.IKKE_KRAV_OM_MINST_HALVT_AR_I_FODSELSAR -> LovhenvisningDb.IKKE_KRAV_OM_MINST_HALVT_AR_I_FODSELSAR
        Lovhenvisning.OPPTJENING_GIS_BARNETRYGDMOTTAKER -> LovhenvisningDb.OPPTJENING_GIS_BARNETRYGDMOTTAKER
        Lovhenvisning.FULL_OMSORG_KAN_GODSKRIVES_AUTOMATISK -> LovhenvisningDb.FULL_OMSORG_KAN_GODSKRIVES_AUTOMATISK
        Lovhenvisning.FYLLER_17_AR -> LovhenvisningDb.FYLLER_17_AR
        Lovhenvisning.FYLLER_69_AR -> LovhenvisningDb.FYLLER_69_AR
        Lovhenvisning.OMSORGSOPPTJENING_GIS_KUN_EN_OMSORGSYTER -> LovhenvisningDb.OMSORGSOPPTJENING_GIS_KUN_EN_OMSORGSYTER
        Lovhenvisning.KAN_KUN_GODSKRIVES_ET_BARN -> LovhenvisningDb.KAN_KUN_GODSKRIVES_ET_BARN
    }
}

internal fun LovhenvisningDb.toDomain(): Lovhenvisning {
    return when(this){
        LovhenvisningDb.MINST_HALVT_AR_OMSORG -> Lovhenvisning.MINST_HALVT_AR_OMSORG
        LovhenvisningDb.OMSORGSMOTTAKER_IKKE_FYLT_6_AR -> Lovhenvisning.OMSORGSMOTTAKER_IKKE_FYLT_6_AR
        LovhenvisningDb.IKKE_KRAV_OM_MINST_HALVT_AR_I_FODSELSAR -> Lovhenvisning.IKKE_KRAV_OM_MINST_HALVT_AR_I_FODSELSAR
        LovhenvisningDb.OPPTJENING_GIS_BARNETRYGDMOTTAKER -> Lovhenvisning.OPPTJENING_GIS_BARNETRYGDMOTTAKER
        LovhenvisningDb.FULL_OMSORG_KAN_GODSKRIVES_AUTOMATISK -> Lovhenvisning.FULL_OMSORG_KAN_GODSKRIVES_AUTOMATISK
        LovhenvisningDb.FYLLER_17_AR -> Lovhenvisning.FYLLER_17_AR
        LovhenvisningDb.FYLLER_69_AR -> Lovhenvisning.FYLLER_69_AR
        LovhenvisningDb.OMSORGSOPPTJENING_GIS_KUN_EN_OMSORGSYTER -> Lovhenvisning.OMSORGSOPPTJENING_GIS_KUN_EN_OMSORGSYTER
        LovhenvisningDb.KAN_KUN_GODSKRIVES_ET_BARN -> Lovhenvisning.KAN_KUN_GODSKRIVES_ET_BARN
    }
}

internal fun Set<LovhenvisningDb>.toDomain(): Set<Lovhenvisning> {
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
                lovhenvisninger = paragrafer.toDomain(),
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
                lovhenvisninger = paragrafer.toDomain(),
                grunnlag = grunnlag.toDomain(),
                utfall = utfall.toDomain()
            )
        }

        is VilkårsvurderingDb.OmsorgsyterIkkeEldreEnn69År -> {
            OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårVurdering(
                lovhenvisninger = paragrafer.toDomain(),
                grunnlag = grunnlag.toDomain(),
                utfall = utfall.toDomain()
            )
        }

        is VilkårsvurderingDb.KanKunGodskrivesEnOmsorgsyter -> {
            KanKunGodskrivesEnOmsorgsyterVurdering(
                lovhenvisninger = paragrafer.toDomain(),
                grunnlag = grunnlag.toDomain(),
                utfall = utfall.toDomain()
            )
        }
        is VilkårsvurderingDb.KanKunGodskrivesEtBarnPerÅr -> {
            KanKunGodskrivesEtBarnPerÅrVurdering(
                lovhenvisninger = paragrafer.toDomain(),
                grunnlag = grunnlag.toDomain(),
                utfall = utfall.toDomain()

            )
        }

        is VilkårsvurderingDb.OmsorgsmottakerIkkeFylt6Ar -> {
            OmsorgsmottakerIkkeFylt6ArVurdering(
                lovhenvisninger = paragrafer.toDomain(),
                grunnlag = grunnlag.toDomain(),
                utfall = utfall.toDomain()
            )
        }
    }
}
