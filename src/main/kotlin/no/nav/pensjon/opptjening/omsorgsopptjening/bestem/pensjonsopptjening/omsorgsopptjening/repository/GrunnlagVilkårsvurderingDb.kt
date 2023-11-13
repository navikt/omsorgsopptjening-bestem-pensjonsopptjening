package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.AldersvurderingsGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.AntallMånederRegel
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.GyldigeOmsorgsmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Omsorgsmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterErForelderTilMottakerAvHjelpestønad
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarTilstrekkeligOmsorgsarbeid
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
internal sealed class GrunnlagVilkårsvurderingDb {
    @JsonTypeName("OmsorgBarnUnder6")
    data class OmsorgBarnUnder6(
        val omsorgsytersOmsorgsmånederForOmsorgsmottaker: OmsorgsmånederDb,
        val antallMånederRegel: AntallMånederRegelDb
    ) : GrunnlagVilkårsvurderingDb()

    @JsonTypeName("AldersvurderingGrunnlag")
    data class AldersvurderingGrunnlag(
        val fnr: String,
        val fødselsdato: LocalDate,
        val omsorgsAr: Int,
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
        val omsorgsyter: String,
        val data: List<OmsorgsyterMottakerAntallMånederDb>
    ) : GrunnlagVilkårsvurderingDb()

    @JsonTypeName("OmsorgsyterOgOmsorgsmottaker")
    data class OmsorgsyterOgOmsorgsmottaker(
        val omsorgsyter: String,
        val omsorgsytersFamilierelasjoner: Map<String, String>,
        val omsorgsmottaker: String,
        val omsorgsmottakersFamilierelasjoner: Map<String, String>,
    ) : GrunnlagVilkårsvurderingDb()

    @JsonTypeName("MottarBarnetrygd")
    data class MottarBarnetrygd(
        val omsorgsytersUtbetalingsmåneder: Set<UtbetalingsmånedDb>,
        val antallMånederRegel: AntallMånederRegelDb,
        val omsorgstype: OmsorgstypeDb,
    ) : GrunnlagVilkårsvurderingDb()

    @JsonTypeName("GyldigOmsorgsarbeid")
    data class GyldigOmsorgsarbeid(
        val omsorgsytersUtbetalingsmåneder: Set<UtbetalingsmånedDb>,
        val omsorgsytersOmsorgsmåneder: OmsorgsmånederDb,
        val antallMånederRegel: AntallMånederRegelDb,
    ) : GrunnlagVilkårsvurderingDb()
}

data class AntallMånederRegelDb(
    val enum: AntallMånederRegelEnum,
    val antallMåneder: Int,
)

enum class AntallMånederRegelEnum {
    FødtIOmsorgsår,
    FødtUtenforOmsorgsår
}

internal fun AntallMånederRegel.toDb(): AntallMånederRegelDb {
    return when (this) {
        AntallMånederRegel.FødtIOmsorgsår -> AntallMånederRegelDb(
            AntallMånederRegelEnum.FødtIOmsorgsår,
            AntallMånederRegel.FødtIOmsorgsår.antall
        )

        AntallMånederRegel.FødtUtenforOmsorgsår -> AntallMånederRegelDb(
            AntallMånederRegelEnum.FødtUtenforOmsorgsår,
            AntallMånederRegel.FødtUtenforOmsorgsår.antall
        )
    }
}

internal fun AntallMånederRegelDb.toDomain(): AntallMånederRegel {
    return when (this.enum) {
        AntallMånederRegelEnum.FødtIOmsorgsår -> AntallMånederRegel.FødtIOmsorgsår
        AntallMånederRegelEnum.FødtUtenforOmsorgsår -> AntallMånederRegel.FødtUtenforOmsorgsår
    }
}

internal fun GrunnlagVilkårsvurderingDb.OmsorgsyterOgOmsorgsmottaker.toDomain(): OmsorgsyterErForelderTilMottakerAvHjelpestønad.Grunnlag {
    return OmsorgsyterErForelderTilMottakerAvHjelpestønad.Grunnlag(
        omsorgsyter = omsorgsyter,
        omsorgsytersFamilierelasjoner = omsorgsytersFamilierelasjoner.toDomain(),
        omsorgsmottaker = omsorgsmottaker,
        omsorgsmottakersFamilierelasjoner = omsorgsmottakersFamilierelasjoner.toDomain(),
    )
}

