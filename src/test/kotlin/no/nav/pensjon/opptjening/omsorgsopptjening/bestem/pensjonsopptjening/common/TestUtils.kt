package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.LandstilknytningMåned
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Landstilknytningmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Omsorgsmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Utbetalingsmåned
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Utbetalingsmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Ytelsemåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
import java.time.YearMonth

internal fun YearMonth.tilOmsorgsmåned(omsorgstype: DomainOmsorgstype): Omsorgsmåneder.Omsorgsmåned {
    return Omsorgsmåneder.Omsorgsmåned(this, omsorgstype)
}

internal fun Periode.tilOmsorgsmåneder(omsorgstype: DomainOmsorgstype): Set<Omsorgsmåneder.Omsorgsmåned> {
    return alleMåneder().map { Omsorgsmåneder.Omsorgsmåned(it, omsorgstype) }.toSet()
}

internal fun Periode.omsorgsmåneder(omsorgstype: DomainOmsorgstype.Barnetrygd): Omsorgsmåneder.Barnetrygd {
    return Omsorgsmåneder.Barnetrygd(tilOmsorgsmåneder(omsorgstype))
}

internal fun Periode.omsorgsmånederHjelpestønad(omsorgstype: DomainOmsorgstype.Barnetrygd): Omsorgsmåneder.BarnetrygdOgHjelpestønad {
    return Omsorgsmåneder.BarnetrygdOgHjelpestønad(tilOmsorgsmåneder(omsorgstype))
}

internal fun Periode.landstilknytningmåneder(landstilknytning: Landstilknytning): Landstilknytningmåneder {
    return Landstilknytningmåneder(alleMåneder().map { LandstilknytningMåned(it, landstilknytning) }.toSet())
}

internal fun Periode.ytelseMåneder(): Ytelsemåneder {
    return Ytelsemåneder(alleMåneder())
}

internal fun YearMonth.tilUtbetalingsmåned(utbetalt: Int, landstilknytning: Landstilknytning): Utbetalingsmåned? {
    return Utbetalingsmåned.of(this, utbetalt, landstilknytning)
}

internal fun Periode.tilUtbetalingsmåneder(utbetalt: Int, landstilknytning: Landstilknytning): Set<Utbetalingsmåned> {
    return alleMåneder().mapNotNull { it.tilUtbetalingsmåned(utbetalt, landstilknytning) }.toSet()
}

internal fun Periode.utbetalingsmåneder(utbetalt: Int, landstilknytning: Landstilknytning): Utbetalingsmåneder {
    return Utbetalingsmåneder(tilUtbetalingsmåneder(utbetalt, landstilknytning))
}

