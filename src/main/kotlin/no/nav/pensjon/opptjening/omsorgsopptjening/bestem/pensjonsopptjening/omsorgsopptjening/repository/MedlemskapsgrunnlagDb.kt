package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Medlemskapsgrunnlag
import java.time.YearMonth

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonTypeName("MedlemskapsgrunnlagDb")
internal data class MedlemskapsgrunnlagDb(
    val unntaksperioder: List<UnntaksperiodeDb>,
    val rådata: String,
)

internal fun Medlemskapsgrunnlag.toDb(): MedlemskapsgrunnlagDb {
    return MedlemskapsgrunnlagDb(
        unntaksperioder = unntaksperioder.toDb(),
        rådata = rådata,
    )
}

internal fun MedlemskapsgrunnlagDb.toDomain(): Medlemskapsgrunnlag {
    return Medlemskapsgrunnlag(
        unntaksperioder = unntaksperioder.toDomain(),
        rådata = rådata,
    )
}

internal fun List<UnntaksperiodeDb>.toDomain(): List<Medlemskapsgrunnlag.Unntaksperiode> {
    return map { it.toDomain() }
}

internal fun Medlemskapsgrunnlag.Unntaksperiode.toDb(): UnntaksperiodeDb {
    return UnntaksperiodeDb(
        fraOgMed = this.fraOgMed,
        tilOgMed = this.tilOgMed,
    )
}

internal fun UnntaksperiodeDb.toDomain(): Medlemskapsgrunnlag.Unntaksperiode {
    return Medlemskapsgrunnlag.Unntaksperiode(
        fraOgMed = this.fraOgMed,
        tilOgMed = this.tilOgMed,
    )
}

internal fun List<Medlemskapsgrunnlag.Unntaksperiode>.toDb(): List<UnntaksperiodeDb> {
    return map { it.toDb() }
}

internal data class UnntaksperiodeDb(
    val fraOgMed: YearMonth,
    val tilOgMed: YearMonth,
)
