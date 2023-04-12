package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka.producer.OmsorgsOpptejningProducer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.OmsorgsArbeidService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidSnapshot
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.IndividuellVilkarsvurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.SammenstiltVilkarsvurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.SamletVilkarsresultat
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Utfall
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

        val samledeVilkarsresultat =
            (relaterteOmsorgsarbeidSnapshot + omsorgsarbeidSnapshot).map { SamletVilkarsresultat(snapshot = it) }

        samledeVilkarsresultat.forEach {
            it.individueltVilkarsresultat = individuellVilkarsvurdering.vilkarsvurder(it.snapshot)
        }

        //TODO samlet vilkarsvurdering


        samledeVilkarsresultat.forEach {
            publiserOmsorgsOpptjening(it)
        }
    }


    private fun publiserOmsorgsOpptjening(samletVilkarsresultat: SamletVilkarsresultat) {
        //TODO legg in sammenstilt opplegg i omsorgsopptjening!!
        val omsorgsArbeidSnapshot = samletVilkarsresultat.snapshot
        val vilkarsVurdering = samletVilkarsresultat.individueltVilkarsresultat!!

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
}