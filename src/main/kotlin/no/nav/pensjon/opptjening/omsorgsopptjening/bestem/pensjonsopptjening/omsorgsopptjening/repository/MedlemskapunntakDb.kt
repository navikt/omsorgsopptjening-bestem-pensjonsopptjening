package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Medlemskapsunntak
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.MedlemskapsunntakPeriode
import java.time.YearMonth

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonTypeName("MedlemskapsunntakDb")
internal data class MedlemskapsunntakDb(
    val ikkeMedlem: Set<MedlemskapsunntakPeriodeDb>,
    val pliktigEllerFrivillig: Set<MedlemskapsunntakPeriodeDb>,
    val rådata: String,
)

internal fun Medlemskapsunntak.toDb(): MedlemskapsunntakDb {
    return MedlemskapsunntakDb(
        ikkeMedlem = ikkeMedlem.toDb(),
        pliktigEllerFrivillig = pliktigEllerFrivillig.toDb(),
        rådata = rådata,
    )
}

internal fun MedlemskapsunntakDb.toDomain(): Medlemskapsunntak {
    return Medlemskapsunntak(
        ikkeMedlem = ikkeMedlem.toDomain(),
        pliktigEllerFrivillig = pliktigEllerFrivillig.toDomain(),
        rådata = rådata
    )
}

internal fun Set<MedlemskapsunntakPeriodeDb>.toDomain(): Set<MedlemskapsunntakPeriode> {
    return map { it.toDomain() }.toSet()
}

internal fun MedlemskapsunntakPeriode.toDb(): MedlemskapsunntakPeriodeDb {
    return MedlemskapsunntakPeriodeDb(
        fraOgMed = fraOgMed,
        tilOgMed = tilOgMed,
    )
}

internal fun MedlemskapsunntakPeriodeDb.toDomain(): MedlemskapsunntakPeriode {
    return MedlemskapsunntakPeriode(
        fraOgMed = fraOgMed,
        tilOgMed = tilOgMed,
    )
}

internal fun Set<MedlemskapsunntakPeriode>.toDb(): Set<MedlemskapsunntakPeriodeDb> {
    return map { it.toDb() }.toSet()
}

internal data class MedlemskapsunntakPeriodeDb(
    val fraOgMed: YearMonth,
    val tilOgMed: YearMonth,
)