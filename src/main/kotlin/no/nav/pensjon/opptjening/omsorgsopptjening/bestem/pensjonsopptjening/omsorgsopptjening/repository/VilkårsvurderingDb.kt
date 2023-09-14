package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.EllerVurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.JuridiskHenvisning
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OgVurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsmottakerHarIkkeFylt6VedUtløpAvOpptjeningsår
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsmottakerOppfyllerAlderskravForHjelpestønad
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterErFylt17VedUtløpAvOmsorgsår
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterErIkkeEldreEnn69VedUtløpAvOmsorgsår
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarTilstrekkeligOmsorgsarbeid
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
        val grunnlag: GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    @JsonTypeName("OmsorgsyterErFylt17VedUtløpAvOmsorgsår")
    internal data class OmsorgsyterErFylt17VedUtløpAvOmsorgsår(
        val grunnlag: GrunnlagVilkårsvurderingDb.PersonOgOmsorgsÅr,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    @JsonTypeName("OmsorgsyterErIkkeEldreEnn69VedUtløpAvOmsorgsår")
    internal data class OmsorgsyterErIkkeEldreEnn69VedUtløpAvOmsorgsår(
        val grunnlag: GrunnlagVilkårsvurderingDb.PersonOgOmsorgsÅr,
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

    @JsonTypeName("OmsorgsmottakerHarIkkeFylt6VedUtløpAvOpptjeningsår")
    internal data class OmsorgsmottakerHarIkkeFylt6VedUtløpAvOpptjeningsår(
        val grunnlag: GrunnlagVilkårsvurderingDb.PersonOgOmsorgsÅr,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    @JsonTypeName("OmsorgsmottakerIkkeFylt6VedUtløpAvOpptjeningsår")
    internal data class OmsorgsmottakerOppfyllerAlderskravForHjelpestønad(
        val grunnlag: GrunnlagVilkårsvurderingDb.PersonOgOmsorgsÅr,
        val utfall: VilkårsvurderingUtfallDb,
    ) : VilkårsvurderingDb()

    @JsonTypeName("OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere")
    internal data class OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere(
        val grunnlag: GrunnlagVilkårsvurderingDb.MestAvAlleOmsorgsytere,
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

        is OmsorgsyterErFylt17VedUtløpAvOmsorgsår.Vurdering -> {
            VilkårsvurderingDb.OmsorgsyterErFylt17VedUtløpAvOmsorgsår(
                grunnlag = grunnlag.toDb(),
                utfall = utfall.toDb()
            )
        }

        is OmsorgsyterErIkkeEldreEnn69VedUtløpAvOmsorgsår.Vurdering -> {
            VilkårsvurderingDb.OmsorgsyterErIkkeEldreEnn69VedUtløpAvOmsorgsår(
                grunnlag = grunnlag.toDb(),
                utfall = utfall.toDb()
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

        is OmsorgsmottakerHarIkkeFylt6VedUtløpAvOpptjeningsår.Vurdering -> {
            VilkårsvurderingDb.OmsorgsmottakerHarIkkeFylt6VedUtløpAvOpptjeningsår(
                grunnlag = grunnlag.toDb(),
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

        is VilkårsvurderingDb.OmsorgsyterErFylt17VedUtløpAvOmsorgsår -> {
            OmsorgsyterErFylt17VedUtløpAvOmsorgsår.Vurdering(
                grunnlag = grunnlag.toDomain(),
                utfall = utfall.toDomain()
            )
        }

        is VilkårsvurderingDb.OmsorgsyterErIkkeEldreEnn69VedUtløpAvOmsorgsår -> {
            OmsorgsyterErIkkeEldreEnn69VedUtløpAvOmsorgsår.Vurdering(
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

        is VilkårsvurderingDb.OmsorgsmottakerHarIkkeFylt6VedUtløpAvOpptjeningsår -> {
            OmsorgsmottakerHarIkkeFylt6VedUtløpAvOpptjeningsår.Vurdering(
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
    }
}
