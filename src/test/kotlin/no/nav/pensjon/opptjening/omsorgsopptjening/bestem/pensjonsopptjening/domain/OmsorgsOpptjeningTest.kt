package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.factory.PersonFactory
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.FastsettOmsorgsOpptjening
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.Fnr
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.*
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Month
import java.time.YearMonth

internal class OmsorgsOpptjeningTest {

    @Test
    fun `Given omsorgs arbeid for six months When calling personMedInvilgetOmsorgsopptjening Then return person`() {
        val omsorgsArbeidSnapshot = creatOmsorgsArbeidSnapshot(
            omsorgsAr = 2010,
            omsorgsYter = FNR_1,
            utbetalingsPeriode = listOf(
                creatUtbetalingsPeriode(
                    fom = YearMonth.of(2010, Month.JANUARY),
                    tom = YearMonth.of(2010, Month.JUNE),
                )
            )
        )

        val person = PersonFactory.createPerson(FNR_1, listOf())

        val opptjening = FastsettOmsorgsOpptjening.fastsettOmsorgsOpptjening(omsorgsArbeidSnapshot, person)

        assertTrue(opptjening.person identifiseresAv Fnr(FNR_1))
        assertTrue(opptjening.invilget)
    }

    @Test
    fun `Given omsorgs arbeid for less than six months When calling personMedInvilgetOmsorgsopptjening Then return null`() {
        val omsorgsArbeidSnapshot = creatOmsorgsArbeidSnapshot(
            omsorgsAr = 2010,
            omsorgsYter = FNR_1,
            utbetalingsPeriode = listOf(
                creatUtbetalingsPeriode(
                    fom = YearMonth.of(2010, Month.JANUARY),
                    tom = YearMonth.of(2010, Month.MAY),
                )
            )
        )

        val person = PersonFactory.createPerson(FNR_1, listOf())

        val opptjening = FastsettOmsorgsOpptjening.fastsettOmsorgsOpptjening(omsorgsArbeidSnapshot, person)

        assertTrue(opptjening.person identifiseresAv Fnr(FNR_1))
        assertFalse(opptjening.invilget)
    }


    private fun creatOmsorgsArbeidSnapshot(
        omsorgsAr: Int,
        omsorgsYter: String = "1234566",
        utbetalingsPeriode: List<OmsorgsArbeidsUtbetalinger>
    ) =

        OmsorgsarbeidsSnapshot(
            omsorgsAr = omsorgsAr,
            kjoreHash = "xxx",
            omsorgsYter = Person(omsorgsYter),
            omsorgstype = Omsorgstype.BARNETRYGD,
            kilde = Kilde.BA,
            omsorgsArbeidSaker = listOf(
                OmsorgsArbeidSak(
                    utbetalingsPeriode.map {
                        OmsorgsArbeid(
                            omsorgsyter = Person(omsorgsYter),
                            omsorgsArbeidsUtbetalinger = it
                        )
                    }
                )
            )
        )

    private fun creatUtbetalingsPeriode(fom: YearMonth, tom: YearMonth) = OmsorgsArbeidsUtbetalinger(fom = fom, tom = tom)

    companion object {
        const val FNR_1: String = "12345678902"
    }
}