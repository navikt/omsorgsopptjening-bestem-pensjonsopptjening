package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.AldersvurderingsGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterErForelderTilMottakerAvHjelpestønad
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarTilstrekkeligOmsorgsarbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Omsorgsmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.MedlemIFolketrygden
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
internal sealed class GrunnlagVilkårsvurderingDb {
    internal sealed class OmsorgBarnUnder6 : GrunnlagVilkårsvurderingDb() {
        abstract val aldersvurderingOmsorgsmottaker: AldersvurderingGrunnlag
        abstract val omsorgsytersOmsorgsmånederForOmsorgsmottaker: YtelseMånederDb

        @JsonTypeName("OmsorgBarnFødtOmsorgsår")
        data class OmsorgBarnFødtOmsorgsår(
            override val aldersvurderingOmsorgsmottaker: AldersvurderingGrunnlag,
            override val omsorgsytersOmsorgsmånederForOmsorgsmottaker: YtelseMånederDb,
        ) : OmsorgBarnUnder6()

        @JsonTypeName("OmsorgBarnFødtDesemberOmsorgsår")
        data class OmsorgBarnFødtDesemberOmsorgsår(
            override val aldersvurderingOmsorgsmottaker: AldersvurderingGrunnlag,
            override val omsorgsytersOmsorgsmånederForOmsorgsmottaker: YtelseMånederDb,
        ) : OmsorgBarnUnder6()

        @JsonTypeName("OmsorgBarnFødtUtenforOmsorgsår")
        data class OmsorgBarnFødtUtenforOmsorgsår(
            override val aldersvurderingOmsorgsmottaker: AldersvurderingGrunnlag,
            override val omsorgsytersOmsorgsmånederForOmsorgsmottaker: YtelseMånederDb,
        ) : OmsorgBarnUnder6()
    }

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

    internal sealed class MedlemIFolketrygden : GrunnlagVilkårsvurderingDb() {
        abstract val omsorgsytersMedlemskapsmåneder: Set<YearMonth>

        @JsonTypeName("MedlemskapBarnFødtOmsorgsår")
        data class MedlemskapBarnFødtOmsorgsår(
            override val omsorgsytersMedlemskapsmåneder: Set<YearMonth>,
        ) : MedlemIFolketrygden()

        @JsonTypeName("MedlemskapBarnFødtDesemberOmsorgsår")
        data class MedlemskapBarnFødtDesemberOmsorgsår(
            override val omsorgsytersMedlemskapsmåneder: Set<YearMonth>,
        ) : MedlemIFolketrygden()

