package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.metrics

import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics.MetricsMåling
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.OmsorgsgrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import org.springframework.stereotype.Component

@Component
class OmsorgsarbeidListenerMetricsMåling(registry: MeterRegistry): MetricsMåling<OmsorgsgrunnlagMelding> {

    private val antallLesteMeldinger = registry.counter("meldinger", "antall", "lest")
    val antallVedtaksperioderFullBarnetrygd = registry.counter("barnetrygd", "antall", "full")
    val antallVedtaksperioderDeltBarnetrygd = registry.counter("barnetrygd", "antall", "delt")
    val antallVedtaksperioderHjelpestonadSats3 = registry.counter("barnetrygd", "antall", "hjelpestonadSats3")
    val antallVedtaksperioderHjelpestonadSats4 = registry.counter("barnetrygd", "antall", "hjelpestonadSats4")

    override fun mål(lambda: () -> OmsorgsgrunnlagMelding): OmsorgsgrunnlagMelding {
        antallLesteMeldinger.increment()
        return tellOmsorgstyper(lambda.invoke())
    }
    private fun tellOmsorgstyper(omsorgsgrunnlagMelding: OmsorgsgrunnlagMelding): OmsorgsgrunnlagMelding {
        omsorgsgrunnlagMelding.saker.forEach { sak ->
            sak.vedtaksperioder.forEach { vedtakPeriode ->
                when (vedtakPeriode.omsorgstype) {
                    Omsorgstype.DELT_BARNETRYGD -> antallVedtaksperioderDeltBarnetrygd.increment()
                    Omsorgstype.FULL_BARNETRYGD -> antallVedtaksperioderFullBarnetrygd.increment()
                    Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_3 -> antallVedtaksperioderHjelpestonadSats3.increment()
                    Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_4 -> antallVedtaksperioderHjelpestonadSats4.increment()
                }
            }
        }
        return omsorgsgrunnlagMelding
    }
}