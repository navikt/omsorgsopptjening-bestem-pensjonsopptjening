package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.kafka

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketDatagrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketVedtaksperiode
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.BehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.*
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.deserialize
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.KafkaMessageType
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.PersonMedFødselsår
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.stereotype.Service
import java.time.Month

@Service
class OmsorgsarbeidMessageHandler(
    private val omsorgsgrunnlagService: OmsorgsgrunnlagService,
    private val behandlingRepo: BehandlingRepo,
    private val gyldigOpptjeningsår: GyldigOpptjeningår
) {
    fun handle(record: ConsumerRecord<String, String>): List<FullførtBehandling> {
        return if (record.kafkaMessageType() == KafkaMessageType.OMSORGSARBEID) {
            deserialize<OmsorgsGrunnlag>(record.value())
                .berikDatagrunnlag()
                .barnetrygdgrunnlagPerMottakerPerÅr()
                /**
                 * TODO
                 * vilkår ingen godskrevet for samme barn
                 * vilkår yter ikke godskrevet annet barn samme år
                 * filtrer vekk all år som ikke er "gydlig opptjeningsår"?
                 */
                .filter { gyldigOpptjeningsår.get().contains(it.omsorgsAr) }
                .map {
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
        } else {
            emptyList()
        }
    }

    private fun OmsorgsGrunnlag.berikDatagrunnlag(): BeriketDatagrunnlag {
        return omsorgsgrunnlagService.berikDatagrunnlag(this)
    }

    private fun BeriketDatagrunnlag.barnetrygdgrunnlagPerMottakerPerÅr(): List<BarnetrygdGrunnlag> {
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

    private fun BeriketDatagrunnlag.perMottakerPerÅr(): List<Triple<PersonMedFødselsår, Int, BeriketDatagrunnlag>> {
        return perMottaker().flatMap { (omsorgsmottaker, grunnlagPerMottaker) ->
            grunnlagPerMottaker.perÅr().map { (år, grunnlagPerÅrPerMottaker) ->
                Triple(omsorgsmottaker, år, grunnlagPerÅrPerMottaker)
            }
        }
    }

    private fun BeriketDatagrunnlag.perMottaker(): Map<PersonMedFødselsår, BeriketDatagrunnlag> {
        return omsorgsmottakere().associateWith { omsorgsmottaker ->
            copy(omsorgsSaker = omsorgsSaker.map { sak -> sak.copy(omsorgVedtakPeriode = sak.omsorgVedtakPeriode.filter { it.omsorgsmottaker == omsorgsmottaker }) })
        }
    }

    private fun BeriketDatagrunnlag.perÅr(): Map<Int, BeriketDatagrunnlag> {
        val alleÅrIGrunnlag = omsorgsSaker.flatMap { it.omsorgVedtakPeriode }
            .flatMap { it.periode.alleMåneder() }
            .map { it.year }
            .distinct()

        return alleÅrIGrunnlag.associateWith { år ->
            copy(omsorgsSaker = omsorgsSaker
                .map { sak ->
                    sak.copy(omsorgVedtakPeriode = sak.omsorgVedtakPeriode
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