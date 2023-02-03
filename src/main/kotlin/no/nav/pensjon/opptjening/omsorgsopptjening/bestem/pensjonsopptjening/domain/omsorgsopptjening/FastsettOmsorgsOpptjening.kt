package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.omsorgsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.omsorgsarbeid.OmsorgsArbeidSak
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.paragraf.lover.HalvtArMedOmsorgForBarn
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.person.Person

//TODO lag bønne og lere personer osv
class FastsettOmsorgsOpptjening {
    companion object {
        fun fastsettOmsorgsOpptjening(omsorgsArbeidSak: OmsorgsArbeidSak, omsorgsAr: Int): List<OmsorgsOpptjening> {
            return omsorgsArbeidSak
                .involvetePersoner()
                .map { person -> behandlPerson(person, omsorgsAr, omsorgsArbeidSak) }
        }

        private fun behandlPerson(person: Person, ar: Int, sak: OmsorgsArbeidSak): OmsorgsOpptjening {
            val regelResultat = HalvtArMedOmsorgForBarn()
                .medInput(sak.monthsWithOmsorgsarbeid(ar, person))
                .utforRegel()

            return OmsorgsOpptjening.lagOmsorgsopptjening(
                omsorgsAr = ar,
                person = person,
                grunnlag = sak,
                omsorgsopptjeningResultater = regelResultat,
            )
        }
    }
}