        @JsonTypeName("MedleskapBarnFødtUtenforOmsorgsår")
        data class MedleskapBarnFødtUtenforOmsorgsår(
            override val omsorgsytersMedlemskapsmåneder: Set<YearMonth>,
        ) : MedlemIFolketrygden()
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
                omsorgsmåneder = it.omsorgsmåneder.toDomain(),
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
    val omsorgsmåneder: YtelseMånederDb,
    val omsorgsår: Int,
)

internal fun OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Grunnlag.toDb(): GrunnlagVilkårsvurderingDb.MestAvAlleOmsorgsytere {
    return GrunnlagVilkårsvurderingDb.MestAvAlleOmsorgsytere(
        omsorgsyter = omsorgsyter,
        data = data.map {
            OmsorgsyterMottakerAntallMånederDb(
                omsorgsyter = it.omsorgsyter,
                omsorgsmottaker = it.omsorgsmottaker,
                omsorgsmåneder = it.omsorgsmåneder.toDb(),
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
    return when (this) {
        is OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtIOmsorgsår -> {
            GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.OmsorgBarnFødtOmsorgsår(
                aldersvurderingOmsorgsmottaker = aldersvurderingOmsorgsmottaker.toDb(),
                omsorgsytersOmsorgsmånederForOmsorgsmottaker = omsorgsytersOmsorgsmånederForOmsorgsmottaker.toDb(),
            )
        }

        is OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtUtenforOmsorgsår -> {
            GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.OmsorgBarnFødtUtenforOmsorgsår(
                aldersvurderingOmsorgsmottaker = aldersvurderingOmsorgsmottaker.toDb(),
                omsorgsytersOmsorgsmånederForOmsorgsmottaker = omsorgsytersOmsorgsmånederForOmsorgsmottaker.toDb()
            )
        }

        is OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtIDesemberOmsorgsår -> {
            GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.OmsorgBarnFødtDesemberOmsorgsår(
                aldersvurderingOmsorgsmottaker = aldersvurderingOmsorgsmottaker.toDb(),
                omsorgsytersOmsorgsmånederForOmsorgsmottaker = omsorgsytersOmsorgsmånederForOmsorgsmottaker.toDb()
            )
        }
    }
}

internal fun GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.toDomain(): OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag {
    return when (this) {
        is GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.OmsorgBarnFødtOmsorgsår -> {
            OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtIOmsorgsår(
                aldersvurderingOmsorgsmottaker = aldersvurderingOmsorgsmottaker.toDomain(),
                omsorgsytersOmsorgsmånederForOmsorgsmottaker = omsorgsytersOmsorgsmånederForOmsorgsmottaker.toDomain()
            )
        }

        is GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.OmsorgBarnFødtUtenforOmsorgsår -> {
            OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtUtenforOmsorgsår(
                aldersvurderingOmsorgsmottaker = aldersvurderingOmsorgsmottaker.toDomain(),
                omsorgsytersOmsorgsmånederForOmsorgsmottaker = omsorgsytersOmsorgsmånederForOmsorgsmottaker.toDomain()

            )
        }

        is GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.OmsorgBarnFødtDesemberOmsorgsår -> {
            OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtIDesemberOmsorgsår(
                aldersvurderingOmsorgsmottaker = aldersvurderingOmsorgsmottaker.toDomain(),
                omsorgsytersOmsorgsmånederForOmsorgsmottaker = omsorgsytersOmsorgsmånederForOmsorgsmottaker.toDomain(),
            )
        }
    }
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

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
sealed class YtelseMånederDb {
    @JsonTypeName("Barnetrygdmåneder")
    data class BarnetrygdMånederDb(
        val omsorgsmåneder: Set<YearMonth>,
    ) : YtelseMånederDb()

    @JsonTypeName("BarnetrygdOgHjelpestønadMånederDb")
    data class BarnetrygdOgHjelpestønadMånederDb(
        val omsorgsmåneder: Set<YearMonth>,
        val barnetrygdmåneder: Set<YearMonth>,
        val hjelpestønadmåneder: Set<YearMonth>,
    ) : YtelseMånederDb()
}

internal fun Omsorgsmåneder.toDb(): YtelseMånederDb {
    return when (this) {
        is Omsorgsmåneder.Barnetrygd -> {
            YtelseMånederDb.BarnetrygdMånederDb(this)
        }

        is Omsorgsmåneder.Hjelpestønad -> {
            YtelseMånederDb.BarnetrygdOgHjelpestønadMånederDb(
                omsorgsmåneder = this.måneder,
                barnetrygdmåneder = this.barnetrygd,
                hjelpestønadmåneder = this.hjelpestønad,
            )
        }
    }
}

internal fun YtelseMånederDb.toDomain(): Omsorgsmåneder {
    return when (this) {
        is YtelseMånederDb.BarnetrygdMånederDb -> {
            Omsorgsmåneder.Barnetrygd(
                måneder = this.omsorgsmåneder
            )
        }

        is YtelseMånederDb.BarnetrygdOgHjelpestønadMånederDb -> {
            Omsorgsmåneder.Hjelpestønad(
                måneder = this.omsorgsmåneder,
                barnetrygd = this.barnetrygdmåneder,
                hjelpestønad = this.hjelpestønadmåneder,
            )
        }
    }
}

