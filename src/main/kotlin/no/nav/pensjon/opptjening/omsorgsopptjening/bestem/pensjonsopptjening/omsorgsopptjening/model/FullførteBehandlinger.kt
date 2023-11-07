package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import org.jetbrains.annotations.TestOnly
import java.util.UUID

data class FullførteBehandlinger(
    private val behandlinger: List<FullførtBehandling>
) {
    private val behandlingerPerOmsorgsår = behandlinger
        .alleOmsorgsår()
        .associateWith { år -> behandlinger.filter { år == it.omsorgsAr } }
        .map { (år, behandlinger) ->
            AggregertResultatForÅr(
                år = år,
                behandlinger = behandlinger,
                aggregertUtfall = AggregerBehandlingsutfallPerOmsorgsår(behandlinger).utfall()
            )
        }

    init {
        require(behandlingerPerOmsorgsår.flatMap { it.behandlinger }.map { it.utfall }
                    .count { it.erInnvilget() } <= 1) { "Det kan kun eksistere 0..1 innvilget behandling per omsorgsyter per år." }
    }

    data class AggregertResultatForÅr(
        val år: Int,
        val behandlinger: List<FullførtBehandling>,
        val aggregertUtfall: BehandlingUtfall,
    ) {
        fun erInnvilget(): Boolean {
            return aggregertUtfall.erInnvilget()
        }

        fun antall(): Int {
            return behandlinger.count()
        }
    }

    private fun innvilget(år: Int): FullførtBehandling? {
        return finnÅr(år)?.behandlinger?.singleOrNull { it.erInnvilget() }
    }

    private fun manuell(år: Int): List<FullførtBehandling> {
        return finnÅr(år)?.behandlinger?.filter { it.erManuell() } ?: emptyList()
    }

    fun håndterUtfall(
        innvilget: (behandling: FullførtBehandling) -> Unit,
        manuell: (behandling: FullførtBehandling) -> Unit,
        avslag: () -> Unit,
    ) {
        behandlingerPerOmsorgsår.forEach { (år, _, utfall) ->
            when (utfall) {
                BehandlingUtfall.Avslag -> {
                    avslag()
                }

                BehandlingUtfall.Innvilget -> {
                    innvilget(innvilget(år)!!)
                }

                BehandlingUtfall.Manuell -> {
                    manuell(år).forEach { manuell(it) }
                }
            }
        }
    }

    //TODO denne bør ikke brukes, men er praktisk for tester som kun forventer en behandling
    @TestOnly
    fun single(): FullførtBehandling {
        return behandlinger.single()
    }

    fun finnÅr(år: Int): AggregertResultatForÅr? {
        return behandlingerPerOmsorgsår.singleOrNull { it.år == år }
    }

    fun antallBehandlinger(år: Int): Int {
        return finnÅr(år)?.antall() ?: 0
    }

    fun finnBehandlingsId(år: Int): List<UUID> {
        return finnÅr(år)?.behandlinger?.map { it.id } ?: emptyList()
    }

    private fun List<FullførtBehandling>.alleOmsorgsår(): Set<Int> {
        return this.map { it.omsorgsAr }.toSet()
    }

    fun statistikk(): FullførteBehandlingerStatistikk {
        return FullførteBehandlingerStatistikk(
            innvilgetOpptjening = behandlingerPerOmsorgsår.count { it.aggregertUtfall.erInnvilget() },
            avslåttOpptjening = behandlingerPerOmsorgsår.count { it.aggregertUtfall.erAvslag() },
            manuellBehandling = behandlingerPerOmsorgsår.count { it.aggregertUtfall.erManuell() },
            //summerer bare avslagsårsaker for tilfeller hvor aggregert utfall er avslag
            summertAvslagPerVilkår = behandlingerPerOmsorgsår
                .filter { it.aggregertUtfall.erAvslag() }
                .flatMap { it.behandlinger }
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