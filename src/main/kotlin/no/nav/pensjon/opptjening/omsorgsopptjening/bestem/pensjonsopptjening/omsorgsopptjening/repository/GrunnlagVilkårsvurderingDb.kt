package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarTilstrekkeligOmsorgsarbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.PersonOgOmsorgsårGrunnlag
import java.time.YearMonth
import java.util.UUID

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
internal sealed class GrunnlagVilkårsvurderingDb {
    internal sealed class OmsorgBarnUnder6 : GrunnlagVilkårsvurderingDb() {
        abstract val omsorgsAr: Int
        abstract val omsorgsmottaker: PersonDb
        abstract val omsorgsmåneder: Set<YearMonth>

        @JsonTypeName("OmsorgBarnFødtOmsorgsår")
        data class OmsorgBarnFødtOmsorgsår(
            override val omsorgsAr: Int,
            override val omsorgsmottaker: PersonDb,
            override val omsorgsmåneder: Set<YearMonth>,
        ) : OmsorgBarnUnder6()

        @JsonTypeName("OmsorgBarnFødtDesemberOmsorgsår")
        data class OmsorgBarnFødtDesemberOmsorgsår(
            override val omsorgsAr: Int,
            override val omsorgsmottaker: PersonDb,
            override val omsorgsmåneder: Set<YearMonth>,
        ) : OmsorgBarnUnder6()

        @JsonTypeName("OmsorgBarnFødtUtenforOmsorgsår")
        data class OmsorgBarnFødtUtenforOmsorgsår(
            override val omsorgsAr: Int,
            override val omsorgsmottaker: PersonDb,
            override val omsorgsmåneder: Set<YearMonth>,
        ) : OmsorgBarnUnder6()
    }

    @JsonTypeName("PersonOgOmsorgsÅr")
    data class PersonOgOmsorgsÅr(
        val person: PersonDb,
        val omsorgsAr: Int
    ) : GrunnlagVilkårsvurderingDb()

    @JsonTypeName("KanKunGodskrivesEnOmsorgsyter")
    data class KanKunGodskrivesEnOmsorgsyter(
        val omsorgsAr: Int,
        val behandlinger: List<FullførteBehandlingerForOmsorgsmottakerDb>
    ) : GrunnlagVilkårsvurderingDb()

    @JsonTypeName("KanKunGodskrivesEtBarnPerÅr")
    data class KanKunGodskrivesEtBarnPerÅr(
        val omsorgsmottaker: String,
        val omsorgsAr: Int,
        val behandlinger: List<FullførteBehandlingForOmsorgsyterDb>
    ) : GrunnlagVilkårsvurderingDb()

    @JsonTypeName("MestAvAlleOmsorgsytere")
    data class MestAvAlleOmsorgsytere(
        val omsorgsyter: PersonDb,
        val data: List<OmsorgsyterMottakerAntallMånederDb>
    ) : GrunnlagVilkårsvurderingDb()
}

