package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.factory.OmsorgsArbeidSakFactory
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.FastsettOmsorgsOpptjening
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.Fnr
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsArbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsMottaker
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.Omsorgsyter
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.UtbetalingsPeriode
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Month
import java.time.YearMonth

internal class OmsorgsOpptjeningTest {

    @Test
    fun `Given omsorgs arbeid for six months When calling personMedInvilgetOmsorgsopptjening Then return person`() {
        val omsorgsArbeidInput = creatOmsorgsArbeid(
            omsorgsAr = "2010",
            omsorgsYter = FNR_1,
            utbetalingsPeriode = listOf(
                creatUtbetalingsPeriode(
                    fom = YearMonth.of(2010, Month.JANUARY),
                    tom = YearMonth.of(2010, Month.JUNE),
                )
            )
        )

        val omsorgsArbeidSak = OmsorgsArbeidSakFactory.createOmsorgsArbeidSak(omsorgsArbeidInput)
        val opptjeningList = FastsettOmsorgsOpptjening.fastsettOmsorgsOpptjening(omsorgsArbeidSak, 2010)

        assertTrue(opptjeningList.first().person identifiseresAv Fnr(FNR_1))
        assertTrue(opptjeningList.first().invilget)
    }

    @Test
    fun `Given omsorgs arbeid for less than six months When calling personMedInvilgetOmsorgsopptjening Then return null`() {
        val omsorgsArbeidInput = creatOmsorgsArbeid(
            omsorgsAr = "2010",
            omsorgsYter = FNR_1,
            utbetalingsPeriode = listOf(
                creatUtbetalingsPeriode(
                    fom = YearMonth.of(2010, Month.JANUARY),
                    tom = YearMonth.of(2010, Month.MAY),
                )
            )
        )

        val omsorgsArbeidSak = OmsorgsArbeidSakFactory.createOmsorgsArbeidSak(omsorgsArbeidInput)
        val opptjeningList = FastsettOmsorgsOpptjening.fastsettOmsorgsOpptjening(omsorgsArbeidSak, 2010)

        assertTrue(opptjeningList.first().person identifiseresAv Fnr(FNR_1))
        assertFalse(opptjeningList.first().invilget)
    }


    private fun creatOmsorgsArbeid(
        omsorgsAr: String,
        omsorgsYter: String = "1234566",
        utbetalingsPeriode: List<UtbetalingsPeriode>
    ) =
        OmsorgsArbeid(
            omsorgsAr = omsorgsAr,
            hash = "12345",
            omsorgsyter = Omsorgsyter(
                fnr = omsorgsYter,
                utbetalingsperioder = utbetalingsPeriode
            )
        )

    private fun creatUtbetalingsPeriode(
        fom: YearMonth = YearMonth.of(2020, Month.JANUARY),
        tom: YearMonth = YearMonth.of(2020, Month.JUNE),
    ) = UtbetalingsPeriode(
        omsorgsmottaker = OmsorgsMottaker("123123"),
        fom = fom,
        tom = tom,
    )

    companion object {
        const val FNR_1: String = "12345678902"
    }
}