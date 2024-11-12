package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.metrics

import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics.Metrikker
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.EllerVurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførteBehandlinger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OgVurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsmottakerOppfyllerAlderskravForBarnetrygd
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsmottakerOppfyllerAlderskravForHjelpestønad
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterErForelderTilMottakerAvHjelpestønad
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterErMedlemIFolketrygden
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterErikkeOmsorgsmottaker
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarGyldigOmsorgsarbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarIkkeDødsdato
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarTilstrekkeligOmsorgsarbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterMottarBarnetrgyd
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterOppfyllerAlderskrav

val avslagValue = "avslag"
val antallTag = "antall"
val manuellValue = "manuell"

class OmsorgsarbeidProcessingMetrikker(private val registry: MeterRegistry) : Metrikker<List<FullførteBehandlinger?>?> {

    private val omsorgsarbeidProsessertTidsbruk = registry.timer("prosessering", "tidsbruk", "omsorgsarbeidProsessert")
    private val innvilget = registry.counter("behandling", antallTag, "innvilget")
    private val avslag = registry.counter("behandling", antallTag, avslagValue)
    private val manuell = registry.counter("behandling", antallTag, manuellValue)
    private val totalt = registry.counter("behandling", antallTag, "totalt")

    val harMestOmsorgName = "OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere"
    val kunEtBarnName = "OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr"
    val tilstrekkeligName = "OmsorgsyterHarTilstrekkeligOmsorgsarbeid"
    val kunEnPerÅrName = "OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr"
    val erForelderHjelpestønadName = "OmsorgsyterErForelderTilMottakerAvHjelpestønad"
    val mottakerOppfyllerHjelpestønadName = "OmsorgsmottakerOppfyllerAlderskravForHjelpestønad"
    val mottakerOppfyllerBarnetrygdName = "OmsorgsmottakerOppfyllerAlderskravForBarnetrygd"
    val omsorgsyterOppfyllerAlderName = "OmsorgsyterOppfyllerAlderskrav"
    val omsorgsyterMottarName = "OmsorgsyterMottarBarnetrgyd"
    val omsorgsyterHarGyldigName = "OmsorgsyterHarGyldigOmsorgsarbeid"
    val omsorgsyterMedlemName = "OmsorgsyterErMedlemIFolketrygden"
    val omsorgsyterIkkeMottakerName = "OmsorgsyterErikkeOmsorgsmottaker"
    val omsorgsyterDødsdatoName = "OmsorgsyterHarIkkeDodsdato"
    val ytelseEøsName = "OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs"
    val ikkeGodskrevetAnnetFellesbarn = "OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn"

