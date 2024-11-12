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

val avslagTag = "avslag"
val antallTag = "antall"
val manuellTag = "manuell"

class OmsorgsarbeidProcessingMetrikker(private val registry: MeterRegistry) : Metrikker<List<FullførteBehandlinger?>?> {

    private val omsorgsarbeidProsessertTidsbruk = registry.timer("prosessering", "tidsbruk", "omsorgsarbeidProsessert")
    private val innvilget = registry.counter("behandling", antallTag, "innvilget")
    private val avslag = registry.counter("behandling", antallTag, avslagTag)
    private val manuell = registry.counter("behandling", antallTag, manuellTag)
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
                        is OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering -> registry.counter(harMestOmsorgName, avslagTag, antallTag).increment()
                        is OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering -> registry.counter(kunEtBarnName, avslagTag, antallTag).increment()
                        is OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering -> registry.counter(tilstrekkeligName, avslagTag, antallTag).increment()
                        is OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Vurdering -> registry.counter(kunEnPerÅrName, avslagTag, antallTag).increment()
                        is OmsorgsyterErForelderTilMottakerAvHjelpestønad.Vurdering -> registry.counter(erForelderHjelpestønadName, avslagTag, antallTag).increment()
                        is OmsorgsmottakerOppfyllerAlderskravForHjelpestønad.Vurdering -> registry.counter(mottakerOppfyllerHjelpestønadName, avslagTag, antallTag).increment()
                        is OmsorgsmottakerOppfyllerAlderskravForBarnetrygd.Vurdering -> registry.counter(mottakerOppfyllerBarnetrygdName, avslagTag, antallTag).increment()
                        is OmsorgsyterOppfyllerAlderskrav.Vurdering -> registry.counter(omsorgsyterOppfyllerAlderName, avslagTag, antallTag).increment()
                        is OmsorgsyterMottarBarnetrgyd.Vurdering -> registry.counter(omsorgsyterMottarName, avslagTag, antallTag).increment()
                        is OmsorgsyterHarGyldigOmsorgsarbeid.Vurdering -> registry.counter(omsorgsyterHarGyldigName, avslagTag, antallTag).increment()
                        is OmsorgsyterErMedlemIFolketrygden.Vurdering -> registry.counter(omsorgsyterMedlemName, avslagTag, antallTag).increment()
                        is OmsorgsyterErikkeOmsorgsmottaker.Vurdering -> registry.counter(omsorgsyterIkkeMottakerName, avslagTag, antallTag).increment()
                        is OmsorgsyterHarIkkeDødsdato.Vurdering -> registry.counter(omsorgsyterDødsdatoName, avslagTag, antallTag).increment()
                        is OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.Vurdering -> registry.counter(ytelseEøsName, avslagTag, antallTag).increment()
                        is OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.Vurdering -> registry.counter(ikkeGodskrevetAnnetFellesbarn, avslagTag, antallTag).increment()
                    }
                }
                statistikk.summertManuellPerVilkår.map {
                    when (it.key) {
                        is EllerVurdering -> {}
                        is OgVurdering -> {}
                        is OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering -> registry.counter(harMestOmsorgName, manuellTag, antallTag).increment()
                        is OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering -> registry.counter(kunEtBarnName, manuellTag, antallTag).increment()
                        is OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering -> registry.counter(tilstrekkeligName, manuellTag, antallTag).increment()
                        is OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Vurdering -> registry.counter(kunEnPerÅrName, manuellTag, antallTag).increment()
                        is OmsorgsyterErForelderTilMottakerAvHjelpestønad.Vurdering -> registry.counter(erForelderHjelpestønadName, manuellTag, antallTag).increment()
                        is OmsorgsmottakerOppfyllerAlderskravForHjelpestønad.Vurdering -> registry.counter(mottakerOppfyllerHjelpestønadName, manuellTag, antallTag).increment()
                        is OmsorgsmottakerOppfyllerAlderskravForBarnetrygd.Vurdering -> registry.counter(mottakerOppfyllerBarnetrygdName, manuellTag, antallTag).increment()
                        is OmsorgsyterOppfyllerAlderskrav.Vurdering -> registry.counter(omsorgsyterOppfyllerAlderName, manuellTag, antallTag).increment()
                        is OmsorgsyterMottarBarnetrgyd.Vurdering -> registry.counter(omsorgsyterMottarName, manuellTag, antallTag).increment()
                        is OmsorgsyterHarGyldigOmsorgsarbeid.Vurdering -> registry.counter(omsorgsyterHarGyldigName, manuellTag, antallTag).increment()
                        is OmsorgsyterErMedlemIFolketrygden.Vurdering -> registry.counter(omsorgsyterMedlemName, manuellTag, antallTag).increment()
                        is OmsorgsyterErikkeOmsorgsmottaker.Vurdering -> registry.counter(omsorgsyterIkkeMottakerName, manuellTag, antallTag).increment()
                        is OmsorgsyterHarIkkeDødsdato.Vurdering -> registry.counter(omsorgsyterDødsdatoName, manuellTag, antallTag).increment()
                        is OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.Vurdering -> registry.counter(ytelseEøsName, manuellTag, antallTag).increment()
                        is OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.Vurdering -> registry.counter(ikkeGodskrevetAnnetFellesbarn, manuellTag, antallTag).increment()
                    }
                }
            }
        }
    }
}