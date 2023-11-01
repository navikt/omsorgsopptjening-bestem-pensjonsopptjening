package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.metrics

import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics.Metrikker
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.*
import org.springframework.stereotype.Component

@Component
class OmsorgsarbeidProcessingMetrikker(registry: MeterRegistry) : Metrikker<List<FullførtBehandling>?> {

    private val omsorgsarbeidProsessertTidsbruk = registry.timer("prosessering", "tidsbruk", "omsorgsarbeidProsessert")
    private val innvilget = registry.counter("behandling", "antall", "innvilget")
    private val avslag = registry.counter("behandling", "antall", "avslag")
    private val antallUnikeProsesseringer = registry.counter("behandling", "antall", "totalt")
    private val antallBehandlingerPerProsessering = registry.counter("behandling", "antall", "pr_prosessering")
    private val antallAvslåttBehandlingPerProsessering = registry.counter("behandling", "antall", "avslag_pr_prosessering")

    private val omsorgsyterHarMestOmsorgAvAlleOmsorgsytere =
        registry.counter("avslag", "antall", "omsorgsyterHarMestOmsorgAvAlleOmsorgsytere")
    private val omsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr =
        registry.counter("avslag", "antall", "OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr")
    private val omsorgsyterHarTilstrekkeligOmsorgsarbeid =
        registry.counter("avslag", "antall", "OmsorgsyterHarTilstrekkeligOmsorgsarbeid")
    private val omsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr =
        registry.counter("avslag", "antall", "OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr")
    private val omsorgsyterErForelderTilMottakerAvHjelpestønad =
        registry.counter("avslag", "antall", "OmsorgsyterErForelderTilMottakerAvHjelpestønad")
    private val omsorgsmottakerOppfyllerAlderskravForHjelpestønad =
        registry.counter("avslag", "antall", "OmsorgsmottakerOppfyllerAlderskravForHjelpestønad")
    private val omsorgsmottakerOppfyllerAlderskravForBarnetrygd =
        registry.counter("avslag", "antall", "OmsorgsmottakerOppfyllerAlderskravForBarnetrygd")
    private val omsorgsyterOppfyllerAlderskrav = registry.counter("avslag", "antall", "OmsorgsyterOppfyllerAlderskrav")
    private val omsorgsyterErMedlemAvFolketrygden =
        registry.counter("avslag", "antall", "OmsorgsyterErMedlemAvFolketrygden")
    private val omsorgsyterMottarBarnetrygd = registry.counter("avslag", "antall", "OmsorgsyterMottarBarnetrgyd")
    private val omsorgsyterHarGyldigOmsorgsarbeid =
        registry.counter("avslag", "antall", "OmsorgsyterHarGyldigOmsorgsarbeid")


    override fun oppdater(lambda: () -> List<FullførtBehandling>?): List<FullførtBehandling>? {
        return omsorgsarbeidProsessertTidsbruk.recordCallable(lambda)?.also { fullførte ->
            antallUnikeProsesseringer.increment()
            fullførte.tellAntallBehandlinger()
            if (fullførte.any { it.erInnvilget() }) {
                innvilget.increment()
            } else {
                fullførte.tellAvslagsårsaker()
                fullførte.tellAntallAvslag()
                avslag.increment()
            }
        }
    }

    private fun List<FullførtBehandling>.tellAntallBehandlinger(){
        return forEach { _ -> antallBehandlingerPerProsessering.increment() }
    }
    private fun List<FullførtBehandling>.tellAntallAvslag(){
        return filter { !it.erInnvilget() }.forEach { _ -> antallAvslåttBehandlingPerProsessering.increment() }
    }

    private fun List<FullførtBehandling>.tellAvslagsårsaker() {
        flatMap { it.avslagsArsaker() }.associateBy { it.javaClass }.values.map {
            when (it) {
                is EllerVurdering -> {}
                is OgVurdering -> {}
                is OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering -> omsorgsyterHarMestOmsorgAvAlleOmsorgsytere.increment()
                is OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering -> omsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.increment()
                is OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering -> omsorgsyterHarTilstrekkeligOmsorgsarbeid.increment()
                is OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Vurdering -> omsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.increment()
                is OmsorgsyterErForelderTilMottakerAvHjelpestønad.Vurdering -> omsorgsyterErForelderTilMottakerAvHjelpestønad.increment()
                is OmsorgsmottakerOppfyllerAlderskravForHjelpestønad.Vurdering -> omsorgsmottakerOppfyllerAlderskravForHjelpestønad.increment()
                is OmsorgsmottakerOppfyllerAlderskravForBarnetrygd.Vurdering -> omsorgsmottakerOppfyllerAlderskravForBarnetrygd.increment()
                is OmsorgsyterOppfyllerAlderskrav.Vurdering -> omsorgsyterOppfyllerAlderskrav.increment()
                is OmsorgsyterErMedlemAvFolketrygden.Vurdering -> omsorgsyterErMedlemAvFolketrygden.increment()
                is OmsorgsyterMottarBarnetrgyd.Vurdering -> omsorgsyterMottarBarnetrygd.increment()
                is OmsorgsyterHarGyldigOmsorgsarbeid.Vurdering -> omsorgsyterHarGyldigOmsorgsarbeid.increment()
            }
        }
    }
}