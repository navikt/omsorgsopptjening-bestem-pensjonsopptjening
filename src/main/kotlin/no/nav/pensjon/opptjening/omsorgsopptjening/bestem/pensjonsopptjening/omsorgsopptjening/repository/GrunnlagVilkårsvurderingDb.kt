package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanIkkeGisHvisTilnærmetLikeMyeOmsorgsarbeidBlantFlereOmsorgsytere
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyter
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarTilstrekkeligOmsorgsarbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarTilstrekkeligOmsorgsarbeidOgIngenAndreOmsorgsyterHarLikeMyeOmsorgsarbeid
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
        val behandlingsIdUtfall: List<BehandlingsIdUtfallDb>
    ) : GrunnlagVilkårsvurderingDb()

    data class KanKunGodskrivesEtBarnPerÅr(
        val omsorgsmottaker: String,
        val behandlinger: List<AndreBehandlingerDb>
    ) : GrunnlagVilkårsvurderingDb()

    data class LiktAntallMåneder(
        val omsorgsyter: OmsorgsyterMottakerAntallMånederDb,
        val andreOmsorgsytere: List<OmsorgsyterMottakerAntallMånederDb>
    ) : GrunnlagVilkårsvurderingDb()

    data class OmsorgBarnUnder6OgIngenHarLikeMangeMåneder(
        val barnUnder6: VilkårsvurderingDb,
        val likeMangeMåneder: VilkårsvurderingDb
    ) : GrunnlagVilkårsvurderingDb()
}

internal fun GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6OgIngenHarLikeMangeMåneder.toDomain(): OmsorgsyterHarTilstrekkeligOmsorgsarbeidOgIngenAndreOmsorgsyterHarLikeMyeOmsorgsarbeid.Grunnlag {
    return OmsorgsyterHarTilstrekkeligOmsorgsarbeidOgIngenAndreOmsorgsyterHarLikeMyeOmsorgsarbeid.Grunnlag(
        fullOmsorgForBarnUnder6Vurdering = barnUnder6.toDomain() as OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering,
        liktAntallMånederOmsorgVurdering = likeMangeMåneder.toDomain() as OmsorgsopptjeningKanIkkeGisHvisTilnærmetLikeMyeOmsorgsarbeidBlantFlereOmsorgsytere.Vurdering
    )
}


internal data class OmsorgsyterMottakerAntallMånederDb(
    val omsorgsyter: PersonMedFødselsårDb,
    val omsorgsmottaker: PersonMedFødselsårDb,
    val antallMåneder: Int
)

internal fun OmsorgsyterMottakerAntallMånederDb.toDomain(): OmsorgsopptjeningKanIkkeGisHvisTilnærmetLikeMyeOmsorgsarbeidBlantFlereOmsorgsytere.Grunnlag.YterMottakerManeder {
    return OmsorgsopptjeningKanIkkeGisHvisTilnærmetLikeMyeOmsorgsarbeidBlantFlereOmsorgsytere.Grunnlag.YterMottakerManeder(
        omsorgsyter = omsorgsyter.toDomain(),
        omsorgsmottaker = omsorgsmottaker.toDomain(),
        antallManeder = antallMåneder
    )
}

internal fun OmsorgsopptjeningKanIkkeGisHvisTilnærmetLikeMyeOmsorgsarbeidBlantFlereOmsorgsytere.Grunnlag.toDb(): GrunnlagVilkårsvurderingDb.LiktAntallMåneder {
    return GrunnlagVilkårsvurderingDb.LiktAntallMåneder(
        omsorgsyter = omsorgsyter.toDb(),
        andreOmsorgsytere = andreOmsorgsytere.map { it.toDb() }
    )
}

internal fun GrunnlagVilkårsvurderingDb.LiktAntallMåneder.toDomain(): OmsorgsopptjeningKanIkkeGisHvisTilnærmetLikeMyeOmsorgsarbeidBlantFlereOmsorgsytere.Grunnlag {
    return OmsorgsopptjeningKanIkkeGisHvisTilnærmetLikeMyeOmsorgsarbeidBlantFlereOmsorgsytere.Grunnlag(
        omsorgsyter = omsorgsyter.toDomain(),
        andreOmsorgsytere = andreOmsorgsytere.map { it.toDomain() }
    )
}

