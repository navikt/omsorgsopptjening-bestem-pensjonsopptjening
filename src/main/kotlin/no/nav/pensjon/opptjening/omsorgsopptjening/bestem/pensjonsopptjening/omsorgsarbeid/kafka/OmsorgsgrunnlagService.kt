package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.kafka


import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketDatagrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketSak
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketVedtaksperiode
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.toDomain
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.PdlService
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.PersonMedFødselsår
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.OmsorgsgrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.serialize
import org.springframework.stereotype.Service

@Service
class OmsorgsgrunnlagService(
    private val pdlService: PdlService,
) {
    fun berikDatagrunnlag(omsorgsarbeidsSnapshot: OmsorgsgrunnlagMelding): BeriketDatagrunnlag {
        val personer = omsorgsarbeidsSnapshot.hentPersoner().map {
            pdlService.hentPerson(it)
        }.map {
            PersonMedFødselsår(it.gjeldendeFnr, it.fodselsAr)
        }.toSet()

        return omsorgsarbeidsSnapshot.berikDatagrunnlag(personer)
    }

    private fun OmsorgsgrunnlagMelding.berikDatagrunnlag(persondata: Set<PersonMedFødselsår>): BeriketDatagrunnlag {
        fun Set<PersonMedFødselsår>.finnPerson(fnr: String): PersonMedFødselsår {
            return single { it.fnr == fnr }
        }

        return BeriketDatagrunnlag(
            omsorgsyter = persondata.finnPerson(omsorgsyter),
            omsorgstype = omsorgstype.toDomain(),
            kjoreHash = kjoreHash,
            kilde = kilde.toDomain(),
            omsorgsSaker = saker.map { omsorgsSak ->
                BeriketSak(
                    omsorgsyter = persondata.finnPerson(omsorgsSak.omsorgsyter),
                    omsorgVedtakPerioder = omsorgsSak.vedtaksperioder.map { omsorgVedtakPeriode ->
                        BeriketVedtaksperiode(
                            fom = omsorgVedtakPeriode.fom,
                            tom = omsorgVedtakPeriode.tom,
                            prosent = omsorgVedtakPeriode.prosent,
                            omsorgsmottaker = persondata.finnPerson(omsorgVedtakPeriode.omsorgsmottaker)
                        )
                    }

                )
            },
            originaltGrunnlag = serialize(this)
        )
    }
}