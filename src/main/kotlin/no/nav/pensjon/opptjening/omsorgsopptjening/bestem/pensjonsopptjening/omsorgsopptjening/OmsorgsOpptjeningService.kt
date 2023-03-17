package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka.OmsorgsOpptejningProducer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.omsorgsArbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.PersonService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
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

        val person = personService.getPerson(omsorgsArbeidSnapshot.omsorgsyter.fnr)
        val omsorgsmottakere = getOmsorgsmottakere(omsorgsArbeidSnapshot, person)
        val omsorgsOpptjening = FastsettOmsorgsOpptjening.fastsettOmsorgsOpptjening(omsorgsArbeidSnapshot, person, omsorgsmottakere)

        omsorgsOpptejningProducer.publiserOmsorgsopptejning(omsorgsOpptjening)
    }

    private fun getOmsorgsmottakere(omsorgsArbeidSnapshot: OmsorgsarbeidsSnapshot, omsorgsyter: Person): List<Person> {
        val omsorgsmottakere = omsorgsArbeidSnapshot.omsorgsArbeid(omsorgsyter).flatMap { barn -> barn.omsorgsmottaker.map { it.fnr }}
        return omsorgsmottakere.toSet().map { personService.getPerson(it) }
    }

    companion object {
        private val SECURE_LOG = LoggerFactory.getLogger("secure")
    }
}