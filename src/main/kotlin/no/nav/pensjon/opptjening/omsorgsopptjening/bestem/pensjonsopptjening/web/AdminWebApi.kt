package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.web

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.BrevService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.endringslogg.Endringslogg
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.endringslogg.Endringslogg.Handling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.endringslogg.Endringslogg.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.endringslogg.Endringslogg.Status
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.endringslogg.EndringsloggService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjeningService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kontrollbehandling.KontrollbehandlingService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.Oppgave
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveService.OppgaveInfoResult
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveService.OppgaveInfoResult.FantIkkeOppgavenLokalt
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveService.OppgaveInfoResult.FantIkkeOppgavenRemote
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMeldingService
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.security.token.support.core.api.Protected
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.http.MediaType.TEXT_PLAIN_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@Protected
class AdminWebApi(
    private val persongrunnlagMeldingService: PersongrunnlagMeldingService,
    private val oppgaveService: OppgaveService,
    private val brevService: BrevService,
    private val godskrivOpptjeningService: GodskrivOpptjeningService,
    private val kontrollbehandlingService: KontrollbehandlingService,
    private val endringsloggService: EndringsloggService
) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(AdminWebApi::class.java)
        private val secureLog: Logger = LoggerFactory.getLogger("secure")
    }

    @PostMapping("/bestem/rekjor-flere", consumes = [APPLICATION_FORM_URLENCODED_VALUE], produces = [TEXT_PLAIN_VALUE])
    fun rekjørMeldinger(
        @NavIdent navIdent: String,
        @RequestParam("uuidliste") meldingerString: String,
        @RequestParam("begrunnelse") begrunnelse: String? = null
    ): ResponseEntity<String> {

        val meldinger = try {
            parseUUIDListe(meldingerString)
        } catch (_: Throwable) {
            return ResponseEntity.badRequest().body("Kunne ikke parse uuid'ene")
        }

        log.info("rekjør flere: begrunnelse: $begrunnelse")

        try {
            val loggKontekst = loggKontekst(navIdent, Handling.REKJOR_MELDINGER, begrunnelse)

            val inputToResultater = endringsloggService.kjørOperasjonerOgLogg(
                inputListe = meldinger,
                loggKontekst = loggKontekst,
                loggInputMapping = { meldingId -> Endringslogg.Input.Melding(meldingId) },
                loggResultatMapping = { nyId ->
                    Endringslogg.Resultat(
                        status = Status.OK,
                        resultatId = nyId
                    )
                },
            ) { melding ->
                persongrunnlagMeldingService.stoppOgOpprettKopiAvMelding(melding, begrunnelse)
            }

            val responsStrenger = inputToResultater.map { (id, result) ->
                result.fold(
                    onSuccess = { nyId -> "$id OK, erstattet av: $nyId" },
                    onFailure = { ex -> "$id: Feilet, ${ex::class.simpleName}" },
                )
            }

            val respons = responsStrenger.joinToString("\n")
            return ResponseEntity.ok(respons)
        } catch (ex: Throwable) {
            return ResponseEntity.internalServerError().body("Feil ved prosessering: $ex")
        }
    }

    @PostMapping("/bestem/stopp-flere", consumes = [APPLICATION_FORM_URLENCODED_VALUE], produces = [TEXT_PLAIN_VALUE])
    fun stoppMeldinger(
        @NavIdent navIdent: String,
        @RequestParam("uuidliste") meldingerString: String,
        @RequestParam("begrunnelse") begrunnelse: String? = null
    ): ResponseEntity<String> {
        val meldinger = try {
            parseUUIDListe(meldingerString)
        } catch (_: Throwable) {
            return ResponseEntity.badRequest().body("Kunne ikke parse uuid'ene")
        }

        log.info("stopp flere")

        try {
            val loggKontekst = loggKontekst(navIdent, Handling.STOPP_MELDINGER, begrunnelse)

            val inputToResultater = endringsloggService.kjørOperasjonerOgLogg(
                inputListe = meldinger,
                loggKontekst = loggKontekst,
                loggInputMapping = { meldingId -> Endringslogg.Input.Melding(meldingId) },
                loggResultatMapping = { nyId ->
                    Endringslogg.Resultat(
                        status = Status.STOPPET,
                        resultatId = nyId,
                    )
                },
            ) { id ->
                persongrunnlagMeldingService.stoppMelding(id, begrunnelse ?: "")
            }

            val responsStrenger = inputToResultater.map { (id, result) ->
                result.fold(
                    onSuccess = { _ -> "$id Stoppet" },
                    onFailure = { ex -> "$id: Feilet, ${ex::class.simpleName}" },
                )
            }
            val respons = responsStrenger.joinToString("\n")
            return ResponseEntity.ok(respons)
        } catch (ex: Throwable) {
            return ResponseEntity.internalServerError().body("Feil ved prosessering: $ex")
        }
    }

    @PostMapping("/bestem/avslutt-flere", consumes = [APPLICATION_FORM_URLENCODED_VALUE], produces = [TEXT_PLAIN_VALUE])
    fun avsluttMeldinger(
        @NavIdent navIdent: String,
        @RequestParam("uuidliste") meldingerString: String,
        @RequestParam("begrunnelse") begrunnelse: String? = null
    ): ResponseEntity<String> {
        val meldinger = try {
            parseUUIDListe(meldingerString)
        } catch (_: Throwable) {
            return ResponseEntity.badRequest().body("Kunne ikke parse uuid'ene")
        }

        log.info("avslutt flere (antall: ${meldinger.size})")
        try {
            val loggKontekst = loggKontekst(navIdent, Handling.AVSLUTT_MELDINGER, begrunnelse)

            val inputToResultater = endringsloggService.kjørOperasjonerOgLogg(
                inputListe = meldinger,
                loggKontekst = loggKontekst,
                loggInputMapping = { meldingId -> Endringslogg.Input.Melding(meldingId) },
                loggResultatMapping = { nyId ->
                    Endringslogg.Resultat(
                        status = Status.AVSLUTTET,
                        resultatId = nyId,
                    )
                },
            ) { id ->
                persongrunnlagMeldingService.avsluttMelding(id, begrunnelse ?: "")
            }

            val responsStrenger = inputToResultater.map { (id, result) ->
                result.fold(
                    onSuccess = { _ -> "$id: Avsluttet" },
                    onFailure = { ex -> "$id: Feilet, ${ex::class.simpleName}" },
                )
            }
            val respons = responsStrenger.joinToString("\n")
            return ResponseEntity.ok(respons)
        } catch (ex: Throwable) {
            return ResponseEntity.internalServerError().body("Feil ved prosessering: $ex")
        }
    }

    @PostMapping(
        "/bestem/restart-oppgaver",
        consumes = [APPLICATION_FORM_URLENCODED_VALUE],
        produces = [TEXT_PLAIN_VALUE]
    )
    fun restartOppgaver(
        @NavIdent navIdent: String,
        @RequestParam("uuidliste") oppgaverString: String,
        @RequestParam("begrunnelse") begrunnelse: String? = null
    ): ResponseEntity<String> {

        val oppgaver = try {
            parseUUIDListe(oppgaverString)
        } catch (_: Throwable) {
            return ResponseEntity.badRequest().body("Kunne ikke parse uuid'ene")
        }
        try {
            val loggKontekst = loggKontekst(navIdent, Handling.RESTART_OPPGAVER, begrunnelse)

            val inputToResultater = endringsloggService.kjørOperasjonerOgLogg(
                inputListe = oppgaver,
                loggKontekst = loggKontekst,
                loggInputMapping = { oppgaveId -> Endringslogg.Input.Oppgave(oppgaveId) },
                loggResultatMapping = { nyId ->
                    Endringslogg.Resultat(
                        status = if (nyId == null) Status.IKKE_FUNNET else Status.RESTARTET,
                        resultatId = nyId,
                    )
                },
                operasjon = oppgaveService::restart
            )

            val responsStrenger = inputToResultater.map { (id, result) ->
                result.fold(
                    onSuccess = { nyId -> "$id ${nyId?.let { "Restartet" } ?: "Fant ikke oppgaven"}" },
                    onFailure = { ex -> "$id: Feilet, ${ex::class.simpleName}" },
                )
            }
            val respons = responsStrenger.joinToString("\n")
            return ResponseEntity.ok(respons)
        } catch (ex: Throwable) {
            return ResponseEntity.internalServerError().body("Feil ved prosessering: $ex")
        }
    }

    @PostMapping("/bestem/restart-brev", consumes = [APPLICATION_FORM_URLENCODED_VALUE], produces = [TEXT_PLAIN_VALUE])
    fun restartBrev(
        @NavIdent navIdent: String,
        @RequestParam("uuidliste") oppgaverString: String,
        @RequestParam("begrunnelse") begrunnelse: String? = null
    ): ResponseEntity<String> {

        val brev = try {
            parseUUIDListe(oppgaverString)
        } catch (_: Throwable) {
            return ResponseEntity.badRequest().body("Kunne ikke parse uuid'ene")
        }

        try {
            val loggKontekst = loggKontekst(navIdent, Handling.RESTART_BREV, begrunnelse)

            val inputToResultater = endringsloggService.kjørOperasjonerOgLogg(
                inputListe = brev,
                loggKontekst = loggKontekst,
                loggInputMapping = { brevId -> Endringslogg.Input.Brev(brevId) },
                loggResultatMapping = { retId ->
                    Endringslogg.Resultat(
                        status = if (retId == null) Status.IKKE_FUNNET else Status.RESTARTET,
                        resultatId = retId,
                    )
                },
                operasjon = brevService::restart
            )

            val responsStrenger = inputToResultater.map { (id, result) ->
                result.fold(
                    onSuccess = { retId -> if (retId == null) "$id: Fant ikke brevet" else "$id: Restartet" },
                    onFailure = { ex -> "$id: Feilet, ${ex::class.simpleName}" },
                )
            }
            val respons = responsStrenger.joinToString("\n")
            return ResponseEntity.ok(respons)
        } catch (ex: Throwable) {
            return ResponseEntity.internalServerError()
                .contentType(TEXT_PLAIN)
                .body("Feil ved prosessering: $ex")
        }
    }

    @PostMapping("/bestem/stopp-brev", consumes = [APPLICATION_FORM_URLENCODED_VALUE], produces = [TEXT_PLAIN_VALUE])
    fun stoppBrev(
        @NavIdent navIdent: String,
        @RequestParam("uuidliste") oppgaverString: String,
        @RequestParam("begrunnelse") begrunnelse: String? = null
    ): ResponseEntity<String> {

        val brev = try {
            parseUUIDListe(oppgaverString)
        } catch (_: Throwable) {
            return ResponseEntity.badRequest().body("Kunne ikke parse uuid'ene")
        }

        try {
            val loggKontekst = loggKontekst(navIdent, Handling.STOPP_BREV, begrunnelse)

            val inputToResultater = endringsloggService.kjørOperasjonerOgLogg(
                inputListe = brev,
                loggKontekst = loggKontekst,
                loggInputMapping = { brevId -> Endringslogg.Input.Brev(brevId) },
                loggResultatMapping = { retId ->
                    Endringslogg.Resultat(
                        status = if (retId == null) Status.IKKE_FUNNET else Status.STOPPET,
                        resultatId = retId,
                    )
                },
            ) { id ->
                brevService.stopp(id, begrunnelse)
            }

            val responsStrenger = inputToResultater.map { (id, result) ->
                result.fold(
                    onSuccess = { retId -> if (retId == null) "$id: Fant ikke brevet" else "$id: Stoppet" },
                    onFailure = { ex -> "$id: Feilet, ${ex::class.simpleName}" },
                )
            }
            val respons = responsStrenger.joinToString("\n")
            return ResponseEntity.ok(respons)
        } catch (ex: Throwable) {
            return ResponseEntity.internalServerError()
                .contentType(TEXT_PLAIN)
                .body("Feil ved prosessering: $ex")
        }
    }


    @PostMapping(
        "/bestem/stopp-godskriving",
        consumes = [APPLICATION_FORM_URLENCODED_VALUE],
        produces = [TEXT_PLAIN_VALUE]
    )
    fun stoppGodskriving(
        @NavIdent navIdent: String,
        @RequestParam("uuidliste") oppgaverString: String,
        @RequestParam("begrunnelse") begrunnelse: String? = null
    ): ResponseEntity<String> {

        val uuider = try {
            parseUUIDListe(oppgaverString)
        } catch (_: Throwable) {
            return ResponseEntity.badRequest().body("Kunne ikke parse uuid'ene")
        }

        try {
            val loggKontekst = loggKontekst(navIdent, Handling.STOPP_GODSKRIVING, begrunnelse)

            val inputToResultater = endringsloggService.kjørOperasjonerOgLogg(
                inputListe = uuider,
                loggKontekst = loggKontekst,
                loggInputMapping = { godskrivId -> Endringslogg.Input.Godskriving(godskrivId) },
                loggResultatMapping = { retId ->
                    Endringslogg.Resultat(
                        status = if (retId == null) Status.IKKE_FUNNET else Status.STOPPET,
                        resultatId = retId,
                    )
                },
            ) { id ->
                godskrivOpptjeningService.stopp(id, begrunnelse)
            }

            val responsStrenger = inputToResultater.map { (id, result) ->
                result.fold(
                    onSuccess = { retId -> if (retId == null) "$id: Fant ikke godskrivingen" else "$id: Stoppet" },
                    onFailure = { ex -> "$id: Feilet, ${ex::class.simpleName}" },
                )
            }
            val respons = responsStrenger.joinToString("\n")
            return ResponseEntity.ok(respons)
        } catch (ex: Throwable) {
            return ResponseEntity.internalServerError()
                .contentType(TEXT_PLAIN)
                .body("Feil ved prosessering: $ex")
        }
    }

    @PostMapping(
        "/bestem/stopp-oppgave",
        consumes = [APPLICATION_FORM_URLENCODED_VALUE],
        produces = [TEXT_PLAIN_VALUE]
    )
    fun stoppOppgave(
        @NavIdent navIdent: String,
        @RequestParam("uuidliste") oppgaverString: String,
        @RequestParam("begrunnelse") begrunnelse: String? = null
    ): ResponseEntity<String> {

        val uuider = try {
            parseUUIDListe(oppgaverString)
        } catch (_: Throwable) {
            return ResponseEntity.badRequest().body("Kunne ikke parse uuid'ene")
        }

        try {
            val loggKontekst = loggKontekst(navIdent, Handling.STOPP_OPPGAVE, begrunnelse)

            val inputToResultater = endringsloggService.kjørOperasjonerOgLogg(
                inputListe = uuider,
                loggKontekst = loggKontekst,
                loggInputMapping = { oppgaveId -> Endringslogg.Input.Oppgave(oppgaveId) },
                loggResultatMapping = { retId ->
                    Endringslogg.Resultat(
                        status = if (retId == null) Status.IKKE_FUNNET else Status.STOPPET,
                        resultatId = retId,
                    )
                },
            ) { id ->
                oppgaveService.stopp(id, begrunnelse)
            }

            val responsStrenger = inputToResultater.map { (id, result) ->
                result.fold(
                    onSuccess = { retId -> if (retId == null) "$id: Fant ikke oppgave" else "$id: Stoppet" },
                    onFailure = { ex -> "$id: Feilet, ${ex::class.simpleName}" },
                )
            }
            val respons = responsStrenger.joinToString("\n")
            return ResponseEntity.ok(respons)
        } catch (ex: Throwable) {
            return ResponseEntity.internalServerError()
                .contentType(TEXT_PLAIN)
                .body("Feil ved prosessering: $ex")
        }
    }

    @PostMapping(
        "/bestem/restart-godskriving",
        consumes = [APPLICATION_FORM_URLENCODED_VALUE],
        produces = [TEXT_PLAIN_VALUE]
    )
    fun restartGodskriving(
        @NavIdent navIdent: String,
        @RequestParam("uuidliste") oppgaverString: String,
        @RequestParam("begrunnelse") begrunnelse: String? = null
    ): ResponseEntity<String> {

        val uuider = try {
            parseUUIDListe(oppgaverString)
        } catch (_: Throwable) {
            return ResponseEntity.badRequest().body("Kunne ikke parse uuid'ene")
        }

        try {
            val loggKontekst = loggKontekst(navIdent, Handling.RESTART_GODSKRIVING, begrunnelse)

            val inputToResultater = endringsloggService.kjørOperasjonerOgLogg(
                inputListe = uuider,
                loggKontekst = loggKontekst,
                loggInputMapping = { godskrivId -> Endringslogg.Input.Godskriving(godskrivId) },
                loggResultatMapping = { retId ->
                    Endringslogg.Resultat(
                        status = if (retId == null) Status.IKKE_FUNNET else Status.RESTARTET,
                        resultatId = retId,
                    )
                },
                operasjon = godskrivOpptjeningService::restart
            )

            val responsStrenger = inputToResultater.map { (id, result) ->
                result.fold(
                    onSuccess = { retId -> if (retId == null) "$id: Fant ikke godskrivingen" else "$id: Restartet" },
                    onFailure = { ex -> "$id: Feilet, ${ex::class.simpleName}" },
                )
            }
            val respons = responsStrenger.joinToString("\n")
            return ResponseEntity.ok(respons)
        } catch (ex: Throwable) {
            return ResponseEntity.internalServerError()
                .contentType(TEXT_PLAIN)
                .body("Feil ved prosessering: $ex")
        }
    }

    @PostMapping(
        "/bestem/hent-oppgavestatus",
        consumes = [APPLICATION_FORM_URLENCODED_VALUE],
        produces = [TEXT_PLAIN_VALUE]
    )
    fun testkall(
        @NavIdent navIdent: String,
        @RequestParam("uuidliste") oppgaverString: String,
        @RequestParam("begrunnelse") begrunnelse: String? = null
    ): ResponseEntity<String> {

        val uuids = try {
            parseUUIDListe(oppgaverString)
        } catch (_: Throwable) {
            return ResponseEntity.badRequest().body("Kunne ikke parse uuid'ene")
        }

        try {
            val loggKontekst = loggKontekst(navIdent, Handling.HENT_OPPGAVESTATUS, begrunnelse)

            val inputToResultater = endringsloggService.kjørOperasjonerOgLogg(
                inputListe = uuids,
                loggKontekst = loggKontekst,
                loggInputMapping = { oppgaveId -> Endringslogg.Input.Oppgave(oppgaveId) },
                loggResultatMapping = { info ->
                    when (info) {
                        is FantIkkeOppgavenRemote -> Endringslogg.Resultat(status = Status.IKKE_FUNNET_REMOTE)
                        is FantIkkeOppgavenLokalt -> Endringslogg.Resultat(status = Status.IKKE_FUNNET)
                        is OppgaveInfoResult.Info ->
                            Endringslogg.Resultat(
                                status = Status.OK,
                                detalj = "status=${info.info.status}, versjon=${info.info.versjon}"
                            )
                    }
                },
                operasjon = oppgaveService::hentOppgaveInfo
            )

            val responsStrenger = inputToResultater.map { (id, result) ->
                result.fold(
                    onSuccess = { info ->
                        when (info) {
                            is FantIkkeOppgavenRemote -> "$id: Fant ikke oppgaven (remote)"
                            is FantIkkeOppgavenLokalt -> "$id: Fant ikke oppgaven (lokalt)"
                            is OppgaveInfoResult.Info -> "$id: ${info.info.status} (versjon: ${info.info.versjon})"
                        }
                    },
                    onFailure = { ex -> "$id: Feilet, ${ex::class.simpleName}" },
                )
            }
            val respons = responsStrenger.joinToString("\n")
            return ResponseEntity.ok(respons)
        } catch (ex: Throwable) {
            return ResponseEntity.internalServerError()
                .contentType(TEXT_PLAIN)
                .body("Feil ved prosessering: $ex")
        }
    }

    //Statuser ?
    @PostMapping(
        "/bestem/kanseller-oppgaver",
        consumes = [APPLICATION_FORM_URLENCODED_VALUE],
        produces = [TEXT_PLAIN_VALUE]
    )
    fun kansellerOppgaver(
        @NavIdent navIdent: String,
        @RequestParam("uuidliste") oppgaverString: String,
        @RequestParam("begrunnelse") begrunnelse: String? = null
    ): ResponseEntity<String> {
        val uuids = try {
            parseUUIDListe(oppgaverString)
        } catch (_: Throwable) {
            return ResponseEntity.badRequest().body("Kunne ikke parse uuid'ene")
        }

        try {
            val loggKontekst = loggKontekst(navIdent, Handling.KANSELLER_OPPGAVER, begrunnelse)

            val inputToResultater = endringsloggService.kjørOperasjonerOgLogg(
                inputListe = uuids,
                loggKontekst = loggKontekst,
                loggInputMapping = { oppgaveId -> Endringslogg.Input.Oppgave(oppgaveId) },
                loggResultatMapping = { resultat ->
                    when (resultat) {
                        Oppgave.KanselleringsResultat.OPPGAVEN_ER_KANSELLERT ->
                            Endringslogg.Resultat(status = Status.KANSELLERT)

                        Oppgave.KanselleringsResultat.OPPGAVEN_ER_FERDIGBEHANDLET,
                        Oppgave.KanselleringsResultat.KANSELLERING_IKKE_NODVENDIG,
                        Oppgave.KanselleringsResultat.OPPGAVEN_VAR_ALLEREDE_KANSELLERT ->
                            Endringslogg.Resultat(status = Status.INGEN_ENDRING, detalj = "$resultat")

                        Oppgave.KanselleringsResultat.FANT_IKKE_OPPGAVEN_I_OMSORGSOPPTJENING ->
                            Endringslogg.Resultat(status = Status.IKKE_FUNNET, detalj = "$resultat")

                        Oppgave.KanselleringsResultat.FANT_IKKE_OPPGAVEN ->
                            Endringslogg.Resultat(status = Status.IKKE_FUNNET_REMOTE, detalj = "$resultat")

                        Oppgave.KanselleringsResultat.OPPDATERING_FEILET,
                        Oppgave.KanselleringsResultat.OPPGAVENG_ER_ENDRET_I_PARALLELL ->
                            Endringslogg.Resultat(status = Status.FEILET, detalj = "$resultat")
                    }
                },
            ) { id ->
                oppgaveService.kanseller(id, begrunnelse ?: "Ingen begrunnelse oppgitt")
            }

            val responsStrenger = inputToResultater.map { (id, result) ->
                result.fold(
                    onSuccess = { resultat -> resultat.toString() },
                    onFailure = { ex ->
                        secureLog.info("Fikk exception under kansellering av oppgave", ex)
                        "$id: Feilet, ${ex::class.simpleName}"
                    },
                )
            }
            val respons = responsStrenger.joinToString("\n")
            return ResponseEntity.ok(respons)
        } catch (ex: Throwable) {
            return ResponseEntity.internalServerError()
                .contentType(TEXT_PLAIN)
                .body("Feil ved prosessering: $ex")
        }
    }

    @PostMapping(
        "/bestem/kontrollbehandling",
        consumes = [APPLICATION_FORM_URLENCODED_VALUE],
        produces = [TEXT_PLAIN_VALUE]
    )
    fun bestillKontroll(
        @NavIdent navIdent: String,
        @RequestParam("uuidliste") oppgaverString: String,
        @RequestParam("begrunnelse") begrunnelse: String,
        @RequestParam("ar") ar: Int
    ): ResponseEntity<String> {
        val uuid = try {
            parseUUIDListe(oppgaverString).single()
        } catch (_: Throwable) {
            return ResponseEntity.badRequest().body("Kunne ikke parse uuid'ene, godtar kun en enkelt UUID")
        }

        return try {
            val loggKontekst = loggKontekst(navIdent, Handling.BESTILL_KONTROLL, begrunnelse)

            val inputToResultater = endringsloggService.kjørOperasjonerOgLogg(
                inputListe = listOf(uuid),
                loggKontekst = loggKontekst,
                loggInputMapping = { innlesingId ->
                    Endringslogg.Input.Kontrollbehandling(
                        innlesingId = innlesingId,
                        ar = ar
                    )
                },
                loggResultatMapping = { Endringslogg.Resultat(status = Status.OK) },
            ) { innlesingId ->
                kontrollbehandlingService.kontrollbehandling(InnlesingId(innlesingId), begrunnelse, ar)
            }

            val (_, resultat) = inputToResultater.single()

            return resultat.fold(
                onSuccess = { ResponseEntity.ok("Kontrollbehandling opprettet") },
                onFailure = { ex -> kontrollbehandlingFeilResponse(uuid, ex) },
            )
        } catch (ex: Throwable) {
            kontrollbehandlingFeilResponse(uuid, ex)
        }
    }

    private fun kontrollbehandlingFeilResponse(uuid: UUID, ex: Throwable): ResponseEntity<String> {
        secureLog.info("Exception under opprettelse av kontrollbehandling", ex)
        return ResponseEntity.internalServerError()
            .contentType(TEXT_PLAIN)
            .body("Bestilling av kontrollbehandling for innlesing: $uuid feilet med exception: ${ex::class.simpleName}")
    }

    private fun loggKontekst(navIdent: String, handling: Handling, begrunnelse: String?): Endringslogg.Kontekst {
        return Endringslogg.Kontekst(
            kilde = Kilde.ADMIN_WEB_API,
            navIdent = navIdent,
            handling = handling,
            begrunnelse = begrunnelse,
        )
    }

    fun parseUUIDListe(meldingerString: String): List<UUID> {
        return meldingerString.lines()
            .map { it.replace("[^0-9a-f-]".toRegex(), "") }
            .filter { it.isNotEmpty() }
            .map { UUID.fromString(it.trim()) }
    }
}