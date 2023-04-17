package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.*
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Fnr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import org.junit.jupiter.api.Test
import java.time.Month
import java.time.YearMonth
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class OmsorgsarbeidsSnapshotKtTest {

    @Test
    fun `Given omsorg OmsorgsArbeid on person 1 When fetching omsorg for person 1 Then return omsorgsArbeid for person 1`() {
        val omsorgsArbeidPerson1 = createOmsorgsarbeid(person1, YearMonth.of(2010, Month.JANUARY), YearMonth.of(2010, Month.AUGUST))

        val snapshot = creatOmsorgsArbeidSnapshot(
            omsorgsArbeidSaker = listOf(
                OmsorgsarbeidSak(
                    omsorgsarbeidPerioder = listOf(omsorgsArbeidPerson1)
                )
            )
        )

        val omsorgsArbeid = snapshot.getOmsorgsarbeidPerioderForRelevanteAr(person1)

        assertEquals(1, omsorgsArbeid.size)
        assertEquals(omsorgsArbeidPerson1, omsorgsArbeid.first())
    }

    @Test
    fun `Given multiple omsorg OmsorgsArbeid on person 1 When fetching omsorg for person 1 Then return omsorgsArbeid for person 1`() {
        val omsorgsArbeidPerson1 = listOf(
            createOmsorgsarbeid(person1, YearMonth.of(2010, Month.JANUARY), YearMonth.of(2010, Month.AUGUST)),
            createOmsorgsarbeid(person1, YearMonth.of(2010, Month.SEPTEMBER), YearMonth.of(2010, Month.DECEMBER)),
        )

        val snapshot = creatOmsorgsArbeidSnapshot(
            omsorgsArbeidSaker = listOf(
                OmsorgsarbeidSak(
                    omsorgsarbeidPerioder = omsorgsArbeidPerson1
                )
            )
        )

        val omsorgsArbeid = snapshot.getOmsorgsarbeidPerioderForRelevanteAr(person1)

        assertEquals(omsorgsArbeidPerson1.size, omsorgsArbeid.size)
        assertTrue(omsorgsArbeidPerson1.containsAll(omsorgsArbeidPerson1))
    }

    @Test
    fun `Given omsorg OmsorgsArbeid on person 2 When fetching omsorg for person 1 Then return zero omsorgsArbeid`() {
        val omsorgsArbeidPerson2 = createOmsorgsarbeid(person2, YearMonth.of(2010, Month.JANUARY), YearMonth.of(2010, Month.AUGUST))

        val snapshot = creatOmsorgsArbeidSnapshot(
            omsorgsArbeidSaker = listOf(
                OmsorgsarbeidSak(
                    omsorgsarbeidPerioder = listOf(
                        omsorgsArbeidPerson2
                    )
                )
            )
        )

        val omsorgsArbeid1 = snapshot.getOmsorgsarbeidPerioderForRelevanteAr(person1)
        val omsorgsArbeid2 = snapshot.getOmsorgsarbeidPerioderForRelevanteAr(person2)

        assertEquals(0, omsorgsArbeid1.size)
        assertEquals(1, omsorgsArbeid2.size)
    }

    @Test
    fun `Given omsorg OmsorgsArbeid on person 1 and 2 When fetching omsorg for person 1 Then return omsorgsArbeid for person 1`() {
        val omsorgsArbeidPerson1 = createOmsorgsarbeid(person1, YearMonth.of(2010, Month.JANUARY), YearMonth.of(2010, Month.AUGUST))
        val omsorgsArbeidPerson2 = createOmsorgsarbeid(person2, YearMonth.of(2010, Month.AUGUST), YearMonth.of(2010, Month.DECEMBER))

        val snapshot = creatOmsorgsArbeidSnapshot(
            omsorgsArbeidSaker = listOf(
                OmsorgsarbeidSak(
                    omsorgsarbeidPerioder = listOf(
                        omsorgsArbeidPerson1,
                        omsorgsArbeidPerson2
                    )
                )
            )
        )

        val omsorgsArbeid = snapshot.getOmsorgsarbeidPerioderForRelevanteAr(person1)

        assertEquals(1, omsorgsArbeid.size)
        assertEquals(omsorgsArbeidPerson1, omsorgsArbeid.first())
    }

    private fun creatOmsorgsArbeidSnapshot(omsorgsArbeidSaker: List<OmsorgsarbeidSak> = listOf()) =
        OmsorgsarbeidSnapshot(
            omsorgsAr = 2010,
            kjoreHashe = "xxx",
            omsorgsyter = randomPerson,
            omsorgstype = Omsorgstype.BARNETRYGD,
            kilde = Kilde.BARNETRYGD,
            omsorgsarbeidSaker = omsorgsArbeidSaker
        )

    private fun createOmsorgsarbeid(omsorgsyter: Person, fom: YearMonth, tom: YearMonth) =
        OmsorgsarbeidPeriode(
            fom = fom,
            tom = tom,
            prosent = 100,
            omsorgsytere = listOf(omsorgsyter),
            omsorgsmottakere = listOf(),
            landstilknytning = Landstilknytning.NASJONAL
        )

    companion object {
        val person1 = Person(
            alleFnr = mutableSetOf(Fnr(fnr = "12345678901", gjeldende = true)),
            fodselsAr = 1988
        )

        val person2 = Person(
            alleFnr = mutableSetOf(Fnr(fnr = "10987654321", gjeldende = true)),
            fodselsAr = 1988
        )

        val randomPerson = Person(
            alleFnr = mutableSetOf(Fnr(fnr = "22222222222", gjeldende = true)),
            fodselsAr = 1988
        )
    }
}