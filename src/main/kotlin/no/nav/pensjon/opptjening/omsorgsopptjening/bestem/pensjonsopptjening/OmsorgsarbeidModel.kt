package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.YearMonth

private val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)


data class OmsorgsArbeidModel(val omsorgsyter: OmsorgsyterModel, val omsorgsAr: String, val hash: String)
data class OmsorgsyterModel(val fnr: String, val utbetalingsperioder: List<UtbetalingsPeriodeModel>)
data class UtbetalingsPeriodeModel(val fom: YearMonth, val tom: YearMonth, val omsorgsmottaker: OmsorgsMottakerModel)
data class OmsorgsMottakerModel(val fnr: String)
data class OmsorgsArbeidKeyModel(val omsorgsyterFnr: String, val omsorgsAr: String)

internal fun convertToOmsorgsArbeid(omsorgsArbeid: String) =
    objectMapper.readValue(omsorgsArbeid, OmsorgsArbeidModel::class.java)
internal fun convertToOmsorgsArbeidKey(omsorgsArbeid: String) =
    objectMapper.readValue(omsorgsArbeid, OmsorgsArbeidKeyModel::class.java)