package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.*

internal class OmsorgsarbeidsSnapshotKtTest {


    private fun creatOmsorgsArbeidSnapshot(
        omsorgsAr: Int,
        omsorgsYter: String = "1234566",
        utbetalingsPeriode: List<OmsorgsArbeidSak>
    ) =

        OmsorgsarbeidsSnapshot(
            omsorgsAr = omsorgsAr,
            kjoreHash = "xxx",
            omsorgsYter = Person(omsorgsYter),
            omsorgstype = Omsorgstype.BARNETRYGD,
            kilde = Kilde.BA,
            omsorgsArbeidSaker = utbetalingsPeriode
        )

    private fun createOmsorgsArbeidSaker(
        omsorgsYter: String = "1234566",
        utbetalingsPeriode: List<OmsorgsArbeidsUtbetalinger>
    ) = OmsorgsArbeidSak(
        utbetalingsPeriode.map {
            OmsorgsArbeid(
                omsorgsyter = Person(omsorgsYter),
                omsorgsArbeidsUtbetalinger = it
            )
        }
    )
}