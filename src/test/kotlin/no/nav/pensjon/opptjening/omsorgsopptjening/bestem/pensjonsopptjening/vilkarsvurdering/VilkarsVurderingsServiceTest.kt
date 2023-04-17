package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.App
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.PostgresqlTestContainer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.*
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.repository.OmsorgsarbeidSnapshotRepository
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.PdlFnr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.PdlPerson
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.repository.PersonRepository
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Utfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Vilkarsresultat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.Month
import java.time.YearMonth
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


@SpringBootTest(classes = [App::class])
@ActiveProfiles("no-kafka")
internal class VilkarsVurderingsServiceTest {

    private val dbContainer = PostgresqlTestContainer.instance

    @Autowired
    private lateinit var personRepository: PersonRepository

    @Autowired
    private lateinit var omsorgsarbeidRepository: OmsorgsarbeidSnapshotRepository

    @Autowired
    private lateinit var vilkarsVurderingsService: VilkarsVurderingsService

    @BeforeEach
    fun clearDb() {
        dbContainer.removeDataFromDB()
    }

    @ParameterizedTest
    @CsvSource(
        "2020-01, 2020-07, SAKSBEHANDLING",
        "2020-01, 2020-06, AVSLAG",
    )
    fun `Given shared omsorg When seven months shared omsorg Then utfall equals SAKSBEHANDLING`(
        fom: YearMonth,
        tom: YearMonth,
        expectedUtfall: Utfall
    ) {
        val omsorgsYter1 = personRepository.updatePerson(omsorgsYter1988)
        val omsorgsYter2 = personRepository.updatePerson(omsorgsYter1987)
        val omsorgsmottaker1 = personRepository.updatePerson(omsorgsmottaker2017)

        val omsorgsPeriode1 = createOmsorgsarbeidPeriode(
            fom = fom,
            tom = tom,
            prosent = PROSENT_50,
            omsorgsytere = listOf(omsorgsYter1, omsorgsYter2),
            omsorgsMottakere = listOf(omsorgsmottaker1)
        )

        val omsorgsPeriode2 = createOmsorgsarbeidPeriode(
            fom = fom,
            tom = tom,
            prosent = PROSENT_50,
            omsorgsytere = listOf(omsorgsYter1, omsorgsYter2),
            omsorgsMottakere = listOf(omsorgsmottaker1)
        )

        val snapshotOmsorgsyter1 = creatOmsorgsArbeidSnapshot(
            omsorgsyter = omsorgsYter1,
            omsorgsArbeidSaker = listOf(OmsorgsarbeidSak(omsorgsarbeidPerioder = listOf(omsorgsPeriode1))),
            omsorgsAr = OMSORGS_AR_2020
        )

        val snapshotOmsorgsyter2 = creatOmsorgsArbeidSnapshot(
            omsorgsyter = omsorgsYter2,
            omsorgsArbeidSaker = listOf(OmsorgsarbeidSak(omsorgsarbeidPerioder = listOf(omsorgsPeriode2))),
            omsorgsAr = OMSORGS_AR_2020
        )


        omsorgsarbeidRepository.save(snapshotOmsorgsyter1)
        omsorgsarbeidRepository.save(snapshotOmsorgsyter2)
        val vilkarsresultater = vilkarsVurderingsService.vilkarsVurder(snapshotOmsorgsyter1)

        assertEquals(2, vilkarsresultater.size)
        assertEquals(expectedUtfall, vilkarsresultater[0].getUtfall())
        assertEquals(expectedUtfall, vilkarsresultater[1].getUtfall())
    }

    @Test
    fun `Given shared omsorg and other omsorgsyter is younger than 17 When seven months shared omsorg Then utfall equals INVILGET`() {
        val omsorgsYter = personRepository.updatePerson(omsorgsYter1988)
        val omsorgsYterYoungerThan17 = personRepository.updatePerson(omsorgsYter2004)
        val omsorgsmottaker1 = personRepository.updatePerson(omsorgsmottaker2017)

        val omsorgsPeriode1 = createOmsorgsarbeidPeriode(
            fom = JANUAR_2020,
            tom = DESEMBER_2020,
            prosent = PROSENT_50,
            omsorgsytere = listOf(omsorgsYter, omsorgsYterYoungerThan17),
            omsorgsMottakere = listOf(omsorgsmottaker1)
        )

        val omsorgsPeriode2 = createOmsorgsarbeidPeriode(
            fom = JANUAR_2020,
            tom = DESEMBER_2020,
            prosent = PROSENT_50,
            omsorgsytere = listOf(omsorgsYter, omsorgsYterYoungerThan17),
            omsorgsMottakere = listOf(omsorgsmottaker1)
        )

        val snapshotOmsorgsyter = creatOmsorgsArbeidSnapshot(
            omsorgsyter = omsorgsYter,
            omsorgsArbeidSaker = listOf(OmsorgsarbeidSak(omsorgsarbeidPerioder = listOf(omsorgsPeriode1))),
            omsorgsAr = OMSORGS_AR_2020
        )

        val snapshotOmsorgsyterYungerThan17 = creatOmsorgsArbeidSnapshot(
            omsorgsyter = omsorgsYterYoungerThan17,
            omsorgsArbeidSaker = listOf(OmsorgsarbeidSak(omsorgsarbeidPerioder = listOf(omsorgsPeriode2))),
            omsorgsAr = OMSORGS_AR_2020
        )


        omsorgsarbeidRepository.save(snapshotOmsorgsyter)
        omsorgsarbeidRepository.save(snapshotOmsorgsyterYungerThan17)

        val vilkarsresultater = vilkarsVurderingsService.vilkarsVurder(snapshotOmsorgsyter)

        val vilkarsresultat = vilkarsresultater.hentResultat(omsorgsYter)
        val vilkarsresultatYungerThan17 = vilkarsresultater.hentResultat(omsorgsYterYoungerThan17)

        assertEquals(2, vilkarsresultater.size)
        assertNotNull(vilkarsresultat)
        assertNotNull(vilkarsresultatYungerThan17)
        assertEquals(Utfall.INVILGET, vilkarsresultat.getUtfall())
        //assertEquals(Utfall.AVSLAG, vilkarsresultatYungerThan17.getUtfall())
    }

