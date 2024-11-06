package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Medlemskapsgrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Ytelsegrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
import java.time.YearMonth

data class Persongrunnlag(
    val omsorgsyter: Person,
    val omsorgsperioder: List<Omsorgsperiode>,
    val hjelpestønadperioder: List<Hjelpestønadperiode>,
    val medlemskapsgrunnlag: Medlemskapsgrunnlag,
    val ytelsegrunnlag: Ytelsegrunnlag,
) {
    fun omsorgsmottakere(): Set<Person> {
        return (omsorgsperioder.map { it.omsorgsmottaker } + hjelpestønadperioder.map { it.omsorgsmottaker }).distinct()
            .toSet()
    }

    fun omsorgsår(): Set<Int> {
        return måneder().map { it.year }.distinct().toSet()
    }

    fun måneder(): Set<YearMonth> {
        return perioder().flatMap { it.alleMåneder() }.distinct().toSet()
    }

    fun perioder(): Set<Periode> {
        return (omsorgsperioder.map { it.periode } + hjelpestønadperioder.map { it.periode }).distinct().toSet()
    }
}