package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.EllerVurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OgVurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsmottakerOppfyllerAlderskravForBarnetrygd
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsmottakerOppfyllerAlderskravForHjelpestønad
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterErForelderTilMottakerAvHjelpestønad
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterErMedlemIFolketrygden
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterErikkeOmsorgsmottaker
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarGyldigOmsorgsarbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarIkkeDødsdato
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarTilstrekkeligOmsorgsarbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterMottarBarnetrgyd
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterOppfyllerAlderskrav
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.VilkarsVurdering
import java.util.Queue

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
sealed class VilkårsvurderingDb

internal fun VilkarsVurdering<*>.toDb(): VilkårsvurderingDb {
    return when (this) {
        is EllerVurdering -> toDb()
        is OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering -> toDb()
        is OgVurdering -> toDb()
        is OmsorgsyterOppfyllerAlderskrav.Vurdering -> toDb()
        is OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Vurdering -> toDb()
        is OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering -> toDb()
        is OmsorgsmottakerOppfyllerAlderskravForBarnetrygd.Vurdering -> toDb()
        is OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering -> toDb()
        is OmsorgsmottakerOppfyllerAlderskravForHjelpestønad.Vurdering -> toDb()
        is OmsorgsyterErForelderTilMottakerAvHjelpestønad.Vurdering -> toDb()
        is OmsorgsyterMottarBarnetrgyd.Vurdering -> toDb()
        is OmsorgsyterHarGyldigOmsorgsarbeid.Vurdering -> toDb()
        is OmsorgsyterErMedlemIFolketrygden.Vurdering -> toDb()
        is OmsorgsyterErikkeOmsorgsmottaker.Vurdering -> toDb()
        is OmsorgsyterHarIkkeDødsdato.Vurdering -> toDb()
        is OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.Vurdering -> toDb()
    }
}

internal fun mapRecursive(
    items: Queue<VilkarsVurdering<*>>,
    result: List<VilkårsvurderingDb>
): List<VilkårsvurderingDb> {
    return if (items.isEmpty()) {
        result
    } else {
        return mapRecursive(items, result + items.poll().toDb())
    }
}

internal fun List<VilkårsvurderingDb>.toDomain(): List<VilkarsVurdering<*>> {
    return map { it.toDomain() }
}

internal fun VilkårsvurderingDb.toDomain(): VilkarsVurdering<*> {
    return when (this) {
        is EllerDb -> toDomain()
        is OmsorgsyterHarTilstrekkeligOmsorgsarbeidDb -> toDomain()
        is OgDb -> toDomain()
        is OmsorgsyterOppfyllerAlderskravDb -> toDomain()
        is OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterDb -> toDomain()
        is OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅrDb -> toDomain()
        is OmsorgsmottakerOppfyllerAlderskravForBarnetrygdDb -> toDomain()
        is OmsorgsyterHarMestOmsorgAvAlleOmsorgsytereDb -> toDomain()
        is OmsorgsmottakerOppfyllerAlderskravForHjelpestønadDb -> toDomain()
        is OmsorgsyterErForelderTilMottakerAvHjelpestønadDb -> toDomain()
        is OmsorgsyterMottarBarnetrygdDb -> toDomain()
        is OmsorgsyterHarGyldigOmsorgsarbeidDb -> toDomain()
        is OmsorgsyterErMedlemIFolketrygdenDb -> toDomain()
        is OmsorgsyterErIkkeOmsorgsmottakerDb -> toDomain()
        is OmsorgsyterHarIkkeDødsdatoDb -> toDomain()
        is OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøsDb -> toDomain()
    }
}
