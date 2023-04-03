package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka.producer.OmsorgsOpptejningProducer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.OmsorgsArbeidService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidSnapshot
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.VilkarsvurderingService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Utfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.VilkarsVurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.hentOmsorgForBarnUnder6VilkarsVurderinger
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsarbeidsSnapshot
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class OmsorgsOpptjeningService(
    private val omsorgsArbeidService: OmsorgsArbeidService,
    private val omsorgsOpptejningProducer: OmsorgsOpptejningProducer,
    private val vilkarsvurderingService: VilkarsvurderingService
) {

    fun behandlOmsorgsarbeid(incomingKafkaMessage: OmsorgsarbeidsSnapshot) {
        SECURE_LOG.info("Mappet omsorgsmelding til: $incomingKafkaMessage")

        val omsorgsarbeidsInformasjon = omsorgsArbeidService.createOmsorgasbeidsInformasjon(incomingKafkaMessage)
        val vilkarsVurdering = vilkarsvurderingService.vilkarsvurder(omsorgsarbeidsInformasjon.omsorgsarbeidSnapshot)

        publiserOmsorgsOpptjening(omsorgsarbeidsInformasjon.omsorgsarbeidSnapshot, vilkarsVurdering)
    }

    private fun publiserOmsorgsOpptjening(omsorgsArbeidSnapshot: OmsorgsarbeidSnapshot, vilkarsVurdering: VilkarsVurdering<*>) =
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

    companion object {
        private val SECURE_LOG = LoggerFactory.getLogger("secure")
    }

}