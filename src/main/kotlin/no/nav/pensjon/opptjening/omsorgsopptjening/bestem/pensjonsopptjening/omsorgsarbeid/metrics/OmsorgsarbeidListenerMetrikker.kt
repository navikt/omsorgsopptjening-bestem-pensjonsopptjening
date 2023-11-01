package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.metrics

import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics.Metrikker
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding
import org.springframework.stereotype.Component

@Component
class OmsorgsarbeidListenerMetrikker(registry: MeterRegistry) : Metrikker<PersongrunnlagMelding> {

    private val antallLesteMeldinger = registry.counter("meldinger", "antall", "lest")
    val antallVedtaksperioderFullBarnetrygd = registry.counter("barnetrygd", "antall", "full")
    val antallVedtaksperioderDeltBarnetrygd = registry.counter("barnetrygd", "antall", "delt")
    val antallVedtaksperioderUsikkerBarnetrygd = registry.counter("barnetrygd", "antall", "usikker")
    val antallVedtaksperioderHjelpestonadSats3 = registry.counter("barnetrygd", "antall", "hjelpestonadSats3")
    val antallVedtaksperioderHjelpestonadSats4 = registry.counter("barnetrygd", "antall", "hjelpestonadSats4")

    override fun oppdater(lambda: () -> PersongrunnlagMelding): PersongrunnlagMelding {
        return lambda().also {
            antallLesteMeldinger.increment()
            it.tellOmsorgstyper()
        }
    }

    private fun PersongrunnlagMelding.tellOmsorgstyper() {
        persongrunnlag.forEach { persongrunnlag ->
            persongrunnlag.omsorgsperioder.forEach { omsorgsperiode ->
                when (omsorgsperiode.omsorgstype) {
                    Omsorgstype.DELT_BARNETRYGD -> antallVedtaksperioderDeltBarnetrygd.increment()
                    Omsorgstype.FULL_BARNETRYGD -> antallVedtaksperioderFullBarnetrygd.increment()
                    Omsorgstype.USIKKER_BARNETRYGD -> antallVedtaksperioderUsikkerBarnetrygd.increment()
                    Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_3 -> antallVedtaksperioderHjelpestonadSats3.increment()
                    Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_4 -> antallVedtaksperioderHjelpestonadSats4.increment()
                }
            }
        }
    }
}