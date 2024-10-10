package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførtBehandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.BehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.Oppgave
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveDetaljer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgskategori
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

internal class GodskrivOpptjeningServiceImplTest {

    private val godskrivClient: GodskrivOpptjeningClient = mock()
    private val godskrivOpptjeningRepo: GodskrivOpptjeningRepo = mock()
    private val behandlingRepo: BehandlingRepo = mock()
    private val oppgaveService: OppgaveService = mock()

    private val service: GodskrivOpptjeningService = GodskrivOpptjeningServiceImpl(
        godskrivClient = godskrivClient,
        godskrivOpptjeningRepo = godskrivOpptjeningRepo,
        behandlingRepo = behandlingRepo,
        oppgaveService = oppgaveService,
    )

    @Test
    fun `vellykket håndtering kaller godskriving og oppdaterer status`() {
        val behandling: FullførtBehandling = mock {
            on { omsorgsyter }.thenReturn("fnr")
            on { omsorgsAr }.thenReturn(2024)
            on { omsorgstype }.thenReturn(DomainOmsorgskategori.BARNETRYGD)
            on { omsorgsmottaker }.thenReturn("fnrBarn")
        }
        whenever(behandlingRepo.finn(any())).thenReturn(behandling)

        val godskriv = GodskrivOpptjening.Persistent(
            id = UUID.randomUUID(),
            opprettet = Instant.now(),
            meldingId = UUID.randomUUID(),
            correlationId = CorrelationId.generate(),
            omsorgsyter = "fnr",
            innlesingId = InnlesingId.generate(),
            behandlingId = UUID.randomUUID(),
        ).retry("1 retry")

        service.håndter(godskriv)

        verify(godskrivClient).godskriv(
            omsorgsyter = behandling.omsorgsyter,
            omsorgsÅr = behandling.omsorgsAr,
            omsorgstype = behandling.omsorgstype,
            omsorgsmottaker = behandling.omsorgsmottaker,
        )
        verify(godskrivOpptjeningRepo).updateStatus(argThat { status is GodskrivOpptjening.Status.Ferdig })
        verify(behandlingRepo).finn(godskriv.behandlingId)
        verifyNoInteractions(oppgaveService)
    }

    @Test
    fun `gitt at godskriving har blitt forsøkt uten hell får den status retry`() {
        val godskriv = GodskrivOpptjening.Persistent(
            id = UUID.randomUUID(),
            opprettet = Instant.now(),
            meldingId = UUID.randomUUID(),
            correlationId = CorrelationId.generate(),
            omsorgsyter = "fnr",
            innlesingId = InnlesingId.generate(),
            behandlingId = UUID.randomUUID(),
        )

        service.retry(godskriv, RuntimeException("whatever"))

        verifyNoInteractions(godskrivClient)
        verify(godskrivOpptjeningRepo).updateStatus(argThat { status is GodskrivOpptjening.Status.Retry })
        verifyNoInteractions(behandlingRepo)
        verifyNoInteractions(oppgaveService)
    }

    @Test
    fun `gitt at godskriving har blitt forsøkt maks antall ganger uten hell får den status feilet og oppgave opprettes`() {
        val godskriv = GodskrivOpptjening.Persistent(
            id = UUID.randomUUID(),
            opprettet = Instant.now(),
            meldingId = UUID.randomUUID(),
            correlationId = CorrelationId.generate(),
            omsorgsyter = "fnr",
            innlesingId = InnlesingId.generate(),
            behandlingId = UUID.randomUUID(),
        ).retry("1 retry").retry("2 retry").retry("3 retry")

        service.retry(godskriv, RuntimeException("whatever"))

        verifyNoInteractions(godskrivClient)
        verify(godskrivOpptjeningRepo).updateStatus(argThat { status is GodskrivOpptjening.Status.Feilet })
        verifyNoInteractions(behandlingRepo)
        verify(oppgaveService).opprett(argThat {
            equals(
                Oppgave.Transient(
                    detaljer = OppgaveDetaljer.MottakerOgTekst(
                        oppgavemottaker = godskriv.omsorgsyter,
                        oppgavetekst = setOf(Oppgave.kunneIkkeBehandlesAutomatisk())
                    ),
                    behandlingId = godskriv.behandlingId,
                    meldingId = godskriv.meldingId,
                )
            )
        })
    }
}