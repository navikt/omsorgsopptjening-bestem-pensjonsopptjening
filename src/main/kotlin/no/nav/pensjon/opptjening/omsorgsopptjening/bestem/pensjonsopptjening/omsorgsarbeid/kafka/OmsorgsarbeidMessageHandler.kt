package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.kafka

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketDatagrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketVedtaksperiode
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.BehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.*
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.VilkårsvurderingFactory
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.OmsorgsgrunnlagMelding
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Month

@Service
class OmsorgsarbeidMessageHandler(
    private val omsorgsgrunnlagService: OmsorgsgrunnlagService,
    private val behandlingRepo: BehandlingRepo,
    private val gyldigOpptjeningsår: GyldigOpptjeningår
) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    fun handle(grunnlag: OmsorgsgrunnlagMelding): List<FullførtBehandling> {
        return grunnlag
            .berik()
            .barnetrygdgrunnlagPerMottakerPerÅr()
            .filter {
                log.info("Filtrerer vekk andre opptjeningsår enn: ${gyldigOpptjeningsår.get()}")
                gyldigOpptjeningsår.get().contains(it.omsorgsAr)
            }
            .map {
                log.info("Utfører vilkårsvurdering")
                behandlingRepo.persist(
                    Behandling(
                        grunnlag = it,
                        vurderVilkår = VilkårsvurderingFactory(
                            grunnlag = it,
                            behandlingRepo = behandlingRepo
                        )
                    )
                )
            }
    }

    private fun OmsorgsgrunnlagMelding.berik(): BeriketDatagrunnlag {
        log.info("Beriker datagrunnlag")
        return omsorgsgrunnlagService.berikDatagrunnlag(this)
    }

    private fun BeriketDatagrunnlag.barnetrygdgrunnlagPerMottakerPerÅr(): List<BarnetrygdGrunnlag> {
        log.info("Lager grunnlag per omsorgsmottaker per opptjeningsår")
        return perMottakerPerÅr().fold(emptyList()) { acc, (mottaker, år, grunnlag) ->
            acc + when (mottaker.erFødt(år)) {
                true -> {
                    require(
                        !mottaker.erFødt(
                            årstall = år,
                            måned = Month.DECEMBER
                        )
                    ) { "Forventer ikke grunnlag for fødselsåret dersom barn er født i desember" }
                    listOf(
                        BarnetrygdGrunnlag.FødtIOmsorgsår.IkkeFødtDesember(
                            omsorgsAr = år,
                            grunnlag = grunnlag
                        )
                    )
                }

                false -> {
                    when (mottaker.erFødt(år - 1, Month.DECEMBER)) {
                        true -> {
                            listOf(
                                BarnetrygdGrunnlag.FødtIOmsorgsår.FødtDesember(
                                    omsorgsAr = år - 1,
                                    grunnlag = grunnlag
                                ),
                                BarnetrygdGrunnlag.IkkeFødtIOmsorgsår(
                                    omsorgsAr = år,
                                    grunnlag = grunnlag
                                ),
                            )
                        }

                        false -> {
                            listOf(
                                BarnetrygdGrunnlag.IkkeFødtIOmsorgsår(
                                    omsorgsAr = år,
                                    grunnlag = grunnlag
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun BeriketDatagrunnlag.perMottakerPerÅr(): List<Triple<Person, Int, BeriketDatagrunnlag>> {
        return perMottaker().flatMap { (omsorgsmottaker, grunnlagPerMottaker) ->
            grunnlagPerMottaker.perÅr().map { (år, grunnlagPerÅrPerMottaker) ->
                Triple(omsorgsmottaker, år, grunnlagPerÅrPerMottaker)
            }
        }
    }

    private fun BeriketDatagrunnlag.perMottaker(): Map<Person, BeriketDatagrunnlag> {
        return omsorgsmottakere().associateWith { omsorgsmottaker ->
            copy(omsorgsSaker = omsorgsSaker.map { sak -> sak.copy(omsorgVedtakPerioder = sak.omsorgVedtakPerioder.filter { it.omsorgsmottaker == omsorgsmottaker }) })
        }
    }

    private fun BeriketDatagrunnlag.perÅr(): Map<Int, BeriketDatagrunnlag> {
        val alleÅrIGrunnlag = omsorgsSaker.flatMap { it.omsorgVedtakPerioder }
            .flatMap { it.periode.alleMåneder() }
            .map { it.year }
            .distinct()

        return alleÅrIGrunnlag.associateWith { år ->
            copy(omsorgsSaker = omsorgsSaker
                .map { sak ->
                    sak.copy(omsorgVedtakPerioder = sak.omsorgVedtakPerioder
                        .filter { it.periode.overlapper(år) }
                        .map { barnetrygdPeriode ->
                            barnetrygdPeriode.periode.overlappendeMåneder(år).let {
                                BeriketVedtaksperiode(
                                    fom = it.min(),
                                    tom = it.max(),
                                    prosent = barnetrygdPeriode.prosent,
                                    omsorgsmottaker = barnetrygdPeriode.omsorgsmottaker
                                )
                            }
                        })
                })
        }
    }
}