package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.FullførtBehandlingOppgaveopplysninger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.FullførteBehandlingerOppgaveopplysninger
import org.jetbrains.annotations.TestOnly
import java.util.UUID

/**
 * [FullførtBehandling] for alle barn omsorgsyter har blitt vurdert omsorgsopptjening for i et gitt omsorgsår.
 */
data class FullførteBehandlinger(
    private val behandlinger: List<FullførtBehandling>
) {
    val aggregertUtfall = AggregertBehandlingsutfall(behandlinger.map { it.utfall }).utfall()

    init {
        require(
            behandlinger.isEmpty() || behandlinger.alleOmsorgsår().count() == 1
        ) { "Forventet bare behandlinger for ett omsorgsår" }
        require(
            behandlinger.isEmpty() || behandlinger.alleOmsorgsytere().count() == 1
        ) { "Forventet bare behandlinger for en omsorgsyter" }
        require(
            behandlinger.isEmpty() || behandlinger.alleMeldingIder().count() == 1
        ) { "Forventer bare fullførte behandlinger med utgangspunkt i samme kafkamelding" }
    }

    fun innvilget(): FullførtBehandling {
        return behandlinger.single { it.erInnvilget() }
    }

    fun alleManuelle(): List<FullførtBehandling> {
        return behandlinger.filter { it.erManuell() }
    }

    fun alleAvslåtte(): List<FullførtBehandling> {
        return behandlinger.filter { it.erAvslag() }
    }

    fun håndterUtfall(
        innvilget: (behandling: FullførtBehandling) -> Unit,
        manuell: (behandlinger: FullførteBehandlinger) -> Unit,
        avslag: () -> Unit,
    ) {
        when (aggregertUtfall) {
            AggregertBehandlingUtfall.Avslag -> {
                avslag() //noop
            }

            AggregertBehandlingUtfall.Innvilget -> {
                innvilget(innvilget())
            }

            AggregertBehandlingUtfall.Manuell -> {
                manuell(this)
            }
        }
    }

    // Denne bør ikke brukes, men er praktisk for tester som kun forventer en behandling
    @TestOnly
    fun single(): FullførtBehandling {
        return behandlinger.single()
    }

    fun alle(): List<FullførtBehandling> {
        return behandlinger
    }

    fun antallBehandlinger(): Int {
        return behandlinger.count()
    }

    fun finnBehandlingsId(): List<UUID> {
        return behandlinger.map { it.id }
    }

    private fun List<FullførtBehandling>.alleOmsorgsår(): Set<Int> {
        return map { it.omsorgsAr }.distinct().toSet()
    }

    private fun List<FullførtBehandling>.alleOmsorgsytere(): Set<String> {
        return map { it.omsorgsyter }.distinct().toSet()
    }

    private fun List<FullførtBehandling>.alleMeldingIder(): Set<UUID> {
        return map { it.meldingId }.distinct().toSet()
    }

    fun statistikk(): FullførteBehandlingerStatistikk {
        return FullførteBehandlingerStatistikk(
            innvilgetOpptjening = if (aggregertUtfall.erInnvilget()) 1 else 0,
            avslåttOpptjening = if (aggregertUtfall.erAvslag()) 1 else 0,
            manuellBehandling = if (aggregertUtfall.erManuell()) 1 else 0,
            //summerer bare avslagsårsaker for tilfeller hvor aggregert utfall er avslag
            summertAvslagPerVilkår = if (aggregertUtfall.erAvslag())
                alleAvslåtte()
                    .flatMap { it.avslåtteVilkår() }
                    .fold(mutableMapOf()) { acc, vilkarsVurdering ->
                        acc.merge(vilkarsVurdering, 1) { gammel, value -> gammel + value }
                        acc
                    } else emptyMap(),
            summertManuellPerVilkår = if (aggregertUtfall.erManuell())
                alleManuelle()
                    .flatMap { it.ubestemteVilkår() }
                    .fold(mutableMapOf()) { acc, vilkarsVurdering ->
                        acc.merge(vilkarsVurdering, 1) { gammel, value -> gammel + value }
                        acc
                    } else emptyMap()
        )
    }

    fun meldingId(): UUID {
        return behandlinger.alleMeldingIder().single()
    }

    fun omsorgsyter(): String {
        return behandlinger.alleOmsorgsytere().single()
    }

    fun omsorgsår(): Int {
        return behandlinger.alleOmsorgsår().single()
    }

    fun hentOppgaveopplysninger(): FullførteBehandlingerOppgaveopplysninger {
        require(aggregertUtfall.erManuell())
        return FullførteBehandlingerOppgaveopplysninger(
            omsorgsyter = omsorgsyter(),
            omsorgsAr = omsorgsår(),
            behandlingOppgaveopplysninger = alleManuelle().map {
                FullførtBehandlingOppgaveopplysninger(
                    behandlingId = it.id,
                    omsorgsmottaker = it.omsorgsmottaker,
                    oppgaveopplysninger = it.hentOppgaveopplysninger()
                )
            },
            meldingId = meldingId()
        )
    }
}

data class FullførteBehandlingerStatistikk(
    val innvilgetOpptjening: Int,
    val avslåttOpptjening: Int,
    val manuellBehandling: Int,
    val summertAvslagPerVilkår: Map<VilkarsVurdering<*>, Int>,
    val summertManuellPerVilkår: Map<VilkarsVurdering<*>, Int>
)