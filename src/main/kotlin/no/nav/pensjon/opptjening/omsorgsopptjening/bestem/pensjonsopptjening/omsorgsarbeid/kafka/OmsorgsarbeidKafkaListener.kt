package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.kafka

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics.MicrometerMetrics
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.repository.OmsorgsarbeidRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.deserialize
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.Topics
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.OmsorgsgrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
@Profile("dev-gcp", "prod-gcp", "kafkaIntegrationTest")
class OmsorgsarbeidKafkaListener(
    private val omsorgsarbeidRepo: OmsorgsarbeidRepo,
    private val metrics: MicrometerMetrics,
) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    @KafkaListener(
        containerFactory = "listener",
        topics = [Topics.Omsorgsopptjening.NAME],
        groupId = "omsorgsopptjening-bestem-pensjonsopptjening"
    )
    fun poll(
        consumerRecord: ConsumerRecord<String, String>,
        acknowledgment: Acknowledgment
    ) {
        metrics.antallLesteMeldinger.increment()
        deserialize<OmsorgsgrunnlagMelding>(consumerRecord.value()).also { omsorgsgrunnlagMelding ->
            Mdc.scopedMdc(omsorgsgrunnlagMelding.correlationId) { _ ->
                Mdc.scopedMdc(omsorgsgrunnlagMelding.innlesingId) { _ ->
                    log.info("Prosesserer melding")
                    omsorgsarbeidRepo.persist(
                        OmsorgsarbeidMelding.Lest(innhold = omsorgsgrunnlagMelding)
                    )
                }
            }
            acknowledgment.acknowledge()
            tellOmsorgstyper(omsorgsgrunnlagMelding)
        }
    }

    fun tellOmsorgstyper(omsorgsgrunnlagMelding: OmsorgsgrunnlagMelding) {
        omsorgsgrunnlagMelding.saker.forEach { sak ->
            sak.vedtaksperioder.forEach { vedtakPeriode ->
                when (vedtakPeriode.omsorgstype) {
                    Omsorgstype.DELT_BARNETRYGD -> metrics.antallVedtaksperioderDeltBarnetrygd.increment()
                    Omsorgstype.FULL_BARNETRYGD -> metrics.antallVedtaksperioderFullBarnetrygd.increment()
                    Omsorgstype.USIKKER_BARNETRYGD -> metrics.antallVedtaksperioderUsikkerBarnetrygd.increment()
                    Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_3 -> metrics.antallVedtaksperioderHjelpestonadSats3.increment()
                    Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_4 -> metrics.antallVedtaksperioderHjelpestonadSats4.increment()
                }
            }
        }
    }
}