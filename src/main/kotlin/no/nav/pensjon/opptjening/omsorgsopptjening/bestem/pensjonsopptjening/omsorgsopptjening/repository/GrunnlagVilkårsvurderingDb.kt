package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarTilstrekkeligOmsorgsarbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.PersonOgOmsorgsårGrunnlag
import java.util.UUID

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
internal sealed class GrunnlagVilkårsvurderingDb {
    internal sealed class OmsorgBarnUnder6 : GrunnlagVilkårsvurderingDb() {
        abstract val omsorgsAr: Int
        abstract val omsorgsmottaker: PersonMedFødselsårDb
        abstract val antallMåneder: Int

        data class OmsorgBarnFødtOmsorgsår(
            override val omsorgsAr: Int,
            override val omsorgsmottaker: PersonMedFødselsårDb,
            override val antallMåneder: Int,
        ) : OmsorgBarnUnder6()

        data class OmsorgBarnFødtDesemberOmsorgsår(
            override val omsorgsAr: Int,
            override val omsorgsmottaker: PersonMedFødselsårDb,
            override val antallMåneder: Int,
        ) : OmsorgBarnUnder6()

        data class OmsorgBarnFødtUtenforOmsorgsår(
            override val omsorgsAr: Int,
            override val omsorgsmottaker: PersonMedFødselsårDb,
            override val antallMåneder: Int,
        ) : OmsorgBarnUnder6()
    }

    data class PersonOgOmsorgsÅr(
        val person: PersonMedFødselsårDb,
        val omsorgsAr: Int
    ) : GrunnlagVilkårsvurderingDb()

    data class KanKunGodskrivesEnOmsorgsyter(
        val omsorgsAr: Int,
        val behandlinger: List<FullførteBehandlingerForOmsorgsmottakerDb>
    ) : GrunnlagVilkårsvurderingDb()

    data class KanKunGodskrivesEtBarnPerÅr(
        val omsorgsmottaker: String,
        val omsorgsAr: Int,
        val behandlinger: List<FullførteBehandlingForOmsorgsyterDb>
    ) : GrunnlagVilkårsvurderingDb()

    data class LiktAntallMåneder(
        val omsorgsyter: OmsorgsyterMottakerAntallMånederDb,
        val andreOmsorgsytere: List<OmsorgsyterMottakerAntallMånederDb>
    ) : GrunnlagVilkårsvurderingDb()

    data class OmsorgBarnUnder6OgIngenHarLikeMangeMåneder(
        val barnUnder6: VilkårsvurderingDb,
        val likeMangeMåneder: VilkårsvurderingDb
    ) : GrunnlagVilkårsvurderingDb()

    data class MestAvAlleOmsorgsytere(
        val omsorgsyter: PersonMedFødselsårDb,
        val data: List<OmsorgsyterMottakerAntallMånederDb>
    ) : GrunnlagVilkårsvurderingDb()
}

internal fun GrunnlagVilkårsvurderingDb.MestAvAlleOmsorgsytere.toDomain(): OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Grunnlag {
    return OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Grunnlag(
        omsorgsyter = omsorgsyter.toDomain(),
        summert = data.map {
            OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.SummertOmsorgForMottakerOgÅr(
                omsorgsyter = it.omsorgsyter.toDomain(),
                omsorgsmottaker = it.omsorgsmottaker.toDomain(),
                antallMåneder = it.antallMåneder,
                år = it.år
            )
        }
    )
}


internal data class OmsorgsyterMottakerAntallMånederDb(
    val omsorgsyter: PersonMedFødselsårDb,
    val omsorgsmottaker: PersonMedFødselsårDb,
    val antallMåneder: Int,
    val år: Int,
)

internal fun OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Grunnlag.toDb(): GrunnlagVilkårsvurderingDb.MestAvAlleOmsorgsytere {
    return GrunnlagVilkårsvurderingDb.MestAvAlleOmsorgsytere(
        omsorgsyter = omsorgsyter.toDb(),
        data = summert.map {
            OmsorgsyterMottakerAntallMånederDb(
                omsorgsyter = it.omsorgsyter.toDb(),
                omsorgsmottaker = it.omsorgsmottaker.toDb(),
                antallMåneder = it.antallMåneder,
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
                antallMåneder = antallMåneder
            )
        }

        is OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtUtenforOmsorgsår -> {
            GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.OmsorgBarnFødtUtenforOmsorgsår(
                omsorgsAr = omsorgsAr,
                omsorgsmottaker = omsorgsmottaker.toDb(),
                antallMåneder = antallMåneder
            )
        }

        is OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtIDesemberOmsorgsår -> {
            GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.OmsorgBarnFødtDesemberOmsorgsår(
                omsorgsAr = omsorgsAr,
                omsorgsmottaker = omsorgsmottaker.toDb(),
                antallMåneder = antallMåneder
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
                antallMåneder = antallMåneder
            )
        }

        is GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.OmsorgBarnFødtUtenforOmsorgsår -> {
            OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtUtenforOmsorgsår(
                omsorgsAr = omsorgsAr,
                omsorgsmottaker = omsorgsmottaker.toDomain(),
                antallMåneder = antallMåneder

            )
        }

        is GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.OmsorgBarnFødtDesemberOmsorgsår -> {
            OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtIDesemberOmsorgsår(
                omsorgsAr = omsorgsAr,
                omsorgsmottaker = omsorgsmottaker.toDomain(),
                antallMåneder = antallMåneder
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