internal fun OmsorgsyterErForelderTilMottakerAvHjelpestønad.Grunnlag.toDb(): GrunnlagVilkårsvurderingDb.OmsorgsyterOgOmsorgsmottaker {
    return GrunnlagVilkårsvurderingDb.OmsorgsyterOgOmsorgsmottaker(
        omsorgsyter = omsorgsyter,
        omsorgsytersFamilierelasjoner = omsorgsytersFamilierelasjoner.toDb(),
        omsorgsmottaker = omsorgsmottaker,
        omsorgsmottakersFamilierelasjoner = omsorgsmottakersFamilierelasjoner.toDb(),
    )
}

internal fun GrunnlagVilkårsvurderingDb.MestAvAlleOmsorgsytere.toDomain(): OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Grunnlag {
    return OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Grunnlag(
        omsorgsyter = omsorgsyter,
        data = data.map {
            OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.OmsorgsmånederForMottakerOgÅr(
                omsorgsyter = it.omsorgsyter,
                omsorgsmottaker = it.omsorgsmottaker,
                omsorgsmåneder = GyldigeOmsorgsmåneder(it.omsorgsmåneder),
                omsorgsår = it.omsorgsår
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
    val omsorgsyter: String,
    val omsorgsmottaker: String,
    val omsorgsmåneder: Set<YearMonth>,
    val omsorgsår: Int,
)

internal fun OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Grunnlag.toDb(): GrunnlagVilkårsvurderingDb.MestAvAlleOmsorgsytere {
    return GrunnlagVilkårsvurderingDb.MestAvAlleOmsorgsytere(
        omsorgsyter = omsorgsyter,
        data = data.map {
            OmsorgsyterMottakerAntallMånederDb(
                omsorgsyter = it.omsorgsyter,
                omsorgsmottaker = it.omsorgsmottaker,
                omsorgsmåneder = it.omsorgsmåneder.alleMåneder(),
                omsorgsår = it.omsorgsår
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
    return GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6(
        omsorgsytersOmsorgsmånederForOmsorgsmottaker = omsorgsytersOmsorgsmånederForOmsorgsmottaker.toDb(),
        antallMånederRegel = antallMånederRegel.toDb()
    )
}

internal fun GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.toDomain(): OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag {
    return OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag(
        omsorgsytersOmsorgsmånederForOmsorgsmottaker = omsorgsytersOmsorgsmånederForOmsorgsmottaker.toDomain(),
        antallMånederRegel = antallMånederRegel.toDomain(),
    )
}

internal fun GrunnlagVilkårsvurderingDb.AldersvurderingGrunnlag.toDomain(): AldersvurderingsGrunnlag {
    return AldersvurderingsGrunnlag(
        person = AldersvurderingsGrunnlag.AldersvurderingsPerson(
            fnr = fnr,
            fødselsdato = fødselsdato,
        ),
        omsorgsAr = omsorgsAr
    )
}

internal fun AldersvurderingsGrunnlag.toDb(): GrunnlagVilkårsvurderingDb.AldersvurderingGrunnlag {
    return GrunnlagVilkårsvurderingDb.AldersvurderingGrunnlag(
        fnr = person.fnr,
        fødselsdato = person.fødselsdato,
        omsorgsAr = omsorgsAr
    )
}

internal data class OmsorgsmånederDb(
    val omsorgsmåneder: Set<YearMonth>,
    val type: OmsorgstypeDb,
)

internal fun Omsorgsmåneder.toDb(): OmsorgsmånederDb {
    return when (this) {
        is Omsorgsmåneder.Barnetrygd -> {
            OmsorgsmånederDb(this.alleMåneder(), OmsorgstypeDb.BARNETRYGD)
        }

        is Omsorgsmåneder.Hjelpestønad -> {
            OmsorgsmånederDb(this.alleMåneder(), OmsorgstypeDb.HJELPESTØNAD)
        }
    }
}

internal fun OmsorgsmånederDb.toDomain(): Omsorgsmåneder {
    return when (this.type) {
        OmsorgstypeDb.BARNETRYGD -> Omsorgsmåneder.Barnetrygd(this.omsorgsmåneder)
        OmsorgstypeDb.HJELPESTØNAD -> Omsorgsmåneder.Hjelpestønad(this.omsorgsmåneder)
    }
}

