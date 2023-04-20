package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.OmsorgsArbeidService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Vilkarsresultat
import org.springframework.stereotype.Service

@Service
class VilkarsVurderingsService(
    private val omsorgsArbeidService: OmsorgsArbeidService,
    private val personVilkarsvurdering: PersonVilkarsvurdering,
    private val individuellVilkarsvurdering: IndividuellVilkarsvurdering,
    private val sammenstiltVilkarsvurdering: SammenstiltVilkarsvurdering
) {

    fun vilkarsVurder(omsorgsGrunnlag: OmsorgsGrunnlag): List<Vilkarsresultat> {
        val relaterteOmsorgsarbeidSnapshot = omsorgsArbeidService.relaterteSnapshot(omsorgsGrunnlag)

        return (relaterteOmsorgsarbeidSnapshot + omsorgsGrunnlag)
            .map { Vilkarsresultat(snapshot = it) }
            .utforPersonVilkarsvurdering()
            .utforIndividuellVilkarsvurdering()
            .utforSammenstiltVilkarsvurdering()
    }

    private fun List<Vilkarsresultat>.utforPersonVilkarsvurdering() = map {
        it.personVilkarsvurdering = personVilkarsvurdering.vilkarsvurder(it)
        return@map it
    }

    private fun List<Vilkarsresultat>.utforIndividuellVilkarsvurdering() = map {
        it.individueltVilkarsVurdering = individuellVilkarsvurdering.vilkarsvurder(it)
        return@map it
    }

    private fun List<Vilkarsresultat>.utforSammenstiltVilkarsvurdering() = map {
        it.sammenstiltVilkarsVurdering = sammenstiltVilkarsvurdering.vilkarsvurder(
            behandledeVilkarsresultat = it,
            involverteVilkarsresultat = filter { involverte -> !involverte.snapshot.omsorgsyter.erSammePerson(it.snapshot.omsorgsyter) }
        )
        return@map it
    }
}