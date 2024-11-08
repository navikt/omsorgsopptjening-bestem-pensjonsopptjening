package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.omsorgsmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.tilOmsorgsmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.tilUtbetalingsmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.utbetalingsmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.desember
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.januar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.juli
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.juni
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.november
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.oktober
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.september
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.år
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OmsorgsyterHarGyldigOmsorgsarbeidTest {
    @Test
    fun `avslag hvis ingen måneder med utbetaling`() {
        OmsorgsyterHarGyldigOmsorgsarbeid.vilkarsVurder(
            OmsorgsyterHarGyldigOmsorgsarbeid.Grunnlag(
                omsorgsytersUtbetalingsmåneder = Utbetalingsmåneder(emptySet()),
                omsorgsmåneder = år(2022).omsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår
            )
        ).also {
            assertThat(it.utfall.erAvslag()).isTrue()
        }
    }

    @Test
    fun `innvilget hvis alle måneder med utbetaling og omsorg`() {
        OmsorgsyterHarGyldigOmsorgsarbeid.vilkarsVurder(
            OmsorgsyterHarGyldigOmsorgsarbeid.Grunnlag(
                omsorgsytersUtbetalingsmåneder = år(2022).utbetalingsmåneder(200, Landstilknytning.Norge),
                omsorgsmåneder = år(2022).omsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår
            )
        ).also {
            assertThat(it.utfall.erInnvilget()).isTrue()
        }
    }

    @Test
    fun `avslag hvis omsorgsmåneder som kvalifiserer for automatisk godskriving ikke overlapper tilstrekkelig med utbetalingsmånedene`() {
        OmsorgsyterHarGyldigOmsorgsarbeid.vilkarsVurder(
            OmsorgsyterHarGyldigOmsorgsarbeid.Grunnlag(
                omsorgsytersUtbetalingsmåneder =
                Utbetalingsmåneder(
                    Periode(januar(2022), juni(2022)).tilUtbetalingsmåneder(200, Landstilknytning.Norge) +
                            Periode(juli(2022), desember(2022)).tilUtbetalingsmåneder(0, Landstilknytning.Norge)
                ),
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                    Periode(januar(2022), juni(2022)).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Delt) +
                            Periode(juli(2022), desember(2022)).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)
                ),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår
            )
        ).also {
            assertThat(it.utfall.erAvslag()).isTrue()
        }
    }

    @Test
    fun `innvilget hvis omsorgsmåneder som kvalifiserer for automatisk godskriving overlapper tilstrekkelig med utbetalingsmånedene`() {
        OmsorgsyterHarGyldigOmsorgsarbeid.vilkarsVurder(
            OmsorgsyterHarGyldigOmsorgsarbeid.Grunnlag(
                omsorgsytersUtbetalingsmåneder =
                Utbetalingsmåneder(
                    Periode(januar(2022), juni(2022)).tilUtbetalingsmåneder(200, Landstilknytning.Norge) +
                            Periode(juli(2022), desember(2022)).tilUtbetalingsmåneder(400, Landstilknytning.Norge)
                ),
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                    Periode(januar(2022), juni(2022)).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Delt) +
                            Periode(juli(2022), desember(2022)).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)
                ),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår
            )
        ).also {
            assertThat(it.utfall.erInnvilget()).isTrue()
        }
    }

    @Test
    fun `innvilget hvis omsorgsmåneder som ikke kvalifiserer for automatisk godskriving overlapper tilstrekkelig med utbetalingsmånedene`() {
        OmsorgsyterHarGyldigOmsorgsarbeid.vilkarsVurder(
            OmsorgsyterHarGyldigOmsorgsarbeid.Grunnlag(
                omsorgsytersUtbetalingsmåneder =
                Utbetalingsmåneder(
                    Periode(januar(2022), juni(2022)).tilUtbetalingsmåneder(200, Landstilknytning.Norge) +
                            Periode(juli(2022), desember(2022)).tilUtbetalingsmåneder(400, Landstilknytning.Norge)
                ),
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                    Periode(januar(2022), oktober(2022)).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Delt) +
                            Periode(november(2022), desember(2022)).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)
                ),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår
            )
        ).also {
            assertThat(it.utfall.erInnvilget()).isTrue()
        }
    }

    @Test
    fun `avslag hvis omsorgsmåneder som ikke kvalifiserer for automatisk godskriving ikke overlapper tilstrekkelig med utbetalingsmånedene`() {
        OmsorgsyterHarGyldigOmsorgsarbeid.vilkarsVurder(
            OmsorgsyterHarGyldigOmsorgsarbeid.Grunnlag(
                omsorgsytersUtbetalingsmåneder =
                Utbetalingsmåneder(
                    Periode(januar(2022), juni(2022)).tilUtbetalingsmåneder(
                        0,
                        Landstilknytning.Eøs.UkjentPrimærOgSekundærLand
                    ) +
                            Periode(november(2022), desember(2022)).tilUtbetalingsmåneder(400, Landstilknytning.Norge)
                ),
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                    Periode(januar(2022), oktober(2022)).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Delt) +
                            Periode(november(2022), desember(2022)).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)
                ),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår
            )
        ).also {
            assertThat(it.utfall.erAvslag()).isTrue()
        }
    }

    @Test
    fun `bruker og tar vare på relevante omsorgsmåneder ved vurdering av vilkår - kan godskrives automatisk`() {
        val alle = Omsorgsmåneder.Barnetrygd(
            Periode(januar(2000), september(2000)).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full) +
                    Periode(oktober(2000), Periode.desember(2000)).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Delt)
        )

        val relevant = Omsorgsmåneder.Barnetrygd(
            Periode(januar(2000), september(2000)).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)
        )

        OmsorgsyterHarGyldigOmsorgsarbeid.vilkarsVurder(
            OmsorgsyterHarGyldigOmsorgsarbeid.Grunnlag(
                omsorgsytersUtbetalingsmåneder =
                Utbetalingsmåneder(
                    Periode(januar(2022), juni(2022)).tilUtbetalingsmåneder(
                        0, Landstilknytning.Eøs.UkjentPrimærOgSekundærLand
                    ) + Periode(november(2022), desember(2022)).tilUtbetalingsmåneder(400, Landstilknytning.Norge)
                ),
                omsorgsmåneder = alle,
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår
            )
        ).also {
            assertThat(it.grunnlag.omsorgsmåneder()).isEqualTo(relevant)
        }
    }

    @Test
    fun `bruker og tar vare på relevante omsorgsmåneder ved vurdering av vilkår - kan ikke godskrives automatisk`() {
        val alle = Omsorgsmåneder.Barnetrygd(
            Periode(januar(2000), september(2000)).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Delt) +
                    Periode(oktober(2000), Periode.desember(2000)).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)
        )

        OmsorgsyterHarGyldigOmsorgsarbeid.vilkarsVurder(
            OmsorgsyterHarGyldigOmsorgsarbeid.Grunnlag(
                omsorgsytersUtbetalingsmåneder =
                Utbetalingsmåneder(
                    Periode(januar(2022), juni(2022)).tilUtbetalingsmåneder(
                        0,
                        Landstilknytning.Eøs.UkjentPrimærOgSekundærLand
                    ) +
                            Periode(november(2022), desember(2022)).tilUtbetalingsmåneder(400, Landstilknytning.Norge)
                ),
                omsorgsmåneder = alle,
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår
            )
        ).also {
            assertThat(it.grunnlag.omsorgsmåneder()).isEqualTo(alle)
        }
    }
}
