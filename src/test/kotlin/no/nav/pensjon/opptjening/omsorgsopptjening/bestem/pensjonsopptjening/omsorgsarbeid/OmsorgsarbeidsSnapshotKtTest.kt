package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Fnr
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.*
import org.junit.jupiter.api.Test
import java.time.Month
import java.time.YearMonth
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class OmsorgsarbeidsSnapshotKtTest {

    @Test
    fun `Given omsorg OmsorgsArbeid on person 1 When fetching omsorg for person 1 Then return omsorgsArbeid for person 1`() {
        val omsorgsArbeidPerson1 = createOmsorgsarbeid(FNR_1, YearMonth.of(2010, Month.JANUARY), YearMonth.of(2010, Month.AUGUST))

        val snapshot = creatOmsorgsArbeidSnapshot(
            omsorgsArbeidSaker = listOf(
                OmsorgsArbeidSak(
                    omsorgsarbedUtfort = listOf(
                        omsorgsArbeidPerson1
                    )
                )
            )
        )

        val omsorgsArbeid = snapshot.omsorgsArbeid(person1)

        assertEquals(1, omsorgsArbeid.size)
        assertEquals(omsorgsArbeidPerson1, omsorgsArbeid.first())
    }

    @Test
    fun `Given multiple omsorg OmsorgsArbeid on person 1 When fetching omsorg for person 1 Then return omsorgsArbeid for person 1`() {
        val omsorgsArbeidPerson1 = listOf(
            createOmsorgsarbeid(FNR_1, YearMonth.of(2010, Month.JANUARY), YearMonth.of(2010, Month.AUGUST)),
            createOmsorgsarbeid(FNR_1, YearMonth.of(2010, Month.SEPTEMBER), YearMonth.of(2010, Month.DECEMBER)),
        )

        val snapshot = creatOmsorgsArbeidSnapshot(
            omsorgsArbeidSaker = listOf(
                OmsorgsArbeidSak(
                    omsorgsarbedUtfort = omsorgsArbeidPerson1
                )
            )
        )

        val omsorgsArbeid = snapshot.omsorgsArbeid(person1)

        assertEquals(omsorgsArbeidPerson1.size, omsorgsArbeid.size)
        assertTrue(omsorgsArbeidPerson1.containsAll(omsorgsArbeidPerson1))
    }

    @Test
    fun `Given omsorg OmsorgsArbeid on person 2 When fetching omsorg for person 1 Then return zero omsorgsArbeid`() {
        val omsorgsArbeidPerson2 = createOmsorgsarbeid(FNR_2, YearMonth.of(2010, Month.JANUARY), YearMonth.of(2010, Month.AUGUST))

        val snapshot = creatOmsorgsArbeidSnapshot(
            omsorgsArbeidSaker = listOf(
                OmsorgsArbeidSak(
                    omsorgsarbedUtfort = listOf(
                        omsorgsArbeidPerson2
                    )
                )
            )
        )

        val omsorgsArbeid1 = snapshot.omsorgsArbeid(person1)
        val omsorgsArbeid2 = snapshot.omsorgsArbeid(person2)

        assertEquals(0, omsorgsArbeid1.size)
        assertEquals(1, omsorgsArbeid2.size)
    }

    @Test
    fun `Given omsorg OmsorgsArbeid on person 1 and 2 When fetching omsorg for person 1 Then return omsorgsArbeid for person 1`() {
        val omsorgsArbeidPerson1 = createOmsorgsarbeid(FNR_1, YearMonth.of(2010, Month.JANUARY), YearMonth.of(2010, Month.AUGUST))
        val omsorgsArbeidPerson2 = createOmsorgsarbeid(FNR_2, YearMonth.of(2010, Month.AUGUST), YearMonth.of(2010, Month.DECEMBER))

        val snapshot = creatOmsorgsArbeidSnapshot(
            omsorgsArbeidSaker = listOf(
                OmsorgsArbeidSak(
                    omsorgsarbedUtfort = listOf(
                        omsorgsArbeidPerson1,
                        omsorgsArbeidPerson2
                    )
                )
            )
        )

        val omsorgsArbeid = snapshot.omsorgsArbeid(person1)

        assertEquals(1, omsorgsArbeid.size)
        assertEquals(omsorgsArbeidPerson1, omsorgsArbeid.first())
    }

    private fun creatOmsorgsArbeidSnapshot(omsorgsArbeidSaker: List<OmsorgsArbeidSak> = listOf()) =
        OmsorgsarbeidsSnapshot(
            omsorgsAr = 2010,
            kjoreHash = "xxx",
            omsorgsyter = Person(FNR_1),
            omsorgstype = OmsorgsarbeidsType.BARNETRYGD,
            kilde = OmsorgsarbeidsKilde.BARNETRYGD,
            omsorgsArbeidSaker = omsorgsArbeidSaker
        )

    private fun createOmsorgsarbeid(omsorgsyter: String, fom: YearMonth, tom: YearMonth) =
        OmsorgsArbeid(
            fom,
            tom,
            prosent = 100,
            omsorgsyter = Person(omsorgsyter),
            omsorgsmottaker = listOf()
        )

    companion object {
        const val FNR_1 = "12345678901"
        val person1 = no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person(
            alleFnr = mutableSetOf(Fnr(fnr = FNR_1, gjeldende = true))
        )

        const val FNR_2 = "10987654321"
        val person2 = no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person(
            alleFnr = mutableSetOf(Fnr(fnr = FNR_2, gjeldende = true))
        )
    }
}