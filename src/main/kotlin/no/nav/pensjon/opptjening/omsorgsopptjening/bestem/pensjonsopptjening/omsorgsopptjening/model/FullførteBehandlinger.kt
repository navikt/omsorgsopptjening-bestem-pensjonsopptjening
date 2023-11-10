package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import org.jetbrains.annotations.TestOnly
import java.util.UUID

data class FullførteBehandlinger(
    private val behandlinger: List<FullførtBehandling>
) {
    private val aggregertUtfall = AggregertBehandlingsutfall(behandlinger.map { it.utfall }).utfall()

    init {
        require(
            behandlinger.isEmpty() || behandlinger.alleOmsorgsår().count() == 1
        ) { "Forventet bare behandlinger for ett omsorgsår" }
        require(
            behandlinger.isEmpty() || behandlinger.alleOmsorgsytere().count() == 1
        ) { "Forventet bare behandlinger for en omsorgsyter" }
    }

    private fun innvilget(): FullførtBehandling? {
        return behandlinger.singleOrNull { it.erInnvilget() }
    }

    private fun manuell(): List<FullførtBehandling> {
        return behandlinger.filter { it.erManuell() }
    }

    fun håndterUtfall(
        innvilget: (behandling: FullførtBehandling) -> Unit,
        manuell: (behandling: FullførtBehandling) -> Unit,
        avslag: () -> Unit,
    ) {
        behandlinger.forEach {
            when (aggregertUtfall) {
                BehandlingUtfall.Avslag -> {
                    avslag()
                }

                BehandlingUtfall.Innvilget -> {
                    innvilget(innvilget()!!)
                }

                BehandlingUtfall.Manuell -> {
                    manuell().forEach { manuell(it) }
                }
            }
        }
    }

    //TODO denne bør ikke brukes, men er praktisk for tester som kun forventer en behandling
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

    fun statistikk(): FullførteBehandlingerStatistikk {
        return FullførteBehandlingerStatistikk(
            innvilgetOpptjening = behandlinger.count { aggregertUtfall.erInnvilget() },
            avslåttOpptjening = behandlinger.count { aggregertUtfall.erAvslag() },
            manuellBehandling = behandlinger.count { aggregertUtfall.erManuell() },
            //summerer bare avslagsårsaker for tilfeller hvor aggregert utfall er avslag
            summertAvslagPerVilkår = behandlinger
                .filter { aggregertUtfall.erAvslag() }
                .flatMap { it.avslåtteVilkår() }
                .fold(mutableMapOf()) { acc, vilkarsVurdering ->
                    acc.merge(vilkarsVurdering, 1) { gammel, value -> gammel + value }
                    acc
                }
        )
    }

    data class FullførteBehandlingerStatistikk(
        val innvilgetOpptjening: Int,
        val avslåttOpptjening: Int,
        val manuellBehandling: Int,
        val summertAvslagPerVilkår: Map<VilkarsVurdering<*>, Int>
    )
}