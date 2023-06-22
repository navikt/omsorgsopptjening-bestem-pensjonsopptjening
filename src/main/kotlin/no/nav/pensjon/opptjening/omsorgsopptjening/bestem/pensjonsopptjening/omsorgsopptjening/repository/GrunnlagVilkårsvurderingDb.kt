package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullOmsorgForBarnUnder6OgIngenHarLiktAntallMåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.KanKunGodskrivesEnOmsorgsyter
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.KanKunGodskrivesEtBarnPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.LiktAntallMånederOmsorg
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.PersonOgOmsorgsårGrunnlag
import java.util.UUID

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(
        value = GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.OmsorgBarnFødtOmsorgsår::class,
        name = "OmsorgBarnFødtOmsorgsår",
    ),
    JsonSubTypes.Type(
        value = GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.OmsorgBarnFødtDesemberOmsorgsår::class,
        name = "OmsorgBarnFødtDesemberOmsorgsår",
    ),
    JsonSubTypes.Type(
        value = GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.OmsorgBarnFødtUtenforOmsorgsår::class,
        name = "OmsorgBarnFødtUtenforOmsorgsår",
    ),
    JsonSubTypes.Type(
        value = GrunnlagVilkårsvurderingDb.PersonOgOmsorgsÅr::class,
        name = "OmsorgsyterOgOmsorgsÅr",
    ),
)
internal sealed class GrunnlagVilkårsvurderingDb {
    internal sealed class OmsorgBarnUnder6 : GrunnlagVilkårsvurderingDb() {
        abstract val omsorgsAr: Int
        abstract val omsorgsmottaker: PersonMedFødselsårDb

        data class OmsorgBarnFødtOmsorgsår(
            override val omsorgsAr: Int,
            override val omsorgsmottaker: PersonMedFødselsårDb,
            val minstEnMindFullOmsorg: Boolean,
        ) : OmsorgBarnUnder6()

        data class OmsorgBarnFødtDesemberOmsorgsår(
            override val omsorgsAr: Int,
            override val omsorgsmottaker: PersonMedFødselsårDb,
            val minstEnMånedFullOmsorgÅretEtterFødsel: Boolean,
        ) : OmsorgBarnUnder6()

