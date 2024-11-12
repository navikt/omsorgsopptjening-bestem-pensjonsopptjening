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
val vilkarTag = "vilkar"
val utfallTag = "utfall"

class OmsorgsarbeidProcessingMetrikker(private val registry: MeterRegistry) : Metrikker<List<FullførteBehandlinger?>?> {

    private val omsorgsarbeidProsessertTidsbruk = registry.timer("prosessering", "tidsbruk", "omsorgsarbeidProsessert")
    private val innvilget = registry.counter("behandling", antallTag, "innvilget")
    private val avslag = registry.counter("behandling", antallTag, avslagValue)
    private val manuell = registry.counter("behandling", antallTag, manuellValue)
    private val totalt = registry.counter("behandling", antallTag, "totalt")

    val vilkarUtfall = "vilkarUtfall"

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
                        is OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering -> registry.counter(vilkarUtfall, vilkarTag, harMestOmsorgName, utfallTag, avslagValue).increment()
                        is OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering -> registry.counter(vilkarUtfall, vilkarTag, kunEtBarnName, utfallTag, avslagValue).increment()
                        is OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering -> registry.counter(vilkarUtfall, vilkarTag, tilstrekkeligName, utfallTag, avslagValue).increment()
                        is OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Vurdering -> registry.counter(vilkarUtfall, vilkarTag, kunEnPerÅrName, utfallTag, avslagValue).increment()
                        is OmsorgsyterErForelderTilMottakerAvHjelpestønad.Vurdering -> registry.counter(vilkarUtfall, vilkarTag, erForelderHjelpestønadName, utfallTag, avslagValue).increment()
                        is OmsorgsmottakerOppfyllerAlderskravForHjelpestønad.Vurdering -> registry.counter(vilkarUtfall, vilkarTag, mottakerOppfyllerHjelpestønadName, utfallTag, avslagValue).increment()
                        is OmsorgsmottakerOppfyllerAlderskravForBarnetrygd.Vurdering -> registry.counter(vilkarUtfall, vilkarTag, mottakerOppfyllerBarnetrygdName, utfallTag, avslagValue).increment()
                        is OmsorgsyterOppfyllerAlderskrav.Vurdering -> registry.counter(vilkarUtfall, vilkarTag, omsorgsyterOppfyllerAlderName, utfallTag, avslagValue).increment()
                        is OmsorgsyterMottarBarnetrgyd.Vurdering -> registry.counter(vilkarUtfall, vilkarTag, omsorgsyterMottarName, utfallTag, avslagValue).increment()
                        is OmsorgsyterHarGyldigOmsorgsarbeid.Vurdering -> registry.counter(vilkarUtfall, vilkarTag, omsorgsyterHarGyldigName, utfallTag, avslagValue).increment()
                        is OmsorgsyterErMedlemIFolketrygden.Vurdering -> registry.counter(vilkarUtfall, vilkarTag, omsorgsyterMedlemName, utfallTag, avslagValue).increment()
                        is OmsorgsyterErikkeOmsorgsmottaker.Vurdering -> registry.counter(vilkarUtfall, vilkarTag, omsorgsyterIkkeMottakerName, utfallTag, avslagValue).increment()
                        is OmsorgsyterHarIkkeDødsdato.Vurdering -> registry.counter(vilkarUtfall, vilkarTag, omsorgsyterDødsdatoName, utfallTag, avslagValue).increment()
                        is OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.Vurdering -> registry.counter(vilkarUtfall, vilkarTag, ytelseEøsName, utfallTag, avslagValue).increment()
                        is OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.Vurdering -> registry.counter(vilkarUtfall, vilkarTag, ikkeGodskrevetAnnetFellesbarn, utfallTag, avslagValue).increment()
                    }
                }
                statistikk.summertManuellPerVilkår.map {
                    when (it.key) {
                        is EllerVurdering -> {}
                        is OgVurdering -> {}
                        is OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering -> registry.counter(vilkarUtfall, vilkarTag, harMestOmsorgName, utfallTag, manuellValue).increment()
                        is OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering -> registry.counter(vilkarUtfall, vilkarTag, kunEtBarnName, utfallTag, manuellValue).increment()
                        is OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering -> registry.counter(vilkarUtfall, vilkarTag, tilstrekkeligName, utfallTag, manuellValue).increment()
                        is OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Vurdering -> registry.counter(vilkarUtfall, vilkarTag, kunEnPerÅrName, utfallTag, manuellValue).increment()
                        is OmsorgsyterErForelderTilMottakerAvHjelpestønad.Vurdering -> registry.counter(vilkarUtfall, vilkarTag, erForelderHjelpestønadName, utfallTag, manuellValue).increment()
                        is OmsorgsmottakerOppfyllerAlderskravForHjelpestønad.Vurdering -> registry.counter(vilkarUtfall, vilkarTag, mottakerOppfyllerHjelpestønadName, utfallTag, manuellValue).increment()
                        is OmsorgsmottakerOppfyllerAlderskravForBarnetrygd.Vurdering -> registry.counter(vilkarUtfall, vilkarTag, mottakerOppfyllerBarnetrygdName, utfallTag, manuellValue).increment()
                        is OmsorgsyterOppfyllerAlderskrav.Vurdering -> registry.counter(vilkarUtfall, vilkarTag, omsorgsyterOppfyllerAlderName, utfallTag, manuellValue).increment()
                        is OmsorgsyterMottarBarnetrgyd.Vurdering -> registry.counter(vilkarUtfall, vilkarTag, omsorgsyterMottarName, utfallTag, manuellValue).increment()
                        is OmsorgsyterHarGyldigOmsorgsarbeid.Vurdering -> registry.counter(vilkarUtfall, vilkarTag, omsorgsyterHarGyldigName, utfallTag, manuellValue).increment()
                        is OmsorgsyterErMedlemIFolketrygden.Vurdering -> registry.counter(vilkarUtfall, vilkarTag, omsorgsyterMedlemName, utfallTag, manuellValue).increment()
                        is OmsorgsyterErikkeOmsorgsmottaker.Vurdering -> registry.counter(vilkarUtfall, vilkarTag, omsorgsyterIkkeMottakerName, utfallTag, manuellValue).increment()
                        is OmsorgsyterHarIkkeDødsdato.Vurdering -> registry.counter(vilkarUtfall, vilkarTag, omsorgsyterDødsdatoName, utfallTag, manuellValue).increment()
                        is OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.Vurdering -> registry.counter(vilkarUtfall, vilkarTag, ytelseEøsName, utfallTag, manuellValue).increment()
                        is OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.Vurdering -> registry.counter(vilkarUtfall, vilkarTag, ikkeGodskrevetAnnetFellesbarn, utfallTag, manuellValue).increment()
                    }
                }
            }
        }
    }
}