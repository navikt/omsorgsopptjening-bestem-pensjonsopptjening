package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.LocalDate

data class OmsorgsArbeid(val omsorgsyter: Omsorgsyter, val omsorgsAr: String, val hash: String)
data class Omsorgsyter(val fnr: String, val utbetalingsperioder: List<UtbetalingsPeriode>)
data class UtbetalingsPeriode(val fom: LocalDate, val tom: LocalDate, val omsorgsmottaker: OmsorgsMottaker)
data class OmsorgsMottaker(val fnr: String)
data class OmsorgsArbeidKey(val omsorgsyterFnr: String, val omsorgsAr: String)

internal fun convertToOmsorgsArbeid(omsorgsArbeid: String) =
    jacksonObjectMapper().readValue(omsorgsArbeid, OmsorgsArbeid::class.java)
internal fun convertToOmsorgsArbeidKey(omsorgsArbeid: String) =
    jacksonObjectMapper().readValue(omsorgsArbeid, OmsorgsArbeidKey::class.java)