internal fun OmsorgsopptjeningKanIkkeGisHvisTilnærmetLikeMyeOmsorgsarbeidBlantFlereOmsorgsytere.Grunnlag.YterMottakerManeder.toDb(): OmsorgsyterMottakerAntallMånederDb {
    return OmsorgsyterMottakerAntallMånederDb(
        omsorgsyter = omsorgsyter.toDb(),
        omsorgsmottaker = omsorgsmottaker.toDb(),
        antallMåneder = antallManeder
    )
}

internal fun OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyter.Grunnlag.toDb(): GrunnlagVilkårsvurderingDb.KanKunGodskrivesEnOmsorgsyter {
    return GrunnlagVilkårsvurderingDb.KanKunGodskrivesEnOmsorgsyter(
        behandlingsIdUtfall = behandlingsIdUtfallListe.toDbs()
    )
}

data class BehandlingsIdUtfallDb(
    val behandlingsId: UUID,
    val erInnvilget: Boolean
)

internal fun List<OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyter.Grunnlag.BehandlingsIdUtfall>.toDbs(): List<BehandlingsIdUtfallDb> {
    return map { it.toDb() }
}

internal fun OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyter.Grunnlag.BehandlingsIdUtfall.toDb(): BehandlingsIdUtfallDb {
    return BehandlingsIdUtfallDb(
        behandlingsId = behandlingsId,
        erInnvilget = erInnvilget
    )
}

internal fun OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag.toDb(): GrunnlagVilkårsvurderingDb.KanKunGodskrivesEtBarnPerÅr {
    return GrunnlagVilkårsvurderingDb.KanKunGodskrivesEtBarnPerÅr(
        omsorgsmottaker = omsorgsmottaker,
        behandlinger = behandlinger.toDb()
    )
}

internal fun GrunnlagVilkårsvurderingDb.KanKunGodskrivesEtBarnPerÅr.toDomain(): OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag {
    return OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag(
        omsorgsmottaker = omsorgsmottaker,
        behandlinger = behandlinger.toDomain()
    )
}

internal fun GrunnlagVilkårsvurderingDb.KanKunGodskrivesEnOmsorgsyter.toDomain(): OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyter.Grunnlag {
    return OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyter.Grunnlag(
        behandlingsIdUtfallListe = behandlingsIdUtfall.toDomains()
    )
}

internal fun List<BehandlingsIdUtfallDb>.toDomains(): List<OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyter.Grunnlag.BehandlingsIdUtfall> {
    return map { it.toDomain() }
}

internal fun BehandlingsIdUtfallDb.toDomain(): OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyter.Grunnlag.BehandlingsIdUtfall {
    return OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyter.Grunnlag.BehandlingsIdUtfall(
        behandlingsId = behandlingsId,
        erInnvilget = erInnvilget
    )
}

internal data class AndreBehandlingerDb(
    val behandlingsId: UUID,
    val år: Int,
    val omsorgsmottaker: String,
    val erInnvilget: Boolean
)

internal fun List<OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag.AndreBehandlinger>.toDb(): List<AndreBehandlingerDb> {
    return map { it.toDb() }
}

internal fun List<AndreBehandlingerDb>.toDomain(): List<OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag.AndreBehandlinger> {
    return map { it.toDomain() }
}

internal fun OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag.AndreBehandlinger.toDb(): AndreBehandlingerDb {
    return AndreBehandlingerDb(
        behandlingsId = behandlingsId,
        år = år,
        omsorgsmottaker = omsorgsmottaker,
        erInnvilget = erInnvilget
    )
}

internal fun AndreBehandlingerDb.toDomain(): OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag.AndreBehandlinger {
    return OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag.AndreBehandlinger(
        behandlingsId = behandlingsId,
        år = år,
        omsorgsmottaker = omsorgsmottaker,
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

