package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.april
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.februar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.januar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.mars
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.YearMonth

class OmsorgsmånederTest {
    @Test
    fun `full barnetrygd gjelder som en omsorgsmåned`() {
        val full = Omsorgsmåneder.Barnetrygd.of(januar(2022), DomainOmsorgstype.Barnetrygd.Full)
        assertThat(full.antall()).isEqualTo(1)
        assertThat(full.alle()).isEqualTo(setOf(januar(2022)))
    }

    @Test
    fun `kan slå sammen omsorgsmåned`() {
        val all = listOf(
            Omsorgsmåneder.Barnetrygd.of(februar(2022), DomainOmsorgstype.Barnetrygd.Full),
            Omsorgsmåneder.Barnetrygd.of(januar(2022), DomainOmsorgstype.Barnetrygd.Full),
            Omsorgsmåneder.Barnetrygd.of(mars(2022), DomainOmsorgstype.Barnetrygd.Delt),
            Omsorgsmåneder.Barnetrygd.of(april(2022), DomainOmsorgstype.Barnetrygd.Full),
        ).reduce { acc, barnetrygd -> acc.merge(barnetrygd) }

        assertThat(all.antall()).isEqualTo(3)
        assertThat(all.alle()).isEqualTo(setOf(januar(2022), februar(2022), april(2022)))
    }

    @Test
    fun `delt barnetrygd gjelder ikke som en omsorgsmåned`() {
        val delt = Omsorgsmåneder.Barnetrygd.of(januar(2022), DomainOmsorgstype.Barnetrygd.Delt)
        assertThat(delt.antall()).isEqualTo(0)
        assertThat(delt.alle()).isEqualTo(emptySet<YearMonth>())
    }
}