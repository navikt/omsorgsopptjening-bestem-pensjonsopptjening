package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.OmsorgsArbeidService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidSnapshot
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Vilkarsresultat
import org.springframework.stereotype.Service

@Service
class VilkarsVurderingsService(
    private val omsorgsArbeidService: OmsorgsArbeidService,
    private val personVilkarsvurdering: PersonVilkarsvurdering,
    private val individuellVilkarsvurdering: IndividuellVilkarsvurdering,
    private val sammenstiltVilkarsvurdering: SammenstiltVilkarsvurdering
) {

    fun vilkarsVurder(omsorgsarbeidSnapshot: OmsorgsarbeidSnapshot): List<Vilkarsresultat> {
        val relaterteOmsorgsarbeidSnapshot = omsorgsArbeidService.relaterteSnapshot(omsorgsarbeidSnapshot)

        return (relaterteOmsorgsarbeidSnapshot + omsorgsarbeidSnapshot)
            .map { Vilkarsresultat(snapshot = it) }
            .utforPersonVilkarsvurdering()
            .utforIndividuellVilkarsvurdering()
            .utforSammenstiltVilkarsvurdering()
    }

    private fun List<Vilkarsresultat>.utforPersonVilkarsvurdering() = map {
        it.apply {
            personVilkarsvurdering = personVilkarsvurdering.vilkarsvurder(it)
        }
    }

    private fun List<Vilkarsresultat>.utforIndividuellVilkarsvurdering() = map {
        it.apply {
            individueltVilkarsVurdering = individuellVilkarsvurdering.vilkarsvurder(it)
        }
    }

    private fun List<Vilkarsresultat>.utforSammenstiltVilkarsvurdering() = map {
        it.apply {
            sammenstiltVilkarsVurdering = sammenstiltVilkarsvurdering.vilkarsvurder(
                behandledeVilkarsresultat = it,
                involverteVilkarsresultat = filter { involverte -> !involverte.snapshot.omsorgsyter.erSammePerson(it.snapshot.omsorgsyter) }
            )
        }
    }
}