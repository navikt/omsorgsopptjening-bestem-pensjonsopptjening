package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Feilinformasjon
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.IdentRolle

fun List<Feilinformasjon>.oppgavetekster(): Set<String> {
    return map {
        when (it) {
            is Feilinformasjon.OverlappendeBarnetrygdperioder -> {
                """Teknisk. Kunne ikke behandle automatisk. Motstridende opplysninger for barnetrygdperiodene tilhørende et av barna"""
            }

            is Feilinformasjon.OverlappendeHjelpestønadperioder -> {
                """Teknisk. Kunne ikke behandle automatisk. Motstridende opplysninger for hjelpestønadsperiodene tilhørende et av barna"""
            }

            is Feilinformasjon.UgyldigIdent -> {
                """Teknisk. Kunne ikke behandle automatisk. Det mangler gjeldende fødselsnummer for ${rolle(it)} med ident ${it.ident}"""
            }

            is Feilinformasjon.FeilIDataGrunnlag -> {
                """Teknisk. Kunne ikke behandle automatisk på grunn av feil i datagrunnlaget"""
            }
        }
    }.toSet()
}

private fun rolle(it: Feilinformasjon.UgyldigIdent) =
    when (it.identRolle) {
        IdentRolle.BARNETRYGDMOTTAKER -> "barnetrygdmottaker"
        IdentRolle.OMSORGSYTER_BARNETRYGD -> "barnetrygdmottaker"
        IdentRolle.OMSORGSYTER_HJELPESTONAD -> "barnetrygdmottaker"
        IdentRolle.OMSORGSMOTTAKER_BARNETRYGD -> "barn"
        IdentRolle.OMSORGSMOTTAKER_HJELPESTONAD -> "barn"
    }
