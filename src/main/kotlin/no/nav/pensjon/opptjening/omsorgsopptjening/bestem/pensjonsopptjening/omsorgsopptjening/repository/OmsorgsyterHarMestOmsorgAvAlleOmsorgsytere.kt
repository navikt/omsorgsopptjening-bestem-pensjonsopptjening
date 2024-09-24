package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.GyldigeOmsorgsmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere
import java.time.YearMonth

@JsonTypeName("OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere")
internal data class OmsorgsyterHarMestOmsorgAvAlleOmsorgsytereDb(
    val grunnlag: MestAvAlleOmsorgsytere,
    val utfall: VilkårsvurderingUtfallDb,
) : VilkårsvurderingDb()

internal fun OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering.toDb(): OmsorgsyterHarMestOmsorgAvAlleOmsorgsytereDb {
    return OmsorgsyterHarMestOmsorgAvAlleOmsorgsytereDb(
        grunnlag = grunnlag.toDb(),
        utfall = utfall.toDb()
    )
}

internal fun OmsorgsyterHarMestOmsorgAvAlleOmsorgsytereDb.toDomain(): OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering {
    return OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering(
        grunnlag = grunnlag.toDomain(),
        utfall = utfall.toDomain()
    )
}

@JsonTypeName("MestAvAlleOmsorgsytere")
internal data class MestAvAlleOmsorgsytere(
    val omsorgsyter: String,
    val data: List<OmsorgsyterMottakerAntallMånederDb>
) : GrunnlagVilkårsvurderingDb()

internal fun MestAvAlleOmsorgsytere.toDomain(): OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Grunnlag {
    return OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Grunnlag(
        omsorgsyter = omsorgsyter,
        data = data.map {
            OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.OmsorgsmånederForMottakerOgÅr(
                omsorgsyter = it.omsorgsyter,
                omsorgsmottaker = it.omsorgsmottaker,
                omsorgsmåneder = GyldigeOmsorgsmåneder(it.omsorgsmåneder),
                omsorgsår = it.omsorgsår
            )
        }
    )
}

internal fun OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Grunnlag.toDb(): MestAvAlleOmsorgsytere {
    return MestAvAlleOmsorgsytere(
        omsorgsyter = omsorgsyter,
        data = data.map {
            OmsorgsyterMottakerAntallMånederDb(
                omsorgsyter = it.omsorgsyter,
                omsorgsmottaker = it.omsorgsmottaker,
                omsorgsmåneder = it.omsorgsmåneder.alleMåneder(),
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
    val omsorgsmåneder: Set<YearMonth>,
    val omsorgsår: Int,
)

