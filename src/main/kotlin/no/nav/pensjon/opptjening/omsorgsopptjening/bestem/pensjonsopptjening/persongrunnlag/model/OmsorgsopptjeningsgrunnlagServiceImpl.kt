package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.medlemskap.MedlemskapsUnntakOppslag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Medlemskapsgrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Medlemskapsunntak
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Ytelsegrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.PersonOppslag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.ytelse.YtelseOppslag
import java.time.Month
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding as PersongrunnlagMeldingKafka
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding.Persongrunnlag as KafkaPersongrunnlag

internal class OmsorgsopptjeningsgrunnlagServiceImpl(
    private val personOppslag: PersonOppslag,
    private val medlemskapsUnntakOppslag: MedlemskapsUnntakOppslag,
    private val ytelseOppslag: YtelseOppslag,
) : OmsorgsopptjeningsgrunnlagService {

    override fun lagOmsorgsopptjeningsgrunnlag(melding: PersongrunnlagMelding.Mottatt): List<OmsorgsopptjeningGrunnlag> {
        return melding.innhold.berikDatagrunnlag().tilOmsorgsopptjeningsgrunnlag()
    }

    fun PersongrunnlagMeldingKafka.berikDatagrunnlag(): BeriketDatagrunnlag {
        val personer = hentPersoner()
            .map { personOppslag.hentPerson(it) }
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
            persongrunnlag = persongrunnlag
                .oppdaterFødselsnummerOgSlåSammenLikePerioder { persondata.finnPerson(it) }
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
                            medlemskapsunntak = medlemskapsUnntakOppslag.hentUnntaksperioder(
                                fnr = omsorgsyter.fnr,
                                fraOgMed = første,
                                tilOgMed = siste,
                            )
                        )
                    } else {
                        Medlemskapsgrunnlag(
                            medlemskapsunntak = Medlemskapsunntak(
                                ikkeMedlem = emptySet(),
                                pliktigEllerFrivillig = emptySet(),
                                rådata = ""
                            )
                        )
                    }


                    val ytelsegrunnlag = if (omsorgsperioder.isNotEmpty()) {
                        val (første, siste) = omsorgsperioder.minOf { it.fom } to omsorgsperioder.maxOf { it.tom }
                        val alderspensjon = ytelseOppslag.hentLøpendeAlderspensjon(
                            fnr = omsorgsyter.fnr,
                            fraOgMed = første,
                            tilOgMed = siste
                        )
                        val uføretrygd = ytelseOppslag.hentLøpendeUføretrygd(
                            fnr = omsorgsyter.fnr,
                            fraOgMed = første,
                            tilOgMed = siste
                        )
                        Ytelsegrunnlag(setOf(alderspensjon, uføretrygd))
                    } else {
                        Ytelsegrunnlag(emptySet())
                    }

                    Persongrunnlag(
                        omsorgsyter = omsorgsyter,
                        omsorgsperioder = omsorgsperioder,
                        hjelpestønadperioder = hjelpestønadsperioder,
                        medlemskapsgrunnlag = medlemskapsgrunnlag,
                        ytelsegrunnlag = ytelsegrunnlag,
                    )
                },
            innlesingId = innlesingId,
            correlationId = correlationId
        )
    }

    fun List<KafkaPersongrunnlag>.oppdaterFødselsnummerOgSlåSammenLikePerioder(finnPerson: (fnr: String) -> Person): List<KafkaPersongrunnlag> {
        return map { persongrunnlag ->
            KafkaPersongrunnlag.of(
                omsorgsyter = finnPerson(persongrunnlag.omsorgsyter).fnr,
                omsorgsperioder = persongrunnlag.omsorgsperioder
                    .map { (it.copy(omsorgsmottaker = finnPerson(it.omsorgsmottaker).fnr)) },
                hjelpestønadsperioder = persongrunnlag.hjelpestønadsperioder
                    .map { (it.copy(omsorgsmottaker = finnPerson(it.omsorgsmottaker).fnr)) }
            )
        }.groupBy { finnPerson(it.omsorgsyter) }
            .map {
                KafkaPersongrunnlag.of(
                    omsorgsyter = it.key.fnr,
                    omsorgsperioder = it.value.flatMap { it.omsorgsperioder },
                    hjelpestønadsperioder = it.value.flatMap { it.hjelpestønadsperioder })
            }
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
                            medlemskapsgrunnlag = persongrunnlag.medlemskapsgrunnlag.avgrensForÅr(år),
                            ytelsegrunnlag = persongrunnlag.ytelsegrunnlag.avgrensForÅr(år)
                        )
                    })
            }
    }

    class PersonIkkeIdentifisertAvIdentException(msg: String = "Person kunne ikke identifiseres av oppgitt ident") :
        RuntimeException(msg)
}