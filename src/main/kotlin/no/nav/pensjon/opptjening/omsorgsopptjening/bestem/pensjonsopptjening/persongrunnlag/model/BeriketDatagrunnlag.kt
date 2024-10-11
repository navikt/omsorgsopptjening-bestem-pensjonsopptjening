package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Landstilknytningmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Omsorgsmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Utbetalingsmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Hjelpestønadperiode.Companion.omsorgsmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Omsorgsperiode.Companion.landstilknytningsmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Omsorgsperiode.Companion.omsorgsmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Omsorgsperiode.Companion.utbetalingsmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import java.time.YearMonth

/**
 * Intern representasjon av data motatt fra en [DomainKilde], beriket med nødvendige data fra tredjeparter (f.eks PDL).
 *
 * @property persongrunnlag alle [Persongrunnlag] hvor personer [omsorgsyter] har omsorg for inngår som en omsorgsmottaker.
 * Inneholder alltid [omsorgsyter]s persongrunnlag, i tillegg til eventuelle persongrunnlag for andre omsorgsytere (f.eks mor/far,
 * verge, institusjon etc). Persongrunnlag som ikke tilhører [omsorgsyter] kan inneholde omsorgsmottakere som ikke er relevant
 * for [omsorgsyter].
 */
data class BeriketDatagrunnlag(
    val omsorgsyter: Person,
    val persongrunnlag: List<Persongrunnlag>,
    val innlesingId: InnlesingId,
    val correlationId: CorrelationId
) {
    val omsorgsytersPersongrunnlag = persongrunnlag.single { it.omsorgsyter == omsorgsyter }
    val omsorgsytersOmsorgsmottakere = omsorgsytersPersongrunnlag.omsorgsmottakere()
    val omsorgsytersOmsorgsår = omsorgsytersPersongrunnlag.omsorgsår()
    val alleMåneder: Set<YearMonth> = persongrunnlag.flatMap { it.måneder() }.distinct().toSet()
    val alleOmsorgsmottakere = persongrunnlag.flatMap { it.omsorgsmottakere() }.distinct()

    fun omsorgsmånederPerOmsorgsyter(omsorgsmottaker: Person): Map<Person, Omsorgsmåneder.Barnetrygd> {
        return persongrunnlag.associate { pg ->
            pg.omsorgsyter to pg.omsorgsperioder.filter { it.omsorgsmottaker == omsorgsmottaker }.omsorgsmåneder()
        }
    }

    fun hjelpestønadMånederPerOmsorgsyter(omsorgsmottaker: Person): Map<Person, Omsorgsmåneder.Hjelpestønad> {
        return persongrunnlag.associate { pg ->
            pg.omsorgsyter to pg.hjelpestønadperioder.filter { it.omsorgsmottaker == omsorgsmottaker }
                .omsorgsmåneder(omsorgsmånederPerOmsorgsyter(omsorgsmottaker)[pg.omsorgsyter]!!)
        }
    }

    fun utbetalingsmånederPerOmsorgsyter(omsorgsmottaker: Person): Map<Person, Utbetalingsmåneder> {
        return persongrunnlag.associate { pg ->
            pg.omsorgsyter to pg.omsorgsperioder.filter { it.omsorgsmottaker == omsorgsmottaker }.utbetalingsmåneder()
        }
    }

    fun landstilknytningMånederPerOmsorgsyter(omsorgsmottaker: Person): Map<Person, Landstilknytningmåneder> {
        return persongrunnlag.associate { pg ->
            pg.omsorgsyter to pg.omsorgsperioder.filter { it.omsorgsmottaker == omsorgsmottaker }
                .landstilknytningsmåneder()
        }
    }
}





