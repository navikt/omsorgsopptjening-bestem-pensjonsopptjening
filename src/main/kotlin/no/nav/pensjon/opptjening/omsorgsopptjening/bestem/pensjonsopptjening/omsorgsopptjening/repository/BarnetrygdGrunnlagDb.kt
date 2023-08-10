package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.BarnetrygdGrunnlag

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
internal sealed class BarnetrygdGrunnlagDb {
    abstract val omsorgsår: Int
    abstract val omsorgsmottaker: PersonDb
    abstract val grunnlag: BeriketDatagrunnlagDb

    @JsonTypeName("FødtDesember")
    class FødtDesember(
        override val omsorgsår: Int,
        override val omsorgsmottaker: PersonDb,
        override val grunnlag: BeriketDatagrunnlagDb
    ) : BarnetrygdGrunnlagDb()

    @JsonTypeName("IkkeFødtDesember")
    data class IkkeFødtDesember(
        override val omsorgsår: Int,
        override val omsorgsmottaker: PersonDb,
        override val grunnlag: BeriketDatagrunnlagDb
    ) : BarnetrygdGrunnlagDb()

    @JsonTypeName("IkkeFødtIOmsorgsår")
    data class IkkeFødtIOmsorgsår(
        override val omsorgsår: Int,
        override val omsorgsmottaker: PersonDb,
        override val grunnlag: BeriketDatagrunnlagDb
    ) : BarnetrygdGrunnlagDb()
}

internal fun BarnetrygdGrunnlag.toDb(): BarnetrygdGrunnlagDb {
    return when (this) {
        is BarnetrygdGrunnlag.FødtIOmsorgsår.FødtDesember -> {
            BarnetrygdGrunnlagDb.FødtDesember(
                omsorgsår = omsorgsAr,
                omsorgsmottaker = omsorgsmottaker.toDb(),
                grunnlag = grunnlag.toDb()
            )
        }

        is BarnetrygdGrunnlag.FødtIOmsorgsår.IkkeFødtDesember -> {
            BarnetrygdGrunnlagDb.IkkeFødtDesember(
                omsorgsår = omsorgsAr,
                omsorgsmottaker = omsorgsmottaker.toDb(),
                grunnlag = grunnlag.toDb()
            )
        }

        is BarnetrygdGrunnlag.IkkeFødtIOmsorgsår -> {
            BarnetrygdGrunnlagDb.IkkeFødtIOmsorgsår(
                omsorgsår = omsorgsAr,
                omsorgsmottaker = omsorgsmottaker.toDb(),
                grunnlag = grunnlag.toDb()
            )
        }
    }
}

internal fun BarnetrygdGrunnlagDb.toDomain(): BarnetrygdGrunnlag {
    return when (this) {
        is BarnetrygdGrunnlagDb.FødtDesember -> {
            BarnetrygdGrunnlag.FødtIOmsorgsår.FødtDesember(
                omsorgsAr = omsorgsår,
                omsorgsmottaker = omsorgsmottaker.toDomain(),
                grunnlag = grunnlag.toDomain()
            )
        }

        is BarnetrygdGrunnlagDb.IkkeFødtDesember -> {
            BarnetrygdGrunnlag.FødtIOmsorgsår.IkkeFødtDesember(
                omsorgsAr = omsorgsår,
                omsorgsmottaker = omsorgsmottaker.toDomain(),
                grunnlag = grunnlag.toDomain()
            )
        }

        is BarnetrygdGrunnlagDb.IkkeFødtIOmsorgsår -> {
            BarnetrygdGrunnlag.IkkeFødtIOmsorgsår(
                omsorgsAr = omsorgsår,
                omsorgsmottaker = omsorgsmottaker.toDomain(),
                grunnlag = grunnlag.toDomain()
            )
        }
    }
}