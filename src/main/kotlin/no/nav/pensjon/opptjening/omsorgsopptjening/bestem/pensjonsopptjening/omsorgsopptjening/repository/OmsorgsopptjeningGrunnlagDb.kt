package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningGrunnlag

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
internal sealed class OmsorgsopptjeningGrunnlagDb {
    abstract val omsorgsår: Int
    abstract val omsorgsmottaker: PersonDb
    abstract val grunnlag: BeriketDatagrunnlagDb

    @JsonTypeName("FødtDesember")
    class FødtDesember(
        override val omsorgsår: Int,
        override val omsorgsmottaker: PersonDb,
        override val grunnlag: BeriketDatagrunnlagDb
    ) : OmsorgsopptjeningGrunnlagDb()

    @JsonTypeName("IkkeFødtDesember")
    data class IkkeFødtDesember(
        override val omsorgsår: Int,
        override val omsorgsmottaker: PersonDb,
        override val grunnlag: BeriketDatagrunnlagDb
    ) : OmsorgsopptjeningGrunnlagDb()

    @JsonTypeName("IkkeFødtIOmsorgsår")
    data class IkkeFødtIOmsorgsår(
        override val omsorgsår: Int,
        override val omsorgsmottaker: PersonDb,
        override val grunnlag: BeriketDatagrunnlagDb
    ) : OmsorgsopptjeningGrunnlagDb()
}

internal fun OmsorgsopptjeningGrunnlag.toDb(): OmsorgsopptjeningGrunnlagDb {
    return when (this) {
        is OmsorgsopptjeningGrunnlag.FødtIOmsorgsår.FødtDesember -> {
            OmsorgsopptjeningGrunnlagDb.FødtDesember(
                omsorgsår = omsorgsAr,
                omsorgsmottaker = omsorgsmottaker.toDb(),
                grunnlag = grunnlag.toDb()
            )
        }

        is OmsorgsopptjeningGrunnlag.FødtIOmsorgsår.IkkeFødtDesember -> {
            OmsorgsopptjeningGrunnlagDb.IkkeFødtDesember(
                omsorgsår = omsorgsAr,
                omsorgsmottaker = omsorgsmottaker.toDb(),
                grunnlag = grunnlag.toDb()
            )
        }

        is OmsorgsopptjeningGrunnlag.IkkeFødtIOmsorgsår -> {
            OmsorgsopptjeningGrunnlagDb.IkkeFødtIOmsorgsår(
                omsorgsår = omsorgsAr,
                omsorgsmottaker = omsorgsmottaker.toDb(),
                grunnlag = grunnlag.toDb()
            )
        }
    }
}

internal fun OmsorgsopptjeningGrunnlagDb.toDomain(): OmsorgsopptjeningGrunnlag {
    return when (this) {
        is OmsorgsopptjeningGrunnlagDb.FødtDesember -> {
            OmsorgsopptjeningGrunnlag.FødtIOmsorgsår.FødtDesember(
                omsorgsAr = omsorgsår,
                omsorgsmottaker = omsorgsmottaker.toDomain(),
                grunnlag = grunnlag.toDomain()
            )
        }

        is OmsorgsopptjeningGrunnlagDb.IkkeFødtDesember -> {
            OmsorgsopptjeningGrunnlag.FødtIOmsorgsår.IkkeFødtDesember(
                omsorgsAr = omsorgsår,
                omsorgsmottaker = omsorgsmottaker.toDomain(),
                grunnlag = grunnlag.toDomain()
            )
        }

        is OmsorgsopptjeningGrunnlagDb.IkkeFødtIOmsorgsår -> {
            OmsorgsopptjeningGrunnlag.IkkeFødtIOmsorgsår(
                omsorgsAr = omsorgsår,
                omsorgsmottaker = omsorgsmottaker.toDomain(),
                grunnlag = grunnlag.toDomain()
            )
        }
    }
}