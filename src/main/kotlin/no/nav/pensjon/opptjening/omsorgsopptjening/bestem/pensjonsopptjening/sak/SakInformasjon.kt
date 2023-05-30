package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.sak

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SakInformasjon(
    val sakId: String?,
    val sakType: SakType,
    val sakStatus: SakStatus,
    val saksbehandlendeEnhetId: String = "",
    val nyopprettet: Boolean = false,

    @JsonIgnore
    val tilknyttedeSaker: List<SakInformasjon> = emptyList()
)

enum class SakType {
    ALDER,
    UFOREP,
    GJENLEV,
    BARNEP,
    OMSORG,
    GENRL
}

enum class SakStatus {
    TIL_BEHANDLING,
    AVSLUTTET,
    LOPENDE,
    OPPHOR,
    OPPRETTET,
    UKJENT
}