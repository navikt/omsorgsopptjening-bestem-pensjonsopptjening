package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.OmsorgsArbeidSak
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.MåHaEtHalvtÅrMedOmsorg
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.Person

//TODO lag bønne og flere personer osv
class FastsettOmsorgsOpptjening {
    companion object {
        fun fastsettOmsorgsOpptjening(omsorgsArbeidSak: OmsorgsArbeidSak, omsorgsAr: Int): List<OmsorgsOpptjening> {
            return omsorgsArbeidSak
                .involvetePersoner()
                .map { person -> behandlPerson(person, omsorgsAr, omsorgsArbeidSak) }
        }

        private fun behandlPerson(person: Person, ar: Int, sak: OmsorgsArbeidSak): OmsorgsOpptjening {
            val regelResultat = MåHaEtHalvtÅrMedOmsorg()
                .lagVilkarsVurdering(sak.monthsWithOmsorgsarbeid(ar, person))
                .utførVilkarsVurdering()

            return OmsorgsOpptjening(
                omsorgsAr = ar,
                person = person,
                grunnlag = sak.dataObject(),
                omsorgsopptjeningResultater = regelResultat,
                invilget = regelResultat.oppFyllerRegel
            )
        }
    }
}