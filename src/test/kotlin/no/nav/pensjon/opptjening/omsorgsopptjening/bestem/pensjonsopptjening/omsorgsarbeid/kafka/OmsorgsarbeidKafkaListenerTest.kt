package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.kafka

import io.mockk.*
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics.MicrometerMetrics
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.repository.OmsorgsarbeidRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.RådataFraKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.OmsorgsgrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import org.junit.jupiter.api.Test
import java.time.YearMonth
import java.util.*

class OmsorgsarbeidKafkaListenerTest {
    @Test
    fun `gitt en omsorgsgrunnlagmelding med flere omsorgstyper så tell antall for hver omsorgstype`() {
        val omsorgsgrunnlagMelding = OmsorgsgrunnlagMelding(
            omsorgsyter = "",
            saker = listOf(
                OmsorgsgrunnlagMelding.Sak(
                    omsorgsyter = "", vedtaksperioder = listOf(
                        OmsorgsgrunnlagMelding.VedtakPeriode(
                            fom = YearMonth.now(),
                            tom = YearMonth.now(),
                            omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                            omsorgsmottaker = ""
                        )
                    )
                ),
                OmsorgsgrunnlagMelding.Sak(
                    omsorgsyter = "", vedtaksperioder = listOf(
                        OmsorgsgrunnlagMelding.VedtakPeriode(
                            fom = YearMonth.now(),
                            tom = YearMonth.now(),
                            omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                            omsorgsmottaker = ""
                        )
                    )
                )
            ),
            rådata = RådataFraKilde(value = ""),
            innlesingId = InnlesingId(value = UUID.randomUUID()),
            correlationId = CorrelationId(value = UUID.randomUUID())
        )
        val repo = mockk<OmsorgsarbeidRepo>()
        val metrics = mockk<MicrometerMetrics>()
        every { metrics.antallVedtaksperioderFullBarnetrygd.increment() } just runs
        every { metrics.antallVedtaksperioderDeltBarnetrygd.increment() } just runs
        every { metrics.antallVedtaksperioderHjelpestonadSats3.increment() } just runs
        every { metrics.antallVedtaksperioderHjelpestonadSats4.increment() } just runs

        val listener = OmsorgsarbeidKafkaListener(
            omsorgsarbeidRepo = repo,
            metrics = metrics
        )

        listener.tellOmsorgstyper(omsorgsgrunnlagMelding)

        verify(exactly = 1) { metrics.antallVedtaksperioderFullBarnetrygd.increment()  }
        verify(exactly = 1) { metrics.antallVedtaksperioderDeltBarnetrygd.increment()  }
        verify(exactly = 0) { metrics.antallVedtaksperioderHjelpestonadSats3.increment()  }
        verify(exactly = 0) { metrics.antallVedtaksperioderHjelpestonadSats4.increment()  }
    }
}