        data class OmsorgBarnFødtUtenforOmsorgsår(
            override val omsorgsAr: Int,
            override val omsorgsmottaker: PersonMedFødselsårDb,
            val minstSeksMånederFullOmsorg: Boolean,
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

internal fun GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6OgIngenHarLikeMangeMåneder.toDomain(): FullOmsorgForBarnUnder6OgIngenHarLiktAntallMåneder.Grunnlag {
    return FullOmsorgForBarnUnder6OgIngenHarLiktAntallMåneder.Grunnlag(
        fullOmsorgForBarnUnder6Vurdering = barnUnder6.toDomain() as FullOmsorgForBarnUnder6.Vurdering,
        liktAntallMånederOmsorgVurdering = likeMangeMåneder.toDomain() as LiktAntallMånederOmsorg.Vurdering
    )
}


internal data class OmsorgsyterMottakerAntallMånederDb(
    val omsorgsyter: PersonMedFødselsårDb,
    val omsorgsmottaker: PersonMedFødselsårDb,
    val antallMåneder: Int
)

internal fun OmsorgsyterMottakerAntallMånederDb.toDomain(): LiktAntallMånederOmsorg.Grunnlag.YterMottakerManeder {
    return LiktAntallMånederOmsorg.Grunnlag.YterMottakerManeder(
        omsorgsyter = omsorgsyter.toDomain(),
        omsorgsmottaker = omsorgsmottaker.toDomain(),
        antallManeder = antallMåneder
    )
}

internal fun LiktAntallMånederOmsorg.Grunnlag.toDb(): GrunnlagVilkårsvurderingDb.LiktAntallMåneder {
    return GrunnlagVilkårsvurderingDb.LiktAntallMåneder(
        omsorgsyter = omsorgsyter.toDb(),
        andreOmsorgsytere = andreOmsorgsytere.map { it.toDb() }
    )
}

internal fun GrunnlagVilkårsvurderingDb.LiktAntallMåneder.toDomain(): LiktAntallMånederOmsorg.Grunnlag {
    return LiktAntallMånederOmsorg.Grunnlag(
        omsorgsyter = omsorgsyter.toDomain(),
        andreOmsorgsytere = andreOmsorgsytere.map { it.toDomain() }
    )
}

internal fun LiktAntallMånederOmsorg.Grunnlag.YterMottakerManeder.toDb(): OmsorgsyterMottakerAntallMånederDb {
    return OmsorgsyterMottakerAntallMånederDb(
        omsorgsyter = omsorgsyter.toDb(),
        omsorgsmottaker = omsorgsmottaker.toDb(),
        antallMåneder = antallManeder
    )
}

internal fun KanKunGodskrivesEnOmsorgsyter.Grunnlag.toDb(): GrunnlagVilkårsvurderingDb.KanKunGodskrivesEnOmsorgsyter {
    return GrunnlagVilkårsvurderingDb.KanKunGodskrivesEnOmsorgsyter(
        behandlingsIdUtfall = behandlingsIdUtfallListe.toDbs()
    )
}

data class BehandlingsIdUtfallDb(
    val behandlingsId: UUID,
    val erInnvilget: Boolean
)

internal fun List<KanKunGodskrivesEnOmsorgsyter.Grunnlag.BehandlingsIdUtfall>.toDbs(): List<BehandlingsIdUtfallDb> {
    return map { it.toDb() }
}

internal fun KanKunGodskrivesEnOmsorgsyter.Grunnlag.BehandlingsIdUtfall.toDb(): BehandlingsIdUtfallDb {
    return BehandlingsIdUtfallDb(
        behandlingsId = behandlingsId,
        erInnvilget = erInnvilget
    )
}

internal fun KanKunGodskrivesEtBarnPerÅr.Grunnlag.toDb(): GrunnlagVilkårsvurderingDb.KanKunGodskrivesEtBarnPerÅr {
    return GrunnlagVilkårsvurderingDb.KanKunGodskrivesEtBarnPerÅr(
        omsorgsmottaker = omsorgsmottaker,
        behandlinger = behandlinger.toDb()
    )
}

internal fun GrunnlagVilkårsvurderingDb.KanKunGodskrivesEtBarnPerÅr.toDomain(): KanKunGodskrivesEtBarnPerÅr.Grunnlag {
    return KanKunGodskrivesEtBarnPerÅr.Grunnlag(
        omsorgsmottaker = omsorgsmottaker,
        behandlinger = behandlinger.toDomain()
    )
}

internal fun GrunnlagVilkårsvurderingDb.KanKunGodskrivesEnOmsorgsyter.toDomain(): KanKunGodskrivesEnOmsorgsyter.Grunnlag {
    return KanKunGodskrivesEnOmsorgsyter.Grunnlag(
        behandlingsIdUtfallListe = behandlingsIdUtfall.toDomains()
    )
}

internal fun List<BehandlingsIdUtfallDb>.toDomains(): List<KanKunGodskrivesEnOmsorgsyter.Grunnlag.BehandlingsIdUtfall> {
    return map { it.toDomain() }
}

internal fun BehandlingsIdUtfallDb.toDomain(): KanKunGodskrivesEnOmsorgsyter.Grunnlag.BehandlingsIdUtfall {
    return KanKunGodskrivesEnOmsorgsyter.Grunnlag.BehandlingsIdUtfall(
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

internal fun List<KanKunGodskrivesEtBarnPerÅr.Grunnlag.AndreBehandlinger>.toDb(): List<AndreBehandlingerDb> {
    return map { it.toDb() }
}

internal fun List<AndreBehandlingerDb>.toDomain(): List<KanKunGodskrivesEtBarnPerÅr.Grunnlag.AndreBehandlinger> {
    return map { it.toDomain() }
}

internal fun KanKunGodskrivesEtBarnPerÅr.Grunnlag.AndreBehandlinger.toDb(): AndreBehandlingerDb {
    return AndreBehandlingerDb(
        behandlingsId = behandlingsId,
        år = år,
        omsorgsmottaker = omsorgsmottaker,
        erInnvilget = erInnvilget
    )
}

internal fun AndreBehandlingerDb.toDomain(): KanKunGodskrivesEtBarnPerÅr.Grunnlag.AndreBehandlinger {
    return KanKunGodskrivesEtBarnPerÅr.Grunnlag.AndreBehandlinger(
        behandlingsId = behandlingsId,
        år = år,
        omsorgsmottaker = omsorgsmottaker,
        erInnvilget = erInnvilget
    )
}

internal fun FullOmsorgForBarnUnder6.Grunnlag.toDb(): GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6 {
    return when (this) {
        is FullOmsorgForBarnUnder6.Grunnlag.OmsorgsmottakerFødtIOmsorgsår -> {
            GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.OmsorgBarnFødtOmsorgsår(
                omsorgsAr = omsorgsAr,
                omsorgsmottaker = omsorgsmottaker.toDb(),
                minstEnMindFullOmsorg = minstEnMånedFullOmsorg
            )
        }

        is FullOmsorgForBarnUnder6.Grunnlag.OmsorgsmottakerFødtUtenforOmsorgsår -> {
            GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.OmsorgBarnFødtUtenforOmsorgsår(
                omsorgsAr = omsorgsAr,
                omsorgsmottaker = omsorgsmottaker.toDb(),
                minstSeksMånederFullOmsorg = minstSeksMånederFullOmsorg
            )
        }

        is FullOmsorgForBarnUnder6.Grunnlag.OmsorgsmottakerFødtIDesemberOmsorgsår -> {
            GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.OmsorgBarnFødtDesemberOmsorgsår(
                omsorgsAr = omsorgsAr,
                omsorgsmottaker = omsorgsmottaker.toDb(),
                minstEnMånedFullOmsorgÅretEtterFødsel = minstEnMånedOmsorgÅretEtterFødsel
            )
        }
    }
}

internal fun GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.toDomain(): FullOmsorgForBarnUnder6.Grunnlag {
    return when (this) {
        is GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.OmsorgBarnFødtOmsorgsår -> {
            FullOmsorgForBarnUnder6.Grunnlag.OmsorgsmottakerFødtIOmsorgsår(
                omsorgsAr = omsorgsAr,
                omsorgsmottaker = omsorgsmottaker.toDomain(),
                minstEnMånedFullOmsorg = minstEnMindFullOmsorg
            )
        }

        is GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.OmsorgBarnFødtUtenforOmsorgsår -> {
            FullOmsorgForBarnUnder6.Grunnlag.OmsorgsmottakerFødtUtenforOmsorgsår(
                omsorgsAr = omsorgsAr,
                omsorgsmottaker = omsorgsmottaker.toDomain(),
                minstSeksMånederFullOmsorg = minstSeksMånederFullOmsorg

            )
        }

        is GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.OmsorgBarnFødtDesemberOmsorgsår -> {
            FullOmsorgForBarnUnder6.Grunnlag.OmsorgsmottakerFødtIDesemberOmsorgsår(
                omsorgsAr = omsorgsAr,
                omsorgsmottaker = omsorgsmottaker.toDomain(),
                minstEnMånedOmsorgÅretEtterFødsel = minstEnMånedFullOmsorgÅretEtterFødsel
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

