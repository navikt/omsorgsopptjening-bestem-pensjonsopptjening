package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.BarnetrygdGrunnlag

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
internal sealed class BeriketGrunnlagDb {
    abstract val omsorgsår: Int
    abstract val grunnlag: BeriketOmsorgsgrunnlagDb

    class FødtDesember(
        override val omsorgsår: Int,
        override val grunnlag: BeriketOmsorgsgrunnlagDb
    ) : BeriketGrunnlagDb()

    data class IkkeFødtDesember(
        override val omsorgsår: Int,
        override val grunnlag: BeriketOmsorgsgrunnlagDb
    ) : BeriketGrunnlagDb()

    data class IkkeFødtIOmsorgsår(
        override val omsorgsår: Int,
        override val grunnlag: BeriketOmsorgsgrunnlagDb
    ) : BeriketGrunnlagDb()
}

internal fun BarnetrygdGrunnlag.toDb(): BeriketGrunnlagDb {
    return when (this) {
        is BarnetrygdGrunnlag.FødtIOmsorgsår.FødtDesember -> {
            BeriketGrunnlagDb.FødtDesember(
                omsorgsår = omsorgsAr,
                grunnlag = grunnlag.toDb()
            )
        }

        is BarnetrygdGrunnlag.FødtIOmsorgsår.IkkeFødtDesember -> {
            BeriketGrunnlagDb.IkkeFødtDesember(
                omsorgsår = omsorgsAr,
                grunnlag = grunnlag.toDb()
            )
        }

        is BarnetrygdGrunnlag.IkkeFødtIOmsorgsår -> {
            BeriketGrunnlagDb.IkkeFødtIOmsorgsår(
                omsorgsår = omsorgsAr,
                grunnlag = grunnlag.toDb()
            )
        }
    }
}

internal fun BeriketGrunnlagDb.toDomain(): BarnetrygdGrunnlag {
    return when (this) {
        is BeriketGrunnlagDb.FødtDesember -> {
            BarnetrygdGrunnlag.FødtIOmsorgsår.FødtDesember(
                omsorgsAr = omsorgsår,
                grunnlag = grunnlag.toDomain()
            )
        }

        is BeriketGrunnlagDb.IkkeFødtDesember -> {
            BarnetrygdGrunnlag.FødtIOmsorgsår.IkkeFødtDesember(
                omsorgsAr = omsorgsår,
                grunnlag = grunnlag.toDomain()
            )
        }

        is BeriketGrunnlagDb.IkkeFødtIOmsorgsår -> {
            BarnetrygdGrunnlag.IkkeFødtIOmsorgsår(
                omsorgsAr = omsorgsår,
                grunnlag = grunnlag.toDomain()
            )
        }
    }
}