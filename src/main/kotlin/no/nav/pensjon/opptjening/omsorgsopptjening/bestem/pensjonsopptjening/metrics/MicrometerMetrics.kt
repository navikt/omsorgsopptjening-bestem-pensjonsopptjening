package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class MicrometerMetrics(private val registry: MeterRegistry) {
    // Timer
    val oppgaverProsessertTidsbruk = registry.timer("prosesseringTidsbruk","oppgaverProsessert", "tidsbruk")
    val oppgaverFeiletTidsbruk = registry.timer("prosesseringTidsbruk","oppgaverFeilet", "tidsbruk")
    val omsorgsarbeidProsessertTidsbruk = registry.timer("prosesseringTidsbruk","omsorgsarbeidProsessert", "tidsbruk")
    val omsorgsarbeidFeiletTidsbruk = registry.timer("prosesseringTidsbruk","omsorgsarbeidFeilet", "tidsbruk")
    val godskrivProsessertTidsbruk = registry.timer("prosesseringTidsbruk","godskrivProsessert", "tidsbruk")
    val godskrivFeiletTidsbruk = registry.timer("prosesseringTidsbruk","godskrivFeilet", "tidsbruk")
    val brevProsessertTidsbruk = registry.timer("prosesseringTidsbruk","brevProsessert", "tidsbruk")
    val brevFeiletTidsbruk = registry.timer("prosesseringTidsbruk","brevFeilet", "tidsbruk")

    // Counter
    val innvilget = registry.counter("behandling", "antall", "innvilget" )
    val avslag = registry.counter("behandling", "antall", "avslag" )
    val antallOpprettedeOppgaver = registry.counter("oppgaver", "antall", "opprettet")
    val antallSendteBrev = registry.counter("brev", "antall", "opprettet")
    val antallLesteMeldinger = registry.counter("meldinger", "antall", "lest")

    val antallVedtaksperioderFullBarnetrygd = registry.counter("barnetrygd", "antall", "full")
    val antallVedtaksperioderDeltBarnetrygd = registry.counter("barnetrygd", "antall", "delt")
    val antallVedtaksperioderHjelpestonadSats3 = registry.counter("barnetrygd", "antall", "hjelpestonadSats3")
    val antallVedtaksperioderHjelpestonadSats4 = registry.counter("barnetrygd", "antall", "hjelpestonadSats4")

    val antallFeiledeOppgaver = registry.counter("prosesseringsenhetFeilede", "antall", "oppgaver")
    val antallFeiledeOmsorgsarbeid = registry.counter("prosesseringsenhetFeilede","antall", "omsorgsarbeid")
    val antallFeiledeGodskriving = registry.counter("prosesseringsenhetFeilede","antall", "godskriving")
    val antallFeiledeBrev = registry.counter("prosesseringsenhetFeilede","antall", "brev")

    val omsorgsyterHarMestOmsorgAvAlleOmsorgsytere = registry.counter("avslag", "antall", "omsorgsyterHarMestOmsorgAvAlleOmsorgsytere")
    val omsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr = registry.counter("avslag", "antall", "OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr")
    val omsorgsyterHarTilstrekkeligOmsorgsarbeid = registry.counter("avslag", "antall", "OmsorgsyterHarTilstrekkeligOmsorgsarbeid")
    val omsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr = registry.counter("avslag", "antall", "OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr")
    val omsorgsyterErForelderTilMottakerAvHjelpestønad = registry.counter("avslag", "antall", "OmsorgsyterErForelderTilMottakerAvHjelpestønad")
    val omsorgsmottakerOppfyllerAlderskravForHjelpestønad = registry.counter("avslag", "antall", "OmsorgsmottakerOppfyllerAlderskravForHjelpestønad")
    val omsorgsmottakerOppfyllerAlderskravForBarnetrygd = registry.counter("avslag", "antall", "OmsorgsmottakerOppfyllerAlderskravForBarnetrygd")
    val omsorgsyterOppfyllerAlderskrav = registry.counter("avslag", "antall", "OmsorgsyterOppfyllerAlderskrav")
}