package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka.producer.OmsorgsOpptejningProducer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidSnapshot
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.VilkarsVurderingsService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Vilkarsresultat
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Utfall
import org.springframework.stereotype.Service

@Service
class OmsorgsopptjeningsService(
    private val vilkarsVurderingsService: VilkarsVurderingsService,
    private val omsorgsOpptejningProducer: OmsorgsOpptejningProducer,
) {

    fun behandlOmsorgsarbeid(omsorgsarbeidSnapshot: OmsorgsarbeidSnapshot){
        vilkarsVurderingsService.vilkarsVurder(omsorgsarbeidSnapshot)
            .forEach { publiserOmsorgsOpptjening(it) }
    }

    private fun publiserOmsorgsOpptjening(vilkarsresultat: Vilkarsresultat) {
        omsorgsOpptejningProducer.publiserOmsorgsopptejning(
            OmsorgsOpptjening(
                omsorgsAr = vilkarsresultat.getOmsorgsAr(),
                person = vilkarsresultat.getOmsorgsyter(),
                grunnlag = vilkarsresultat.snapshot,
                vilkarsResultat = vilkarsresultat,
                utfall = vilkarsresultat.getUtfall(),
                omsorgsmottakereInvilget = vilkarsresultat.hentVilkarsVurderingerFullOmsorgForBarnUnder6()
                    .filter { it.utfall == Utfall.INVILGET }
                    .map { it.grunnlag.omsorgsmottaker }
            )
        )
    }
}