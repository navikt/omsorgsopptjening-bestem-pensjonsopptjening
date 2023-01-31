package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.OmsorgsArbeidModel
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.OmsorgsMottakerModel
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.OmsorgsyterModel
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.UtbetalingsPeriodeModel
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.factory.OmsorgsopptjeningFactory
import org.junit.jupiter.api.Test
import java.time.Month
import java.time.YearMonth

internal class OmsorgsOpptjeningTest {

    @Test
    fun `Given omsorgs arbeid for six months When calling personMedInvilgetOmsorgsopptjening Then return person`() {
        val omsorgsArbeidInput = creatOmsorgsArbeidModel(
            omsorgsAr = "2010",
            utbetalingsPeriode = listOf(
                creatUtbetalingsPeriodeModel(
                    fom = YearMonth.of(2010, Month.JANUARY),
                    tom = YearMonth.of(2010, Month.JUNE),
                    omsorgsMottaker = "12345678"
                )
            )
        )

        val omsorgsOpptjening = OmsorgsopptjeningFactory.createOmsorgsopptjening(omsorgsArbeidInput)

        omsorgsOpptjening.personMedOmsorgsopptjening()
    }


    private fun creatOmsorgsArbeidModel(omsorgsAr: String, utbetalingsPeriode: List<UtbetalingsPeriodeModel>) =
        OmsorgsArbeidModel(
            omsorgsAr = omsorgsAr,
            hash = "12345",
            omsorgsyter = OmsorgsyterModel(
                fnr = "1234566",
                utbetalingsperioder = utbetalingsPeriode
            )
        )

    private fun creatUtbetalingsPeriodeModel(
        fom: YearMonth = YearMonth.of(2020, Month.JANUARY),
        tom: YearMonth = YearMonth.of(2020, Month.JUNE),
        omsorgsMottaker: String = "12356574353"
    ) = UtbetalingsPeriodeModel(
        omsorgsmottaker = OmsorgsMottakerModel(omsorgsMottaker),
        fom = fom,
        tom = tom,
    )
}