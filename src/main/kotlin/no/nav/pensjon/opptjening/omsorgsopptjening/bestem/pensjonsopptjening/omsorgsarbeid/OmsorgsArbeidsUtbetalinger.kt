package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.grunnlag.Grunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.grunnlag.GrunnlagsVisitor
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.UtbetalingMoneder.Companion.utbetalingMoneder
import java.time.YearMonth

class OmsorgsArbeidsUtbetalinger(val fom: YearMonth, val tom: YearMonth) : Grunnlag {
    fun utbetalingMoneder() = utbetalingMoneder(fom, tom)

    override fun accept(grunnlagsVisitor: GrunnlagsVisitor) {
        grunnlagsVisitor.visit(this)
    }

    override fun dataObject() = OmsorgsArbeidsUtbetalingerDataObject(fom, tom)
}

data class OmsorgsArbeidsUtbetalingerDataObject(val fom: YearMonth, val tom: YearMonth)