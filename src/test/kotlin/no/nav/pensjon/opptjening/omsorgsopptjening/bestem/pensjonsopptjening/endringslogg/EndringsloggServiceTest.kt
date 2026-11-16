package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.endringslogg

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionCallback
import org.springframework.transaction.support.TransactionOperations
import java.util.UUID

class EndringsloggServiceTest {
    private val lagretId = UUID.randomUUID()
    private val endringsloggRepo: EndringsloggRepo = mock {
        on { lagre(any()) } doReturn lagretId
    }
    private val transactionOperations: TransactionOperations = mock {
        on { execute<Any?>(any<TransactionCallback<Any?>>()) } doAnswer { invocation ->
            invocation.getArgument<TransactionCallback<Any?>>(0).doInTransaction(mock<TransactionStatus>())
        }
    }

    private val service = EndringsloggService(
        endringsloggRepo = endringsloggRepo,
        transactionTemplate = transactionOperations,
    )

    @Test
    fun `kan logge og kjoere med generisk input`() {
        val inputId1 = UUID.randomUUID()
        val inputId2 = UUID.randomUUID()
        val input = listOf("melding:$inputId1", "melding:$inputId2")

        val resultater = service.kjørOperasjonerOgLogg(
            loggKontekst = kontekst(),
            inputListe = input,
            loggInputMapping = { Endringslogg.Input.Melding(UUID.fromString(it.substringAfter(":"))) },
            loggResultatMapping = {
                Endringslogg.Resultat(
                    status = Endringslogg.Status.OK,
                    resultatId = UUID.fromString(it.substringAfter(":")),
                )
            },
            operasjon = { it.uppercase() },
        )

        val insertCaptor = argumentCaptor<EndringsloggRepo.IntensjonInsert>()
        verify(endringsloggRepo, times(2)).lagre(insertCaptor.capture())

        assertThat(insertCaptor.firstValue.input.inputId()).isEqualTo(inputId1)
        assertThat(insertCaptor.secondValue.input.inputId()).isEqualTo(inputId2)
        assertThat(resultater).hasSize(2)
        assertThat(resultater.map { it.first }).containsExactlyElementsOf(input)
        assertThat(resultater.map { it.second.getOrThrow() })
            .containsExactly("melding:$inputId1".uppercase(), "melding:$inputId2".uppercase())

        val updateCaptor = argumentCaptor<EndringsloggRepo.ResultatUpdate>()
        verify(endringsloggRepo, times(2)).oppdaterResultat(updateCaptor.capture())
        val førsteResultat = updateCaptor.firstValue.resultat
        assertThat(førsteResultat.status).isEqualTo(Endringslogg.Status.OK)
        assertThat(førsteResultat.resultatId).isEqualTo(inputId1)
    }

    @Test
    fun `lagrer feilet resultat med riktig status`() {
        val inputId = UUID.randomUUID()

        service.kjørOperasjonerOgLogg(
            loggKontekst = kontekst(),
            inputListe = listOf(inputId),
            loggInputMapping = { Endringslogg.Input.Melding(it) },
            loggResultatMapping = { Endringslogg.Resultat(status = Endringslogg.Status.OK, resultatId = it) },
            operasjon = { throw RuntimeException("x".repeat(350)) },
        )

        val captor = argumentCaptor<EndringsloggRepo.ResultatUpdate>()
        verify(endringsloggRepo).oppdaterResultat(captor.capture())

        val resultat = captor.firstValue.resultat
        assertThat(resultat.status).isEqualTo(Endringslogg.Status.FEILET)
        assertThat(resultat.detalj).startsWith("java.lang.RuntimeException: ")
        assertThat(resultat.detalj).hasSize(200)
    }

    @Test
    fun `lagrer ett innslag med riktig type`() {
        val inputId = UUID.randomUUID()

        service.kjørOperasjonerOgLogg(
            loggKontekst = kontekst(begrunnelse = "ok"),
            inputListe = listOf(inputId),
            loggInputMapping = { Endringslogg.Input.Melding(it) },
            loggResultatMapping = { Endringslogg.Resultat(status = Endringslogg.Status.OK, resultatId = it) },
            operasjon = { it },
        )

        val captor = argumentCaptor<EndringsloggRepo.IntensjonInsert>()
        verify(endringsloggRepo).lagre(captor.capture())
        assertThat(captor.firstValue.input).isInstanceOf(Endringslogg.Input.Melding::class.java)
        assertThat(captor.firstValue.input.inputId()).isEqualTo(inputId)
        assertThat(captor.firstValue.status).isEqualTo(Endringslogg.Status.INTENT_LOGGET)
    }