internal fun GrunnlagVilkårsvurderingDb.MestAvAlleOmsorgsytere.toDomain(): OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Grunnlag {
    return OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Grunnlag(
        omsorgsyter = omsorgsyter.toDomain(),
        summert = data.map {
            OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.OmsorgsmånederForMottakerOgÅr(
                omsorgsyter = it.omsorgsyter.toDomain(),
                omsorgsmottaker = it.omsorgsmottaker.toDomain(),
                omsorgsmåneder = it.omsorgsmåneder,
                år = it.år
            )
        }
    )
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonTypeName("OmsorgsyterMottakerAntallMånederDb")
internal data class OmsorgsyterMottakerAntallMånederDb(
    val omsorgsyter: PersonDb,
    val omsorgsmottaker: PersonDb,
    val omsorgsmåneder: Set<YearMonth>,
    val år: Int,
)

internal fun OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Grunnlag.toDb(): GrunnlagVilkårsvurderingDb.MestAvAlleOmsorgsytere {
    return GrunnlagVilkårsvurderingDb.MestAvAlleOmsorgsytere(
        omsorgsyter = omsorgsyter.toDb(),
        data = summert.map {
            OmsorgsyterMottakerAntallMånederDb(
                omsorgsyter = it.omsorgsyter.toDb(),
                omsorgsmottaker = it.omsorgsmottaker.toDb(),
                omsorgsmåneder = it.omsorgsmåneder,
                år = it.år
            )
        }
    )
}

internal fun OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Grunnlag.toDb(): GrunnlagVilkårsvurderingDb.KanKunGodskrivesEnOmsorgsyter {
    return GrunnlagVilkårsvurderingDb.KanKunGodskrivesEnOmsorgsyter(
        omsorgsAr = omsorgsår,
        behandlinger = fullførteBehandlinger.toDbs()
    )
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonTypeName("FullførteBehandlingerForOmsorgsmottakerDb")
data class FullførteBehandlingerForOmsorgsmottakerDb(
    val behandlingsId: UUID,
    val omsorgsyter: String,
    val omsorgsmottaker: String,
    val omsorgsAr: Int,
    val erInnvilget: Boolean
)

internal fun List<OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Grunnlag.FullførtBehandlingForOmsorgsmottaker>.toDbs(): List<FullførteBehandlingerForOmsorgsmottakerDb> {
    return map { it.toDb() }
}

internal fun OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Grunnlag.FullførtBehandlingForOmsorgsmottaker.toDb(): FullførteBehandlingerForOmsorgsmottakerDb {
    return FullførteBehandlingerForOmsorgsmottakerDb(
        behandlingsId = behandlingsId,
        omsorgsyter = omsorgsyter,
        omsorgsmottaker = omsorgsmottaker,
        omsorgsAr = omsorgsår,
        erInnvilget = erInnvilget,
    )
}

internal fun OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag.toDb(): GrunnlagVilkårsvurderingDb.KanKunGodskrivesEtBarnPerÅr {
    return GrunnlagVilkårsvurderingDb.KanKunGodskrivesEtBarnPerÅr(
        omsorgsmottaker = omsorgsmottaker,
        omsorgsAr = omsorgsår,
        behandlinger = behandlinger.toDb()
    )
}

internal fun GrunnlagVilkårsvurderingDb.KanKunGodskrivesEtBarnPerÅr.toDomain(): OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag {
    return OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag(
        omsorgsmottaker = omsorgsmottaker,
        omsorgsår = omsorgsAr,
        behandlinger = behandlinger.toDomain()
    )
}

internal fun GrunnlagVilkårsvurderingDb.KanKunGodskrivesEnOmsorgsyter.toDomain(): OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Grunnlag {
    return OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Grunnlag(
        omsorgsår = omsorgsAr,
        fullførteBehandlinger = behandlinger.toDomain()
    )
}

@JvmName("FullførteBehandlingerForOmsorgsmottakerOgÅrDb")
internal fun List<FullførteBehandlingerForOmsorgsmottakerDb>.toDomain(): List<OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Grunnlag.FullførtBehandlingForOmsorgsmottaker> {
    return map { it.toDomain() }
}

internal fun FullførteBehandlingerForOmsorgsmottakerDb.toDomain(): OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Grunnlag.FullførtBehandlingForOmsorgsmottaker {
    return OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Grunnlag.FullførtBehandlingForOmsorgsmottaker(
        behandlingsId = behandlingsId,
        omsorgsyter = omsorgsyter,
        omsorgsmottaker = omsorgsmottaker,
        omsorgsår = omsorgsAr,
        erInnvilget = erInnvilget
    )
}
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonTypeName("FullførteBehandlingForOmsorgsyterDb")
internal data class FullførteBehandlingForOmsorgsyterDb(
    val behandlingsId: UUID,
    val omsorgsyter: String,
    val omsorgsmottaker: String,
    val år: Int,
    val erInnvilget: Boolean,
)

internal fun List<OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag.FullførtBehandlingForOmsorgsyter>.toDb(): List<FullførteBehandlingForOmsorgsyterDb> {
    return map { it.toDb() }
}

internal fun List<FullførteBehandlingForOmsorgsyterDb>.toDomain(): List<OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag.FullførtBehandlingForOmsorgsyter> {
    return map { it.toDomain() }
}

internal fun OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag.FullførtBehandlingForOmsorgsyter.toDb(): FullførteBehandlingForOmsorgsyterDb {
    return FullførteBehandlingForOmsorgsyterDb(
        behandlingsId = behandlingsId,
        omsorgsyter = omsorgsyter,
        omsorgsmottaker = omsorgsmottaker,
        år = omsorgsÅr,
        erInnvilget = erInnvilget,
    )
}

internal fun FullførteBehandlingForOmsorgsyterDb.toDomain(): OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag.FullførtBehandlingForOmsorgsyter {
    return OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag.FullførtBehandlingForOmsorgsyter(
        behandlingsId = behandlingsId,
        omsorgsyter = omsorgsyter,
        omsorgsmottaker = omsorgsmottaker,
        omsorgsÅr = år,
        erInnvilget = erInnvilget
    )
}

internal fun OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.toDb(): GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6 {
    return when (this) {
        is OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtIOmsorgsår -> {
            GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.OmsorgBarnFødtOmsorgsår(
                omsorgsAr = omsorgsAr,
                omsorgsmottaker = omsorgsmottaker.toDb(),
                omsorgsmåneder = omsorgsmåneder
            )
        }

        is OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtUtenforOmsorgsår -> {
            GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.OmsorgBarnFødtUtenforOmsorgsår(
                omsorgsAr = omsorgsAr,
                omsorgsmottaker = omsorgsmottaker.toDb(),
                omsorgsmåneder = omsorgsmåneder
            )
        }

        is OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtIDesemberOmsorgsår -> {
            GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.OmsorgBarnFødtDesemberOmsorgsår(
                omsorgsAr = omsorgsAr,
                omsorgsmottaker = omsorgsmottaker.toDb(),
                omsorgsmåneder = omsorgsmåneder
            )
        }
    }
}

internal fun GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.toDomain(): OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag {
    return when (this) {
        is GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.OmsorgBarnFødtOmsorgsår -> {
            OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtIOmsorgsår(
                omsorgsAr = omsorgsAr,
                omsorgsmottaker = omsorgsmottaker.toDomain(),
                omsorgsmåneder = omsorgsmåneder
            )
        }

        is GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.OmsorgBarnFødtUtenforOmsorgsår -> {
            OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtUtenforOmsorgsår(
                omsorgsAr = omsorgsAr,
                omsorgsmottaker = omsorgsmottaker.toDomain(),
                omsorgsmåneder = omsorgsmåneder

            )
        }

        is GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.OmsorgBarnFødtDesemberOmsorgsår -> {
            OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtIDesemberOmsorgsår(
                omsorgsAr = omsorgsAr,
                omsorgsmottaker = omsorgsmottaker.toDomain(),
                omsorgsmåneder = omsorgsmåneder
            )
        }
    }
}

internal fun GrunnlagVilkårsvurderingDb.PersonOgOmsorgsÅr.toDomain(): PersonOgOmsorgsårGrunnlag {
    return PersonOgOmsorgsårGrunnlag(
        person = person.toDomain(),
        omsorgsAr = omsorgsAr
    )
}

internal fun PersonOgOmsorgsårGrunnlag.toDb(): GrunnlagVilkårsvurderingDb.PersonOgOmsorgsÅr {
    return GrunnlagVilkårsvurderingDb.PersonOgOmsorgsÅr(
        person = person.toDb(),
        omsorgsAr = omsorgsAr
    )
}

