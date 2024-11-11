package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.april
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.februar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.januar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.mars
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OmsorgsmånederTest {
    @Test
    fun `omsorgsmåneder`() {
        val full = Omsorgsmåneder.Omsorgsmåned(
            januar(2022),
            DomainOmsorgstype.Barnetrygd.Full
        )

        val delt = Omsorgsmåneder.Omsorgsmåned(
            februar(2022),
            DomainOmsorgstype.Barnetrygd.Delt
        )

        val barnetrygd = Omsorgsmåneder.Barnetrygd(
            setOf(full, delt)
        )

        val hjelpestønad = Omsorgsmåneder.BarnetrygdOgHjelpestønad(
            setOf(full, delt)
        )

        assertThat(barnetrygd.full()).isEqualTo(setOf(full))
        assertThat(barnetrygd.kvalifisererForAutomatiskBehandling()).isEqualTo(Omsorgsmåneder.Barnetrygd(setOf(full)))
        assertThat(hjelpestønad.kvalifisererForAutomatiskBehandling()).isEqualTo(Omsorgsmåneder.BarnetrygdOgHjelpestønad(setOf(full)))
        assertThat(barnetrygd.erKvalifisertForAutomatiskBehandling(AntallMånederRegel.FødtIOmsorgsår)).isTrue()
        assertThat(barnetrygd.erKvalifisertForAutomatiskBehandling(AntallMånederRegel.FødtUtenforOmsorgsår)).isFalse()
        assertThat(barnetrygd.delt()).isEqualTo(setOf(delt))
        assertThat(barnetrygd.kvalifisererForManuellBehandling()).isEqualTo(barnetrygd)
        assertThat(hjelpestønad.kvalifisererForManuellBehandling()).isEqualTo(hjelpestønad)
        assertThat(barnetrygd.erKvalifisertForManuellBehandling(AntallMånederRegel.FødtIOmsorgsår)).isTrue()
        assertThat(barnetrygd.erKvalifisertForManuellBehandling(AntallMånederRegel.FødtUtenforOmsorgsår)).isFalse()
        assertThat(barnetrygd.antallFull()).isEqualTo(1)
        assertThat(barnetrygd.antallDelt()).isEqualTo(1)
        assertThat(barnetrygd.alle()).isEqualTo(setOf(januar(2022), februar(2022)))
    }

    @Test
    fun `kan slå sammen omsorgsmåned`() {
        val all = listOf(
            Omsorgsmåneder.Barnetrygd(
                setOf(
                    Omsorgsmåneder.Omsorgsmåned(
                        februar(2022),
                        DomainOmsorgstype.Barnetrygd.Full
                    )
                )
            ),
            Omsorgsmåneder.Barnetrygd(
                setOf(
                    Omsorgsmåneder.Omsorgsmåned(
                        januar(2022),
                        DomainOmsorgstype.Barnetrygd.Full
                    )
                )
            ),
            Omsorgsmåneder.Barnetrygd(
                setOf(
                    Omsorgsmåneder.Omsorgsmåned(
                        mars(2022),
                        DomainOmsorgstype.Barnetrygd.Delt
                    )
                )
            ),
            Omsorgsmåneder.Barnetrygd(
                setOf(
                    Omsorgsmåneder.Omsorgsmåned(
                        april(2022),
                        DomainOmsorgstype.Barnetrygd.Full
                    )
                )
            ),
        ).reduce { acc, barnetrygd -> acc.merge(barnetrygd) }

        assertThat(all.antallFull()).isEqualTo(3)
        assertThat(all.antallDelt()).isEqualTo(1)
        assertThat(all.alle()).isEqualTo(setOf(januar(2022), februar(2022), mars(2022), april(2022)))
    }
}