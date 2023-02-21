package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka.OmsorgsOpptejningProducer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.PersonService
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsArbeidKey
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsarbeidsSnapshot
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class OmsorgsOpptjeningService(
    private val personService: PersonService,
    private val omsorgsOpptejningProducer: OmsorgsOpptejningProducer
) {

    fun behandlOmsorgsarbeid(key: OmsorgsArbeidKey, omsorgsArbeidSnapshot: OmsorgsarbeidsSnapshot) {
        SECURE_LOG.info("Mappet omsorgsmelding til: key: $key , value: $omsorgsArbeidSnapshot")

        val person = personService.getPerson(omsorgsArbeidSnapshot.omsorgsYter.fnr)
        val omsorgsOpptjening = FastsettOmsorgsOpptjening.fastsettOmsorgsOpptjening(omsorgsArbeidSnapshot, person)

        omsorgsOpptejningProducer.publiserOmsorgsopptejning(omsorgsOpptjening)
    }

    companion object {
        private val SECURE_LOG = LoggerFactory.getLogger("secure")
    }
}