    override fun oppdater(lambda: () -> List<FullførteBehandlinger?>?): List<FullførteBehandlinger?>? {
        return omsorgsarbeidProsessertTidsbruk.recordCallable(lambda)?.onEach { fullførte ->
            fullførte?.statistikk()?.let { statistikk ->
                totalt.increment()
                innvilget.increment(statistikk.innvilgetOpptjening.toDouble())
                avslag.increment(statistikk.avslåttOpptjening.toDouble())
                manuell.increment(statistikk.manuellBehandling.toDouble())
                statistikk.summertAvslagPerVilkår.map {
                    when (it.key) {
                        is EllerVurdering -> {}
                        is OgVurdering -> {}
                        is OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering -> registry.counter(harMestOmsorgName, antallTag, avslagValue).increment()
                        is OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering -> registry.counter(kunEtBarnName, antallTag, avslagValue).increment()
                        is OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering -> registry.counter(tilstrekkeligName, antallTag, avslagValue).increment()
                        is OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Vurdering -> registry.counter(kunEnPerÅrName, antallTag, avslagValue).increment()
                        is OmsorgsyterErForelderTilMottakerAvHjelpestønad.Vurdering -> registry.counter(erForelderHjelpestønadName, antallTag, avslagValue).increment()
                        is OmsorgsmottakerOppfyllerAlderskravForHjelpestønad.Vurdering -> registry.counter(mottakerOppfyllerHjelpestønadName, antallTag, avslagValue).increment()
                        is OmsorgsmottakerOppfyllerAlderskravForBarnetrygd.Vurdering -> registry.counter(mottakerOppfyllerBarnetrygdName, antallTag, avslagValue).increment()
                        is OmsorgsyterOppfyllerAlderskrav.Vurdering -> registry.counter(omsorgsyterOppfyllerAlderName, antallTag, avslagValue).increment()
                        is OmsorgsyterMottarBarnetrgyd.Vurdering -> registry.counter(omsorgsyterMottarName, antallTag, avslagValue).increment()
                        is OmsorgsyterHarGyldigOmsorgsarbeid.Vurdering -> registry.counter(omsorgsyterHarGyldigName, antallTag, avslagValue).increment()
                        is OmsorgsyterErMedlemIFolketrygden.Vurdering -> registry.counter(omsorgsyterMedlemName, antallTag, avslagValue).increment()
                        is OmsorgsyterErikkeOmsorgsmottaker.Vurdering -> registry.counter(omsorgsyterIkkeMottakerName, antallTag, avslagValue).increment()
                        is OmsorgsyterHarIkkeDødsdato.Vurdering -> registry.counter(omsorgsyterDødsdatoName, antallTag, avslagValue).increment()
                        is OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.Vurdering -> registry.counter(ytelseEøsName, antallTag, avslagValue).increment()
                        is OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.Vurdering -> registry.counter(ikkeGodskrevetAnnetFellesbarn, antallTag, avslagValue).increment()
                    }
                }
                statistikk.summertManuellPerVilkår.map {
                    when (it.key) {
                        is EllerVurdering -> {}
                        is OgVurdering -> {}
                        is OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering -> registry.counter(harMestOmsorgName, antallTag, manuellValue).increment()
                        is OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering -> registry.counter(kunEtBarnName, antallTag, manuellValue).increment()
                        is OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering -> registry.counter(tilstrekkeligName, antallTag, manuellValue).increment()
                        is OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Vurdering -> registry.counter(kunEnPerÅrName, antallTag, manuellValue).increment()
                        is OmsorgsyterErForelderTilMottakerAvHjelpestønad.Vurdering -> registry.counter(erForelderHjelpestønadName, antallTag, manuellValue).increment()
                        is OmsorgsmottakerOppfyllerAlderskravForHjelpestønad.Vurdering -> registry.counter(mottakerOppfyllerHjelpestønadName, antallTag, manuellValue).increment()
                        is OmsorgsmottakerOppfyllerAlderskravForBarnetrygd.Vurdering -> registry.counter(mottakerOppfyllerBarnetrygdName, antallTag, manuellValue).increment()
                        is OmsorgsyterOppfyllerAlderskrav.Vurdering -> registry.counter(omsorgsyterOppfyllerAlderName, antallTag, manuellValue).increment()
                        is OmsorgsyterMottarBarnetrgyd.Vurdering -> registry.counter(omsorgsyterMottarName, antallTag, manuellValue).increment()
                        is OmsorgsyterHarGyldigOmsorgsarbeid.Vurdering -> registry.counter(omsorgsyterHarGyldigName, antallTag, manuellValue).increment()
                        is OmsorgsyterErMedlemIFolketrygden.Vurdering -> registry.counter(omsorgsyterMedlemName, antallTag, manuellValue).increment()
                        is OmsorgsyterErikkeOmsorgsmottaker.Vurdering -> registry.counter(omsorgsyterIkkeMottakerName, antallTag, manuellValue).increment()
                        is OmsorgsyterHarIkkeDødsdato.Vurdering -> registry.counter(omsorgsyterDødsdatoName, antallTag, manuellValue).increment()
                        is OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.Vurdering -> registry.counter(ytelseEøsName, antallTag, manuellValue).increment()
                        is OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.Vurdering -> registry.counter(ikkeGodskrevetAnnetFellesbarn, antallTag, manuellValue).increment()
                    }
                }
            }
        }
    }
}