package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsArbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsMottaker
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.Omsorgsyter
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.UtbetalingsPeriode
import java.time.Month
import java.time.YearMonth

internal class OmsorgsArbeidTest {

    private fun creatOmsorgsArbeidModel(omsorgsAr: String, utbetalingsPeriode: List<UtbetalingsPeriode>) =
        OmsorgsArbeid(
            omsorgsAr = omsorgsAr,
            hash = "12345",
            omsorgsyter = Omsorgsyter(
                fnr = "1234566",
                utbetalingsperioder = utbetalingsPeriode
            )
        )

    private fun creatUtbetalingsPeriode(
        fom: YearMonth = YearMonth.of(2020, Month.JANUARY),
        tom: YearMonth = YearMonth.of(2020, Month.JUNE)
    ) = UtbetalingsPeriode(
        omsorgsmottaker = OmsorgsMottaker("12356574353"),
        fom = fom,
        tom = tom,
    )
}