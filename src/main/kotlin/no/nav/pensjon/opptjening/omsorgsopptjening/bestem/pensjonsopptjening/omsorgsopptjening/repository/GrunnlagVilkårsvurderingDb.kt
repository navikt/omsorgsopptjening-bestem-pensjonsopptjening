package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterOgOmsorgsårGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullOmsorgForBarnUnder6Grunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsmottakerFødtIDesemberOmsorgsårGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsmottakerFødtIOmsorgsårGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsmottakerFødtUtenforOmsorgsårGrunnlag

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
        value = GrunnlagVilkårsvurderingDb.OmsorgsyterOgOmsorgsÅr::class,
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

    data class OmsorgsyterOgOmsorgsÅr(
        val omsorgsyter: PersonMedFødselsårDb,
        val omsorgsAr: Int
    ) : GrunnlagVilkårsvurderingDb()
}

internal fun FullOmsorgForBarnUnder6Grunnlag.toDb(): GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6 {
    return when (this) {
        is OmsorgsmottakerFødtIOmsorgsårGrunnlag -> {
            GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.OmsorgBarnFødtOmsorgsår(
                omsorgsAr = omsorgsAr,
                omsorgsmottaker = omsorgsmottaker.toDb(),
                minstEnMindFullOmsorg = minstEnMånedFullOmsorg
            )
        }

        is OmsorgsmottakerFødtUtenforOmsorgsårGrunnlag -> {
            GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.OmsorgBarnFødtUtenforOmsorgsår(
                omsorgsAr = omsorgsAr,
                omsorgsmottaker = omsorgsmottaker.toDb(),
                minstSeksMånederFullOmsorg = minstSeksMånederFullOmsorg
            )
        }

        is OmsorgsmottakerFødtIDesemberOmsorgsårGrunnlag -> {
            GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.OmsorgBarnFødtDesemberOmsorgsår(
                omsorgsAr = omsorgsAr,
                omsorgsmottaker = omsorgsmottaker.toDb(),
                minstEnMånedFullOmsorgÅretEtterFødsel = minstEnMånedOmsorgÅretEtterFødsel
            )
        }
    }
}

internal fun GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.toDomain(): FullOmsorgForBarnUnder6Grunnlag {
    return when (this) {
        is GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.OmsorgBarnFødtOmsorgsår -> {
            OmsorgsmottakerFødtIOmsorgsårGrunnlag(
                omsorgsAr = omsorgsAr,
                omsorgsmottaker = omsorgsmottaker.toDomain(),
                minstEnMånedFullOmsorg = minstEnMindFullOmsorg
            )
        }

        is GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.OmsorgBarnFødtUtenforOmsorgsår -> {
            OmsorgsmottakerFødtUtenforOmsorgsårGrunnlag(
                omsorgsAr = omsorgsAr,
                omsorgsmottaker = omsorgsmottaker.toDomain(),
                minstSeksMånederFullOmsorg = minstSeksMånederFullOmsorg

            )
        }

        is GrunnlagVilkårsvurderingDb.OmsorgBarnUnder6.OmsorgBarnFødtDesemberOmsorgsår -> {
            OmsorgsmottakerFødtIDesemberOmsorgsårGrunnlag(
                omsorgsAr = omsorgsAr,
                omsorgsmottaker = omsorgsmottaker.toDomain(),
                minstEnMånedOmsorgÅretEtterFødsel = minstEnMånedFullOmsorgÅretEtterFødsel
            )
        }
    }
}

internal fun GrunnlagVilkårsvurderingDb.OmsorgsyterOgOmsorgsÅr.toDomain(): OmsorgsyterOgOmsorgsårGrunnlag {
    return OmsorgsyterOgOmsorgsårGrunnlag(
        omsorgsyter = omsorgsyter.toDomain(),
        omsorgsAr = omsorgsAr
    )
}


