package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka.producer.OmsorgsOpptejningProducer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidSnapshot
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.VilkarsVurderingsService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Vilkarsresultat
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Utfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.hentOmsorgForBarnUnder6VilkarsVurderinger
import org.springframework.stereotype.Service

@Service
class OmsorgsopptjeningsService(
    private val vilkarsVurderingsService: VilkarsVurderingsService,
    private val omsorgsOpptejningProducer: OmsorgsOpptejningProducer,
) {

    fun behandlOmsorgsarbeid(omsorgsarbeidSnapshot: OmsorgsarbeidSnapshot){
        val vilkarsResultater  = vilkarsVurderingsService.vilkarsVurder(omsorgsarbeidSnapshot)
        vilkarsResultater.publiserOmsorgsOpptjening()
    }

    private fun List<Vilkarsresultat>.publiserOmsorgsOpptjening() = forEach { publiserOmsorgsOpptjening(it) }

    private fun publiserOmsorgsOpptjening(vilkarsresultat: Vilkarsresultat) {
        //TODO legg in sammenstilt opplegg i omsorgsopptjening!!
        val omsorgsArbeidSnapshot = vilkarsresultat.snapshot
        val vilkarsVurdering = vilkarsresultat.individueltVilkarsVurdering!!

        omsorgsOpptejningProducer.publiserOmsorgsopptejning(
            OmsorgsOpptjening(
                omsorgsAr = omsorgsArbeidSnapshot.omsorgsAr,
                person = omsorgsArbeidSnapshot.omsorgsyter,
                grunnlag = omsorgsArbeidSnapshot,
                omsorgsopptjeningResultater = vilkarsVurdering,
                utfall = vilkarsVurdering.utfall,
                omsorgsmottakereInvilget = hentOmsorgForBarnUnder6VilkarsVurderinger(vilkarsVurdering)
                    .filter { it.utfall == Utfall.INVILGET }
                    .map { it.grunnlag.omsorgsmottaker }
            )
        )
    }
}