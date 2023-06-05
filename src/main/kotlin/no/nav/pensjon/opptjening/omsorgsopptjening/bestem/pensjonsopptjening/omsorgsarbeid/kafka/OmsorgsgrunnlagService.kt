package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.kafka


import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketOmsorgsgrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.berik
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.PdlService
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.PersonMedFødselsår
import org.springframework.stereotype.Service

@Service
class OmsorgsgrunnlagService(
    private val pdlService: PdlService,
) {
    fun berik(omsorgsarbeidsSnapshot: OmsorgsGrunnlag): BeriketOmsorgsgrunnlag {
        val personer = omsorgsarbeidsSnapshot.hentPersoner().map {
            pdlService.hentPerson(it)
        }.map {
            PersonMedFødselsår(it.gjeldendeFnr, it.fodselsAr)
        }.toSet()

        return omsorgsarbeidsSnapshot.berik(personer)
    }
}