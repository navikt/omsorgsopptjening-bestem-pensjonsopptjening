package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.EllerVurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.JuridiskHenvisning
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OgVurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsmottakerOppfyllerAlderskravForBarnetrygd
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsmottakerOppfyllerAlderskravForHjelpestønad
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterErForelderTilMottakerAvHjelpestønad
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarGyldigOmsorgsarbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarTilstrekkeligOmsorgsarbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterMottarBarnetrgyd
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterOppfyllerAlderskrav
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Utbetalingsmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.VilkarsVurdering
import java.util.LinkedList
import java.util.Queue

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
sealed class VilkårsvurderingDb {
    @JsonTypeName("OmsorgsyterHarTilstrekkeligOmsorgsarbeid")
    internal data class OmsorgsyterHarTilstrekkeligOmsorgsarbeid(
        val grunnlag: GrunnlagVilkårsvurderingDb.TilstrekkeligOmsorgsarbeid,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    @JsonTypeName("OmsorgsyterOppfyllerAlderskrav")
    internal data class OmsorgsyterOppfyllerAlderskrav(
        val grunnlag: GrunnlagVilkårsvurderingDb.AldersvurderingGrunnlag,
        val gyldigAldersintervall: Aldersintervall,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    @JsonTypeName("Eller")
    internal data class Eller(
        val eller: List<VilkårsvurderingDb>,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    @JsonTypeName("Og")
    internal data class Og(
        val og: List<VilkårsvurderingDb>,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    @JsonTypeName("OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyter")
    internal data class OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyter(
        val grunnlag: GrunnlagVilkårsvurderingDb.KanKunGodskrivesEnOmsorgsyter,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    @JsonTypeName("OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr")
    internal data class OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr(
        val grunnlag: GrunnlagVilkårsvurderingDb.KanKunGodskrivesEtBarnPerÅr,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    @JsonTypeName("OmsorgsmottakerOppfyllerAlderskravForBarnetrygd")
    internal data class OmsorgsmottakerOppfyllerAlderskravForBarnetrygd(
        val grunnlag: GrunnlagVilkårsvurderingDb.AldersvurderingGrunnlag,
        val gyldigAldersintervall: Aldersintervall,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    @JsonTypeName("OmsorgsmottakerOppfyllerAlderskravForHjelpestønad")
    internal data class OmsorgsmottakerOppfyllerAlderskravForHjelpestønad(
        val grunnlag: GrunnlagVilkårsvurderingDb.AldersvurderingGrunnlag,
        val gyldigAldersintervall: Aldersintervall,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    @JsonTypeName("OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere")
    internal data class OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere(
        val grunnlag: GrunnlagVilkårsvurderingDb.MestAvAlleOmsorgsytere,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    @JsonTypeName("OmsorgsyterErForelderTilMottakerAvHjelpestønad")
    internal data class OmsorgsyterErForelderTilMottakerAvHjelpestønad(
        val grunnlag: GrunnlagVilkårsvurderingDb.OmsorgsyterOgOmsorgsmottaker,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    @JsonTypeName("OmsorgsyterMottarBarnetrygd")
    internal data class OmsorgsyterMottarBarnetrygd(
        val grunnlag: GrunnlagVilkårsvurderingDb.MottarBarnetrygd,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    @JsonTypeName("OmsorgsyterHarGyldigOmsorgsarbeid")
    internal data class OmsorgsyterHarGyldigOmsorgsarbeid(
        val grunnlag: GrunnlagVilkårsvurderingDb.GyldigOmsorgsarbeid,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()
}

internal fun VilkarsVurdering<*>.toDb(): VilkårsvurderingDb {
    return when (this) {
        is EllerVurdering -> {
            VilkårsvurderingDb.Eller(
                eller = mapRecursive(LinkedList(grunnlag), emptyList()),
                utfall = utfall.toDb()
            )
        }

        is OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering -> {
            VilkårsvurderingDb.OmsorgsyterHarTilstrekkeligOmsorgsarbeid(
                grunnlag = grunnlag.toDb(),
                utfall = utfall.toDb()
            )
        }

        is OgVurdering -> {
            VilkårsvurderingDb.Og(
                og = mapRecursive(LinkedList(grunnlag), emptyList()),
                utfall = utfall.toDb()
            )
        }

        is OmsorgsyterOppfyllerAlderskrav.Vurdering -> {
            VilkårsvurderingDb.OmsorgsyterOppfyllerAlderskrav(
                grunnlag = grunnlag.toDb(),
                gyldigAldersintervall = gyldigAldersintervall.toDb(),
                utfall = utfall.toDb(),
            )
        }

        is OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Vurdering -> {
            VilkårsvurderingDb.OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyter(
                grunnlag = grunnlag.toDb(),
                utfall = utfall.toDb()
            )
        }

        is OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering -> {
            VilkårsvurderingDb.OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr(
                grunnlag = grunnlag.toDb(),
                utfall = utfall.toDb()
            )
        }

        is OmsorgsmottakerOppfyllerAlderskravForBarnetrygd.Vurdering -> {
            VilkårsvurderingDb.OmsorgsmottakerOppfyllerAlderskravForBarnetrygd(
                grunnlag = grunnlag.toDb(),
                gyldigAldersintervall = gyldigAldersintervall.toDb(),
                utfall = utfall.toDb()
            )
        }

        is OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering -> {
            VilkårsvurderingDb.OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere(
                grunnlag = grunnlag.toDb(),
                utfall = utfall.toDb()
            )
        }

        is OmsorgsmottakerOppfyllerAlderskravForHjelpestønad.Vurdering -> {
            VilkårsvurderingDb.OmsorgsmottakerOppfyllerAlderskravForHjelpestønad(
                grunnlag = grunnlag.toDb(),
                gyldigAldersintervall = gyldigAldersintervall.toDb(),
                utfall = utfall.toDb()
            )
        }

        is OmsorgsyterErForelderTilMottakerAvHjelpestønad.Vurdering -> {
            VilkårsvurderingDb.OmsorgsyterErForelderTilMottakerAvHjelpestønad(
                grunnlag = grunnlag.toDb(),
                utfall = utfall.toDb()
            )
        }

        is OmsorgsyterMottarBarnetrgyd.Vurdering -> {
            VilkårsvurderingDb.OmsorgsyterMottarBarnetrygd(
                grunnlag = grunnlag.toDb(),
                utfall = utfall.toDb(),
            )
        }

        is OmsorgsyterHarGyldigOmsorgsarbeid.Vurdering -> {
            VilkårsvurderingDb.OmsorgsyterHarGyldigOmsorgsarbeid(
                grunnlag = grunnlag.toDb(),
                utfall = utfall.toDb()
            )
        }
    }
}

data class Aldersintervall(
    val min: Int,
    val max: Int
)

internal fun IntRange.toDb(): Aldersintervall {
    return Aldersintervall(min = first, max = last)
}

internal fun Aldersintervall.toDomain(): IntRange {
    return min..max
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

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonTypeName("JuridiskHenvisningDb")
data class JuridiskHenvisningDb(
    val kortTittel: String? = null,
    val dato: String? = null,
    val kapittel: Int? = null,
    val paragraf: Int? = null,
    val ledd: Int? = null,
    val bokstav: String? = null,
    val punktum: Int? = null,
    val tekst: String? = null
)

internal fun JuridiskHenvisning.toDb(): JuridiskHenvisningDb {
    return JuridiskHenvisningDb(
        kortTittel = kortTittel,
        dato = dato,
        kapittel = kapittel,
        paragraf = paragraf,
        ledd = ledd,
        bokstav = bokstav,
        punktum = punktum,
        tekst = tekst
    )
}


internal fun Set<JuridiskHenvisning>.toDb(): Set<JuridiskHenvisningDb> {
    return map { it.toDb() }.toSet()
}

internal fun JuridiskHenvisningDb.toDomain(): JuridiskHenvisning {
    return JuridiskHenvisning.Arkivert(
        kortTittel = kortTittel,
        dato = dato,
        kapittel = kapittel,
        paragraf = paragraf,
        ledd = ledd,
        bokstav = bokstav,
        punktum = punktum,
        tekst = tekst
    )
}

internal fun Set<JuridiskHenvisningDb>.toDomain(): Set<JuridiskHenvisning> {
    return map { it.toDomain() }.toSet()
}

internal fun VilkårsvurderingDb.toDomain(): VilkarsVurdering<*> {
    return when (this) {
        is VilkårsvurderingDb.Eller -> {
            EllerVurdering(
                grunnlag = eller.toDomain(),
                utfall = utfall.toDomain()
            )
        }

        is VilkårsvurderingDb.OmsorgsyterHarTilstrekkeligOmsorgsarbeid -> {
            OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering(
                grunnlag = grunnlag.toDomain(),
                utfall = utfall.toDomain()
            )
        }

        is VilkårsvurderingDb.Og -> {
            OgVurdering(
                grunnlag = og.toDomain(),
                utfall = utfall.toDomain()
            )
        }

        is VilkårsvurderingDb.OmsorgsyterOppfyllerAlderskrav -> {
            OmsorgsyterOppfyllerAlderskrav.Vurdering(
                grunnlag = grunnlag.toDomain(),
                utfall = utfall.toDomain()
            )
        }

        is VilkårsvurderingDb.OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyter -> {
            OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Vurdering(
                grunnlag = grunnlag.toDomain(),
                utfall = utfall.toDomain()
            )
        }

        is VilkårsvurderingDb.OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr -> {
            OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering(
                grunnlag = grunnlag.toDomain(),
                utfall = utfall.toDomain()

            )
        }

        is VilkårsvurderingDb.OmsorgsmottakerOppfyllerAlderskravForBarnetrygd -> {
            OmsorgsmottakerOppfyllerAlderskravForBarnetrygd.Vurdering(
                grunnlag = grunnlag.toDomain(),
                utfall = utfall.toDomain()
            )
        }

        is VilkårsvurderingDb.OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere -> {
            OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering(
                grunnlag = grunnlag.toDomain(),
                utfall = utfall.toDomain()
            )
        }

        is VilkårsvurderingDb.OmsorgsmottakerOppfyllerAlderskravForHjelpestønad -> {
            OmsorgsmottakerOppfyllerAlderskravForHjelpestønad.Vurdering(
                grunnlag = grunnlag.toDomain(),
                utfall = utfall.toDomain()
            )
        }

        is VilkårsvurderingDb.OmsorgsyterErForelderTilMottakerAvHjelpestønad -> {
            OmsorgsyterErForelderTilMottakerAvHjelpestønad.Vurdering(
                grunnlag = grunnlag.toDomain(),
                utfall = utfall.toDomain()
            )
        }

        is VilkårsvurderingDb.OmsorgsyterMottarBarnetrygd -> {
            OmsorgsyterMottarBarnetrgyd.Vurdering(
                grunnlag = grunnlag.toDomain(),
                utfall = utfall.toDomain(),
            )
        }

        is VilkårsvurderingDb.OmsorgsyterHarGyldigOmsorgsarbeid -> {
            OmsorgsyterHarGyldigOmsorgsarbeid.Vurdering(
                grunnlag = grunnlag.toDomain(),
                utfall = utfall.toDomain(),
            )
        }
    }
}

internal fun GrunnlagVilkårsvurderingDb.MottarBarnetrygd.toDomain(): OmsorgsyterMottarBarnetrgyd.Grunnlag {
    return OmsorgsyterMottarBarnetrgyd.Grunnlag(
        omsorgsytersUtbetalingsmåneder = Utbetalingsmåneder(omsorgsytersUtbetalingsmåneder.map { it.toDomain() }
                                                                .toSet()),
        antallMånederRegel = antallMånederRegel.toDomain(),
        omsorgstype = omsorgstype.toDomain(),
    )
}

internal fun OmsorgsyterMottarBarnetrgyd.Grunnlag.toDb(): GrunnlagVilkårsvurderingDb.MottarBarnetrygd {
    return GrunnlagVilkårsvurderingDb.MottarBarnetrygd(
        omsorgsytersUtbetalingsmåneder = omsorgsytersUtbetalingsmåneder.måneder.map { it.toDb() }.toSet(),
        antallMånederRegel = antallMånederRegel.toDb(),
        omsorgstype = omsorgstype.toDb(),
    )
}

internal fun GrunnlagVilkårsvurderingDb.GyldigOmsorgsarbeid.toDomain(): OmsorgsyterHarGyldigOmsorgsarbeid.Grunnlag {
    return OmsorgsyterHarGyldigOmsorgsarbeid.Grunnlag(
        omsorgsytersUtbetalingsmåneder = Utbetalingsmåneder(omsorgsytersUtbetalingsmåneder.map { it.toDomain() }
                                                                .toSet()),
        omsorgsytersOmsorgsmåneder = omsorgsytersOmsorgsmåneder.toDomain(),
        antallMånederRegel = antallMånederRegel.toDomain(),
    )
}

internal fun OmsorgsyterHarGyldigOmsorgsarbeid.Grunnlag.toDb(): GrunnlagVilkårsvurderingDb.GyldigOmsorgsarbeid {
    return GrunnlagVilkårsvurderingDb.GyldigOmsorgsarbeid(
        omsorgsytersUtbetalingsmåneder = omsorgsytersUtbetalingsmåneder.måneder.map { it.toDb() }.toSet(),
        omsorgsytersOmsorgsmåneder = omsorgsytersOmsorgsmåneder.toDb(),
        antallMånederRegel = antallMånederRegel.toDb(),
    )
}
