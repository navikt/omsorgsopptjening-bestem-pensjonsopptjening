package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.OmsorgsArbeidModel
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.OmsorgsMottakerModel
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.OmsorgsyterModel
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.UtbetalingsPeriodeModel
import java.time.LocalDate
import java.time.Month

internal class OmsorgsArbeidTest {

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
        fom: LocalDate = LocalDate.of(2020, Month.JANUARY, 1),
        tom: LocalDate = LocalDate.of(2020, Month.JUNE, 1)
    ) = UtbetalingsPeriodeModel(
        omsorgsmottaker = OmsorgsMottakerModel("12356574353"),
        fom = fom,
        tom = tom,
    )
}