    @ParameterizedTest
    @CsvSource(
        "2020-01, 2020-07, MANGLER_ANNEN_OMSORGSYTER",
        "2020-01, 2020-06, AVSLAG",
    )
    fun `Given missing information about other parties When seven months shared omsorg Then utfall equals MANGLER_ANNEN_OMSORGSYTER`(
        fom: YearMonth,
        tom: YearMonth,
        expectedUtfall: Utfall
    ) {
        val omsorgsYter1 = personRepository.updatePerson(omsorgsYter1988)
        val omsorgsYter2 = personRepository.updatePerson(omsorgsYter1987)
        val omsorgsmottaker1 = personRepository.updatePerson(omsorgsmottaker2017)

        val omsorgsPeriode1 = createOmsorgsarbeidPeriode(
            fom = fom,
            tom = tom,
            prosent = PROSENT_50,
            omsorgsytere = listOf(omsorgsYter1, omsorgsYter2),
            omsorgsMottakere = listOf(omsorgsmottaker1)
        )


        val snapshotOmsorgsyter1 = creatOmsorgsArbeidSnapshot(
            omsorgsyter = omsorgsYter1,
            omsorgsArbeidSaker = listOf(OmsorgsarbeidSak(omsorgsarbeidPerioder = listOf(omsorgsPeriode1))),
            omsorgsAr = OMSORGS_AR_2020
        )

        omsorgsarbeidRepository.save(snapshotOmsorgsyter1)
        val vilkarsresultater = vilkarsVurderingsService.vilkarsVurder(snapshotOmsorgsyter1)

        assertEquals(1, vilkarsresultater.size)
        assertEquals(expectedUtfall, vilkarsresultater.first().getUtfall())
    }


    private fun creatOmsorgsArbeidSnapshot(
        omsorgsyter: Person,
        omsorgsAr: Int,
        kjoreHashe: String = KJORE_HASHE,
        omsorgstype: Omsorgstype = TYPE_BARNETRYGD,
        kilde: Kilde = KILDE_BARNETRYGD,
        omsorgsArbeidSaker: List<OmsorgsarbeidSak> = listOf()
    ) =
        OmsorgsarbeidSnapshot(
            omsorgsAr = omsorgsAr,
            kjoreHashe = kjoreHashe,
            omsorgsyter = omsorgsyter,
            omsorgstype = omsorgstype,
            kilde = kilde,
            omsorgsarbeidSaker = omsorgsArbeidSaker
        )

    private fun createOmsorgsarbeidPeriode(
        fom: YearMonth = JANUAR_2020,
        tom: YearMonth = DESEMBER_2020,
        prosent: Int,
        omsorgsytere: List<Person>,
        omsorgsMottakere: List<Person>
    ) =
        OmsorgsarbeidPeriode(
            fom = fom,
            tom = tom,
            prosent = prosent,
            omsorgsytere = omsorgsytere,
            omsorgsmottakere = omsorgsMottakere
        )

    fun List<Vilkarsresultat>.hentResultat(person: Person) =
        filter { it.getOmsorgsyter().erSammePerson(person) }.firstOrNull()


    companion object {
        private val omsorgsYter1988 = PdlPerson(alleFnr = listOf(PdlFnr("11111988", true)), fodselsAr = 1988)
        private val omsorgsYter1987 = PdlPerson(alleFnr = listOf(PdlFnr("22221987", true)), fodselsAr = 1987)
        private val omsorgsYter2004 = PdlPerson(alleFnr = listOf(PdlFnr("33332004", true)), fodselsAr = 2004)
        private val omsorgsmottaker2017 = PdlPerson(alleFnr = listOf(PdlFnr("6666", true)), fodselsAr = 2017)
        private val omsorgsmottaker2018 = PdlPerson(alleFnr = listOf(PdlFnr("7777", true)), fodselsAr = 2018)

        private const val OMSORGS_AR_2020 = 2020
        private const val KJORE_HASHE = "xxxx"
        private val TYPE_BARNETRYGD = Omsorgstype.BARNETRYGD
        private val KILDE_BARNETRYGD = Kilde.BARNETRYGD

        private const val PROSENT_50 = 50
        private const val PROSENT_100 = 100

        private val JANUAR_2020 = YearMonth.of(2020, Month.JANUARY)
        private val DESEMBER_2020 = YearMonth.of(2020, Month.DECEMBER)
    }
}