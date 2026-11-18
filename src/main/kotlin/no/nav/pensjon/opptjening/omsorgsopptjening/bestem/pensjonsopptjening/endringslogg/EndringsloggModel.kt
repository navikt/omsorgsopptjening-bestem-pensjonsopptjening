package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.endringslogg

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.util.UUID

interface Endringslogg {

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
    )
    sealed class Input {
        abstract fun inputId(): UUID

        @JsonTypeName("MELDING")
        data class Melding(
            val meldingId: UUID,
        ) : Input() {
            override fun inputId(): UUID = meldingId
        }

        @JsonTypeName("OPPGAVE")
        data class Oppgave(
            val oppgaveId: UUID,
        ) : Input() {
            override fun inputId(): UUID = oppgaveId
        }

        @JsonTypeName("BREV")
        data class Brev(
            val brevId: UUID,
        ) : Input() {
            override fun inputId(): UUID = brevId
        }

        @JsonTypeName("GODSKRIVING")
        data class Godskriving(
            val godskrivId: UUID,
        ) : Input() {
            override fun inputId(): UUID = godskrivId
        }

        @JsonTypeName("KONTROLLBEHANDLING")
        data class Kontrollbehandling(
            val innlesingId: UUID,
            val ar: Int,
        ) : Input() {
            override fun inputId(): UUID = innlesingId
        }
    }

    data class Resultat(
        val status: Status,
        val resultatId: UUID? = null,
        val detalj: String? = null,
    ) {
        init {
            require(status != Status.INTENT_LOGGET) {
                "Resultat må ha en terminal status, fikk $status"
            }
        }
    }

    data class Kontekst(
        val kilde: Kilde,
        val navIdent: String,
        val handling: Handling,
        val begrunnelse: String?
    )

    enum class Kilde(val dbValue: String) {
        ADMIN_WEB_API("admin-web-api");

        companion object {
            fun fromDbValue(dbValue: String): Kilde = entries.single { it.dbValue == dbValue }
        }
    }

    enum class Handling(val dbValue: String) {
        REKJOR_MELDINGER("rekjorMeldinger"),
        STOPP_MELDINGER("stoppMeldinger"),
        AVSLUTT_MELDINGER("avsluttMeldinger"),
        RESTART_OPPGAVER("restartOppgaver"),
        STOPP_OPPGAVE("stoppOppgave"),
        KANSELLER_OPPGAVER("kansellerOppgaver"),
        RESTART_BREV("restartBrev"),
        STOPP_BREV("stoppBrev"),
        RESTART_GODSKRIVING("restartGodskriving"),
        STOPP_GODSKRIVING("stoppGodskriving"),
        HENT_OPPGAVESTATUS("hentOppgavestatus"),
        BESTILL_KONTROLL("bestillKontrollbehandling");

        companion object {
            fun fromDbValue(dbValue: String): Handling = entries.single { it.dbValue == dbValue }
        }
    }

    enum class Status(val dbValue: String) {
        INTENT_LOGGET("INTENT_LOGGET"),
        OK("OK"),
        INGEN_ENDRING("INGEN_ENDRING"),
        STOPPET("STOPPET"),
        AVSLUTTET("AVSLUTTET"),
        RESTARTET("RESTARTET"),
        IKKE_FUNNET("IKKE_FUNNET"),
        IKKE_FUNNET_REMOTE("IKKE_FUNNET_REMOTE"),
        FEILET("FEILET"),
        KANSELLERT("KANSELLERT");

        companion object {
            fun fromDbValue(dbValue: String): Status = entries.single { it.dbValue == dbValue }
        }
    }
}