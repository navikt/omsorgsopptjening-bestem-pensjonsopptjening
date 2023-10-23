package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.kafka

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics.MicrometerMetrics
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.RådataFraKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.MedlemIFolketrygden
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import org.junit.jupiter.api.Test
import java.time.YearMonth
import java.util.UUID
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding as PersongrunnlagMeldingKafka

class PersongrunnlagKafkaListenerTest {
    @Test
    fun `gitt en PersongrunnlagMelding med flere omsorgstyper så tell antall for hver omsorgstype`() {
        val PersongrunnlagMelding = PersongrunnlagMeldingKafka(
            omsorgsyter = "",
            persongrunnlag = listOf(
                PersongrunnlagMeldingKafka.Persongrunnlag(
                    omsorgsyter = "", omsorgsperioder = listOf(
                        PersongrunnlagMeldingKafka.Omsorgsperiode(
                            fom = YearMonth.now(),
                            tom = YearMonth.now(),
                            omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                            omsorgsmottaker = "",
                            kilde = Kilde.BARNETRYGD,
                            medlemskap = MedlemIFolketrygden.Ukjent,
                            utbetalt = 7234,
                            landstilknytning = Landstilknytning.NORGE
                        )
                    )
                ),
                PersongrunnlagMeldingKafka.Persongrunnlag(
                    omsorgsyter = "", omsorgsperioder = listOf(
                        PersongrunnlagMeldingKafka.Omsorgsperiode(
                            fom = YearMonth.now(),
                            tom = YearMonth.now(),
                            omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                            omsorgsmottaker = "",
                            kilde = Kilde.BARNETRYGD,
                            medlemskap = MedlemIFolketrygden.Ukjent,
                            utbetalt = 7234,
                            landstilknytning = Landstilknytning.NORGE
                        )
                    )
                )
            ),
            rådata = RådataFraKilde(value = ""),
            innlesingId = InnlesingId(value = UUID.randomUUID()),
            correlationId = CorrelationId(value = UUID.randomUUID())
        )
        val repo = mockk<PersongrunnlagRepo>()
        val metrics = mockk<MicrometerMetrics>()
        every { metrics.antallVedtaksperioderFullBarnetrygd.increment() } just runs
        every { metrics.antallVedtaksperioderDeltBarnetrygd.increment() } just runs
        every { metrics.antallVedtaksperioderHjelpestonadSats3.increment() } just runs
        every { metrics.antallVedtaksperioderHjelpestonadSats4.increment() } just runs

        val listener = PersongrunnlagKafkaListener(
            persongrunnlagRepo = repo,
            metrics = metrics
        )

        listener.tellOmsorgstyper(PersongrunnlagMelding)

        verify(exactly = 1) { metrics.antallVedtaksperioderFullBarnetrygd.increment() }
        verify(exactly = 1) { metrics.antallVedtaksperioderDeltBarnetrygd.increment() }
        verify(exactly = 0) { metrics.antallVedtaksperioderHjelpestonadSats3.increment() }
        verify(exactly = 0) { metrics.antallVedtaksperioderHjelpestonadSats4.increment() }
    }
}