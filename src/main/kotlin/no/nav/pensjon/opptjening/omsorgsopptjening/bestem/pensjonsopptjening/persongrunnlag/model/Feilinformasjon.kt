package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Feilinformasjon
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.IdentRolle

fun List<Feilinformasjon>.oppgavetekster(omsorgsyter: String): Set<String> {
    return map {
        when (it) {
            is Feilinformasjon.OverlappendeBarnetrygdperioder -> {
                "Kunne ikke behandle godskriving av omsorgsopptjening automatisk for $omsorgsyter på grunn av motstridende opplysninger for barnetrygdperiodene tilhørende et av barna. Vurder omsorgsopptjening manuelt."
            }

            is Feilinformasjon.OverlappendeHjelpestønadperioder -> {
                "Kunne ikke behandle godskriving av omsorgsopptjening automatisk for $omsorgsyter på grunn av motstridende opplysninger for hjelpestønadsperiodene tilhørende et av barna. Vurder omsorgsopptjening manuelt."
            }

            is Feilinformasjon.UgyldigIdent -> {
                """Kunne ikke behandle godskriving av omsorgsopptjening automatisk for $omsorgsyter på grunn av at det ikke eksisterer et gjeldende fnr for ${
                    rolle(
                        it
                    )
                } med ident: ${it.ident}""".trimIndent()
            }

            is Feilinformasjon.FeilIDataGrunnlag -> {
                "Kunne ikke behandle godskriving av omsorgsopptjening automatisk for $omsorgsyter på grunn av feil i datagrunnlaget. Vurder omsorgsopptjening manuelt."
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
