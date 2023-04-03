package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka.OmsorgsOpptejningProducer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.OmsorgsArbeidService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidSnapshot
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Utfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.VilkarsVurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.hentOmsorgForBarnUnder6VilkarsVurderinger
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

        val omsorgsArbeidSnapshot = omsorgsArbeidService.createAndSaveOmsorgsarbeidSnapshot(incomingKafkaMessage)
        val vilkarsVurdering = vilkarsvurderingService.vilkarsvurder(omsorgsArbeidSnapshot)

        publiserOmsorgsOpptjening(omsorgsArbeidSnapshot, vilkarsVurdering)
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