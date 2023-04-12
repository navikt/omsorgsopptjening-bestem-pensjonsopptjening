package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka.producer.OmsorgsOpptejningProducer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.OmsorgsArbeidService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidSnapshot
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.IndividuellVilkarsvurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.SammenstiltVilkarsvurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.SamletVilkarsresultat
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Utfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.VilkarsVurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.hentOmsorgForBarnUnder6VilkarsVurderinger
import org.springframework.stereotype.Service

@Service
class OmsorgsOpptjeningService(
    private val omsorgsArbeidService: OmsorgsArbeidService,
    private val omsorgsOpptejningProducer: OmsorgsOpptejningProducer,
    private val individuellVilkarsvurdering: IndividuellVilkarsvurdering,
    private val sammenstiltVilkarsvurdering: SammenstiltVilkarsvurdering
) {

    fun behandlOmsorgsarbeid(omsorgsarbeidSnapshot: OmsorgsarbeidSnapshot) {
        val relaterteOmsorgsarbeidSnapshot = omsorgsArbeidService.relaterteSnapshot(omsorgsarbeidSnapshot)

        val individuellVurderinger: List<SamletVilkarsresultat> = (relaterteOmsorgsarbeidSnapshot + omsorgsarbeidSnapshot).map { individuellVilkarsvurdering.vilkarsvurder(omsorgsarbeidSnapshot) }

        val vilkarsVurdering = individuellVilkarsvurdering.vilkarsvurder(omsorgsarbeidSnapshot)

        individuellVurderinger.forEach {
            publiserOmsorgsOpptjening(it.snapshot, it.individueltVilkarsresultat!!)
        }
    }


    private fun publiserOmsorgsOpptjening(
        omsorgsArbeidSnapshot: OmsorgsarbeidSnapshot,
        vilkarsVurdering: VilkarsVurdering<*>
    ) =
        omsorgsOpptejningProducer.publiserOmsorgsopptejning(
            OmsorgsOpptjening(
                omsorgsAr = omsorgsArbeidSnapshot.omsorgsAr,
                person = omsorgsArbeidSnapshot.omsorgsyter,
                grunnlag = omsorgsArbeidSnapshot,
                omsorgsopptjeningResultater = vilkarsVurdering,
                utfall = vilkarsVurdering.utfall,
                omsorgsmottakereInvilget = hentOmsorgForBarnUnder6VilkarsVurderinger(vilkarsVurdering)
                    .filter { it.utfall == Utfall.INVILGET }
                    .map { it.grunnlag.omsorgsmottaker }
            )
        )
}