    @Test
    fun `lagrer bare input id ved feilet resultat`() {
        val inputId = UUID.randomUUID()

        service.kjørOperasjonerOgLogg(
            loggKontekst = kontekst(begrunnelse = "feilet"),
            inputListe = listOf(inputId),
            loggInputMapping = { Endringslogg.Input.Melding(it) },
            loggResultatMapping = { Endringslogg.Resultat(status = Endringslogg.Status.OK, resultatId = it) },
            operasjon = { throw RuntimeException("RuntimeException") },
        )

        val insertCaptor = argumentCaptor<EndringsloggRepo.IntensjonInsert>()
        val updateCaptor = argumentCaptor<EndringsloggRepo.ResultatUpdate>()
        verify(endringsloggRepo).lagre(insertCaptor.capture())
        verify(endringsloggRepo).oppdaterResultat(updateCaptor.capture())
        assertThat(insertCaptor.firstValue.input.inputId()).isEqualTo(inputId)
        assertThat(updateCaptor.firstValue.resultat.status).isEqualTo(Endringslogg.Status.FEILET)
    }

    @Test
    fun `propagerer feil ved lagring`() {
        val repoSomFeiler: EndringsloggRepo = mock {
            on { lagre(any()) } doThrow RuntimeException("db-feil")
        }
        val serviceSomFeiler = EndringsloggService(
            endringsloggRepo = repoSomFeiler,
            transactionTemplate = transactionOperations,
        )
        val inputId = UUID.randomUUID()

        assertThatThrownBy {
            serviceSomFeiler.kjørOperasjonerOgLogg(
                loggKontekst = kontekst(begrunnelse = "feiler"),
                inputListe = listOf(inputId),
                loggInputMapping = { Endringslogg.Input.Melding(it) },
                loggResultatMapping = { Endringslogg.Resultat(status = Endringslogg.Status.OK, resultatId = it) },
                operasjon = { it },
            )
        }.isInstanceOf(RuntimeException::class.java)
            .hasMessage("db-feil")
    }

    @Test
    fun `kan bulk logge intent`() {
        val forventedeIder = listOf(UUID.randomUUID(), UUID.randomUUID())
        var kall = 0
        val repo = mock<EndringsloggRepo> {
            on { lagre(any()) } doAnswer {
                forventedeIder[kall++]
            }
        }
        val transaksjonellService = EndringsloggService(
            endringsloggRepo = repo,
            transactionTemplate = transactionOperations,
        )

        val inputId1 = UUID.randomUUID()
        val inputId2 = UUID.randomUUID()
        val faktisk = transaksjonellService.kjørOperasjonerOgLogg(
            loggKontekst = kontekst(),
            inputListe = listOf(inputId1, inputId2),
            loggInputMapping = { Endringslogg.Input.Melding(it) },
            loggResultatMapping = { Endringslogg.Resultat(status = Endringslogg.Status.OK, resultatId = it) },
            operasjon = { it },
        )

        assertThat(faktisk.map { it.first }).containsExactly(inputId1, inputId2)
        verify(repo, times(2)).lagre(any())
    }

    @Test
    fun `kan bulk oppdatere resultat`() {
        val inputId1 = UUID.randomUUID()
        val inputId2 = UUID.randomUUID()

        service.kjørOperasjonerOgLogg(
            loggKontekst = kontekst(),
            inputListe = listOf("ok:$inputId1", "fail:$inputId2"),
            loggInputMapping = { Endringslogg.Input.Melding(UUID.fromString(it.substringAfter(":"))) },
            loggResultatMapping = {
                Endringslogg.Resultat(
                    status = Endringslogg.Status.OK,
                    resultatId = UUID.fromString(it.substringAfter(":")),
                )
            },
            operasjon = {
                if (it.startsWith("fail:")) {
                    throw RuntimeException("RuntimeException")
                }
                it
            },
        )

        val updateCaptor = argumentCaptor<EndringsloggRepo.ResultatUpdate>()
        verify(endringsloggRepo, times(2)).oppdaterResultat(updateCaptor.capture())

        val førsteResultat = updateCaptor.firstValue.resultat
        assertThat(førsteResultat.status).isEqualTo(Endringslogg.Status.OK)
        assertThat(førsteResultat.resultatId).isEqualTo(inputId1)

        val andreResultat = updateCaptor.secondValue.resultat
        assertThat(andreResultat.status).isEqualTo(Endringslogg.Status.FEILET)
        assertThat(andreResultat.detalj).isEqualTo("java.lang.RuntimeException: RuntimeException")
    }

    private fun kontekst(begrunnelse: String? = null): Endringslogg.Kontekst {
        return Endringslogg.Kontekst(
            kilde = Endringslogg.Kilde.ADMIN_WEB_API,
            navIdent = "srv-omsorg",
            handling = Endringslogg.Handling.STOPP_MELDINGER,
            begrunnelse = begrunnelse,
        )
    }
}
