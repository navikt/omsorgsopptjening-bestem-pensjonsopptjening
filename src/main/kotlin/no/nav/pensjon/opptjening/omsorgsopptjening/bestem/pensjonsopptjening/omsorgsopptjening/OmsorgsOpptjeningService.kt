package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.factory.OmsorgsArbeidSakFactory
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsArbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsArbeidKey
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class OmsorgsOpptjeningService(
    omsorgsOpptejningProducer: OmsorgsOpptejningProducer
) {

    fun behandlOmsorgsarbeid(key: OmsorgsArbeidKey, value: OmsorgsArbeid) {
        SECURE_LOG.info("Mappet omsorgsmelding til: key: $key , value: $value")

        val omsorgsArbeidSak = OmsorgsArbeidSakFactory.createOmsorgsArbeidSak(value)
        val omsorgsOpptjeninger = FastsettOmsorgsOpptjening.fastsettOmsorgsOpptjening(omsorgsArbeidSak, value.omsorgsAr.toInt())

    }

    companion object {
        private val SECURE_LOG = LoggerFactory.getLogger("secure")
    }
}