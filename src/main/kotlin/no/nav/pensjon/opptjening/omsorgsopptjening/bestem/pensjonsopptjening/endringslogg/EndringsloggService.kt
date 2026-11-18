package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.endringslogg

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionOperations
import java.util.UUID

@Component
class EndringsloggService(
    private val endringsloggRepo: EndringsloggRepo,
    private val transactionTemplate: TransactionOperations,
) {
    /**
     * Kjører en operasjon for hvert input og håndterer endringslogg i tre trinn:
     * 1) Intensjon lagres først for alle input i en egen transaksjon.
     * 2) Operasjonene kjøres.
     * 3) Intensjonsloggen oppdateres deretter med resultat for alle loggrader samlet i én transaksjon.
     *
     * Designet er bevisst delt for å sikre sporbarhet: intensjon skal være lagret før
     * operasjoner starter, slik at vi alltid kan se hva som var forsøkt selv om senere steg feiler.
     */
    fun <I, T> kjørOperasjonerOgLogg(
        inputListe: List<I>,
        loggKontekst: Endringslogg.Kontekst,
        loggInputMapping: (I) -> Endringslogg.Input,
        loggResultatMapping: (T) -> Endringslogg.Resultat,
        operasjon: (I) -> T,
    ): List<Pair<I, Result<T>>> {
        val inputMedLoggIdListe = loggIntensjon(loggKontekst, inputListe, loggInputMapping)

        val utfallMedLoggIdListe = kjørOperasjoner(inputMedLoggIdListe, operasjon)

        oppdaterIntensjonMedUtfall(utfallMedLoggIdListe, loggResultatMapping)

        return utfallMedLoggIdListe.map { utfall -> utfall.input to utfall.resultat }
    }

    private fun <I> loggIntensjon(
        loggKontekst: Endringslogg.Kontekst,
        inputListe: List<I>,
        loggInputMapping: (I) -> Endringslogg.Input
    ): List<InputMedLoggId<I>> {
        // Bevisst uten try/catch: feiler logging av intensjon, skal hele flyten stoppe før operasjonene starter.
        return transactionTemplate.execute {
            inputListe.map { input ->
                val entry = EndringsloggRepo.IntensjonInsert(
                    endretAv = loggKontekst.navIdent,
                    kilde = loggKontekst.kilde,
                    handling = loggKontekst.handling,
                    status = Endringslogg.Status.INTENT_LOGGET,
                    input = loggInputMapping(input),
                    begrunnelse = loggKontekst.begrunnelse,
                )

                InputMedLoggId(
                    input = input,
                    loggId = endringsloggRepo.lagre(entry),
                )
            }
        }
    }

    // Best effort: hver loggrad oppdateres uavhengig; feil på én rad stopper ikke de øvrige.
    private fun <I, T> oppdaterIntensjonMedUtfall(
        utfallMedLoggIdListe: List<UtfallMedLoggId<I, T>>,
        loggResultatMapping: (T) -> Endringslogg.Resultat
    ) {
        utfallMedLoggIdListe.forEach { utfall ->
            try {
                transactionTemplate.execute {
                    val loggResultat = utfall.resultat.fold(
                        onSuccess = { result -> loggResultatMapping(result) },
                        onFailure = { exception ->
                            Endringslogg.Resultat(
                                status = Endringslogg.Status.FEILET,
                                detalj = exception.formattertExceptionDetalj(),
                            )
                        },
                    )

                    endringsloggRepo.oppdaterResultat(
                        EndringsloggRepo.ResultatUpdate(
                            loggId = utfall.loggId,
                            resultat = loggResultat,
                        )
                    )
                }
            } catch (e: Exception) {
                log.error("Best effort-feil ved oppdatering av endringslogg for loggId={}", utfall.loggId, e)
            }
        }
    }

    private fun <I, T> kjørOperasjoner(
        inputMedLoggIdListe: List<InputMedLoggId<I>>,
        operasjon: (I) -> T,
    ): List<UtfallMedLoggId<I, T>> = inputMedLoggIdListe.map { (input, loggId) ->
        UtfallMedLoggId(
            input = input,
            loggId = loggId,
            resultat = runCatching { operasjon(input) },
        )
    }

    private data class InputMedLoggId<I>(
        val input: I,
        val loggId: UUID,
    )

    private data class UtfallMedLoggId<I, T>(
        val input: I,
        val loggId: UUID,
        val resultat: Result<T>,
    )

    private fun Throwable.formattertExceptionDetalj(): String {
        val exceptionType = this::class.qualifiedName ?: this::class.simpleName ?: "UkjentException"
        val exceptionMelding = message?.takeIf(String::isNotBlank)
        val detalj = listOfNotNull(exceptionType, exceptionMelding).joinToString(separator = ": ")

        if (detalj.length <= EXCEPTION_DETALJ_MAKS_LENGDE) {
            return detalj
        }

        val maksLengdeFørEllipsis = EXCEPTION_DETALJ_MAKS_LENGDE - ELLIPSIS.length
        return detalj.take(maksLengdeFørEllipsis) + ELLIPSIS
    }

    private companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
        const val EXCEPTION_DETALJ_MAKS_LENGDE = 200
        const val ELLIPSIS = "..."
    }
}
