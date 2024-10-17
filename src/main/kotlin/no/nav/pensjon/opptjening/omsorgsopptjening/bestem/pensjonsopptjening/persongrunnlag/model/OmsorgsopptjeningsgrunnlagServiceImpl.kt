package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.medlemskap.MedlemskapsUnntakOppslag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics.time
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Medlemskapsgrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Medlemskapsunntak
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.PersonOppslag
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.KanSlåsSammen.Companion.slåSammenLike
import java.time.Month
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding as PersongrunnlagMeldingKafka
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding.Persongrunnlag as KafkaPersongrunnlag

internal class OmsorgsopptjeningsgrunnlagServiceImpl(
    private val personOppslag: PersonOppslag,
    private val medlemskapsUnntakOppslag: MedlemskapsUnntakOppslag
) : OmsorgsopptjeningsgrunnlagService {

    override fun lagOmsorgsopptjeningsgrunnlag(melding: PersongrunnlagMelding.Mottatt): List<OmsorgsopptjeningGrunnlag> {
        return time("lagOmsorgsopptjeningsgrunnlag"){ melding.innhold.berikDatagrunnlag().tilOmsorgsopptjeningsgrunnlag() }
    }

    fun PersongrunnlagMeldingKafka.berikDatagrunnlag(): BeriketDatagrunnlag {
        val personer = hentPersoner()
            .map { time("personOppslag.hentPerson(it)"){ personOppslag.hentPerson(it) } }
            .toSet()

        return berikDatagrunnlag(personer)
    }

    fun BeriketDatagrunnlag.tilOmsorgsopptjeningsgrunnlag(): List<OmsorgsopptjeningGrunnlag> {
        return grunnlagPerMottakerPerÅr()
    }

    fun PersongrunnlagMeldingKafka.berikDatagrunnlag(persondata: Set<Person>): BeriketDatagrunnlag {
        fun Set<Person>.finnPerson(fnr: String): Person {
            return singleOrNull { it.identifisertAv(fnr) } ?: throw PersonIkkeIdentifisertAvIdentException()
        }

        return BeriketDatagrunnlag(
            omsorgsyter = persondata.finnPerson(omsorgsyter),
            persongrunnlag = persongrunnlag.consolidate { persondata.finnPerson(it.omsorgsyter) }
                .map { persongrunnlag ->
                    val omsorgsyter = persondata.finnPerson(persongrunnlag.omsorgsyter)

                    val omsorgsperioder = persongrunnlag.omsorgsperioder.map { omsorgVedtakPeriode ->
                        Omsorgsperiode(
                            fom = omsorgVedtakPeriode.fom,
                            tom = omsorgVedtakPeriode.tom,
                            omsorgstype = omsorgVedtakPeriode.omsorgstype.toDomain() as DomainOmsorgstype.Barnetrygd,
                            omsorgsmottaker = persondata.finnPerson(omsorgVedtakPeriode.omsorgsmottaker),
                            kilde = omsorgVedtakPeriode.kilde.toDomain(),
                            utbetalt = omsorgVedtakPeriode.utbetalt,
                            landstilknytning = omsorgVedtakPeriode.landstilknytning.toDomain()
                        )
                    }

                    val hjelpestønadsperioder = persongrunnlag.hjelpestønadsperioder.map { hjelpestønadperiode ->
                        Hjelpestønadperiode(
                            fom = hjelpestønadperiode.fom,
                            tom = hjelpestønadperiode.tom,
                            omsorgstype = hjelpestønadperiode.omsorgstype.toDomain() as DomainOmsorgstype.Hjelpestønad,
                            omsorgsmottaker = persondata.finnPerson(hjelpestønadperiode.omsorgsmottaker),
                            kilde = hjelpestønadperiode.kilde.toDomain()
                        )
                    }

                    val medlemskapsgrunnlag = if (omsorgsperioder.isNotEmpty()) {
                        val (første, siste) = omsorgsperioder.minOf { it.fom } to omsorgsperioder.maxOf { it.tom }
                        Medlemskapsgrunnlag(
                            medlemskapsunntak = time("medlemskapsUnntakOppslag.hentUnntaksperioder") {
                                medlemskapsUnntakOppslag.hentUnntaksperioder(
                                    fnr = omsorgsyter.fnr,
                                    fraOgMed = første,
                                    tilOgMed = siste,
                                )
                            })
                    } else {
                        Medlemskapsgrunnlag(
                            medlemskapsunntak = Medlemskapsunntak(
                                ikkeMedlem = emptySet(),
                                pliktigEllerFrivillig = emptySet(),
                                rådata = ""
                            )
                        )
                    }

                    Persongrunnlag(
                        omsorgsyter = omsorgsyter,
                        omsorgsperioder = omsorgsperioder,
                        hjelpestønadperioder = hjelpestønadsperioder,
                        medlemskapsgrunnlag = medlemskapsgrunnlag,
                    )
                },
            innlesingId = innlesingId,
            correlationId = correlationId
        )
    }


    fun consolidatePersongrunnlag(
        key: (KafkaPersongrunnlag) -> Any,
        persongrunnlag: List<KafkaPersongrunnlag>
    ): List<KafkaPersongrunnlag> {
        fun merge(persongrunnlag: List<KafkaPersongrunnlag>): KafkaPersongrunnlag {
            // todo : sjekk omsorgsperioder
            val omsorgsyter = persongrunnlag.first().omsorgsyter
            val omsorgsperioder = persongrunnlag.flatMap { it.omsorgsperioder }.sortedBy { it.fom }.slåSammenLike()
            val hjelpestønadperioder =
                persongrunnlag.flatMap { it.hjelpestønadsperioder }.sortedBy { it.fom }.slåSammenLike()
            return KafkaPersongrunnlag(
                omsorgsyter = omsorgsyter,
                omsorgsperioder = omsorgsperioder,
                hjelpestønadsperioder = hjelpestønadperioder,
            )
        }

        val persongrunnlag = persongrunnlag.groupBy { key(it) }.values.map { merge(it) }
        return persongrunnlag
    }

    fun List<KafkaPersongrunnlag>.consolidate(key: (KafkaPersongrunnlag) -> Any): List<KafkaPersongrunnlag> {
        return consolidatePersongrunnlag(key, this)
    }

    fun BeriketDatagrunnlag.grunnlagPerMottakerPerÅr(): List<OmsorgsopptjeningGrunnlag> {
        return `opprett grunnlag per omsorgsmottaker per år`()
            .fold(emptyList()) { acc, (mottaker, år, grunnlag) ->
                acc + when (mottaker.erFødt(år)) {
                    true -> {
                        listOf(
                            OmsorgsopptjeningGrunnlag.FødtIOmsorgsår.IkkeFødtDesember(
                                omsorgsAr = år,
                                omsorgsmottaker = mottaker,
                                grunnlag = grunnlag
                            )
                        )
                    }

                    false -> {
                        when (mottaker.erFødt(år - 1, Month.DECEMBER)) {
                            true -> {
                                listOf(
                                    OmsorgsopptjeningGrunnlag.FødtIOmsorgsår.FødtDesember(
                                        omsorgsAr = år - 1,
                                        omsorgsmottaker = mottaker,
                                        grunnlag = grunnlag
                                    ),
                                    OmsorgsopptjeningGrunnlag.IkkeFødtIOmsorgsår(
                                        omsorgsAr = år,
                                        omsorgsmottaker = mottaker,
                                        grunnlag = grunnlag
                                    ),
                                )
                            }

                            false -> {
                                listOf(
                                    OmsorgsopptjeningGrunnlag.IkkeFødtIOmsorgsår(
                                        omsorgsAr = år,
                                        omsorgsmottaker = mottaker,
                                        grunnlag = grunnlag
                                    )
                                )
                            }
                        }
                    }
                }
            }
    }

    fun BeriketDatagrunnlag.`opprett grunnlag per omsorgsmottaker per år`(): List<Triple<Person, Int, BeriketDatagrunnlag>> {
        return `avgrens grunnlagsdata per omsorgsmottaker`()
            .flatMap { (omsorgsmottaker, grunnlagPerMottaker) ->
                grunnlagPerMottaker.`avgrens for omsorgsår`()
                    .map { (år, grunnlagPerÅrPerMottaker) ->
                        Triple(omsorgsmottaker, år, grunnlagPerÅrPerMottaker)
                    }
            }
    }

    fun BeriketDatagrunnlag.`avgrens grunnlagsdata per omsorgsmottaker`(): Map<Person, BeriketDatagrunnlag> {
        return omsorgsytersOmsorgsmottakere
            .sortedBy { it.fødselsdato } //eldste barn først
            .associateWith { omsorgsmottaker ->
                copy(persongrunnlag = persongrunnlag
                    .map { persongrunnlag ->
                        persongrunnlag.copy(
                            omsorgsperioder = persongrunnlag.omsorgsperioder.filter { it.omsorgsmottaker == omsorgsmottaker },
                            hjelpestønadperioder = persongrunnlag.hjelpestønadperioder.filter { it.omsorgsmottaker == omsorgsmottaker }
                        )
                    }
                )
            }
    }

    fun BeriketDatagrunnlag.`avgrens for omsorgsår`(): Map<Int, BeriketDatagrunnlag> {
        return omsorgsytersOmsorgsår
            .associateWith { år ->
                copy(persongrunnlag = persongrunnlag
                    .map { persongrunnlag ->
                        persongrunnlag.copy(
                            omsorgsperioder = persongrunnlag.omsorgsperioder
                                .filter { it.periode.overlapper(år) }
                                .map { barnetrygdPeriode ->
                                    barnetrygdPeriode.periode.overlappendeMåneder(år)
                                        .let {
                                            Omsorgsperiode(
                                                fom = it.min(),
                                                tom = it.max(),
                                                omsorgstype = barnetrygdPeriode.omsorgstype,
                                                omsorgsmottaker = barnetrygdPeriode.omsorgsmottaker,
                                                kilde = barnetrygdPeriode.kilde,
                                                utbetalt = barnetrygdPeriode.utbetalt,
                                                landstilknytning = barnetrygdPeriode.landstilknytning,
                                            )
                                        }
                                },
                            hjelpestønadperioder = persongrunnlag.hjelpestønadperioder
                                .filter { it.periode.overlapper(år) }
                                .map { hjelpestønadperiode ->
                                    hjelpestønadperiode.periode.overlappendeMåneder(år)
                                        .let {
                                            Hjelpestønadperiode(
                                                fom = it.min(),
                                                tom = it.max(),
                                                omsorgstype = hjelpestønadperiode.omsorgstype,
                                                omsorgsmottaker = hjelpestønadperiode.omsorgsmottaker,
                                                kilde = hjelpestønadperiode.kilde
                                            )
                                        }
                                },
                            medlemskapsgrunnlag = persongrunnlag.medlemskapsgrunnlag.avgrensForÅr(år)
                        )
                    })
            }
    }

    class PersonIkkeIdentifisertAvIdentException(msg: String = "Person kunne ikke identifiseres av oppgitt ident") :
        RuntimeException(msg)
}