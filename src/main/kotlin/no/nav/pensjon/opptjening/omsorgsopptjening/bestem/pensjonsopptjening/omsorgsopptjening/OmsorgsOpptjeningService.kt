package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka.OmsorgsOpptejningProducer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.OmsorgsArbeidService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidSnapshot
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsArbeidKey
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsarbeidsSnapshot
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class OmsorgsOpptjeningService(
    private val omsorgsArbeidService: OmsorgsArbeidService,
    private val omsorgsOpptejningProducer: OmsorgsOpptejningProducer
) {

    fun behandlOmsorgsarbeid(key: OmsorgsArbeidKey, omsorgsArbeidSnapshot: OmsorgsarbeidsSnapshot) {
        SECURE_LOG.info("Mappet omsorgsmelding til: key: $key , value: $omsorgsArbeidSnapshot")
        val omsorgsarbeidsSnapshot: OmsorgsarbeidSnapshot = omsorgsArbeidService.getOmsorgsarbeidSnapshot(omsorgsArbeidSnapshot)
        val omsorgsmottakere = omsorgsarbeidsSnapshot.getOmsorgsmottakere(omsorgsarbeidsSnapshot.omsorgsyter)
        val omsorgsOpptjening = FastsettOmsorgsOpptjening.fastsettOmsorgsOpptjening(omsorgsarbeidsSnapshot, omsorgsarbeidsSnapshot.omsorgsyter, omsorgsmottakere)

        omsorgsOpptejningProducer.publiserOmsorgsopptejning(omsorgsOpptjening)
    }

    companion object {
        private val SECURE_LOG = LoggerFactory.getLogger("secure")
    }
}