package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Pensjonspoeng

data class HentBrevopplysningForInfobrevOmsorgsyterForHjelpestønadsmottaker(
    private val hentPensjonspoengForOmsorgsopptjening: (fnr: String, år: Int, type: DomainOmsorgstype) -> Pensjonspoeng.Omsorg,
    private val hentPensjonspoengForInntekt: (fnr: String, år: Int) -> Pensjonspoeng.Inntekt,
) {
    fun get(
        omsorgsyter: Person,
        omsorgsmottaker: Person,
        omsorgstype: DomainOmsorgstype,
        omsorgsAr: Int,
    ): Brevopplysninger {
        return when (omsorgstype) {
            DomainOmsorgstype.BARNETRYGD -> {
                Brevopplysninger.Ingen
            }

            DomainOmsorgstype.HJELPESTØNAD -> {
                //TODO føles som man kanskje burde sjekke begge omsorgstyper for å unngå brev for tilfeller hvor omsorgsyter har poeng fra barnetryg for året før?
                hentPensjonspoengForOmsorgsopptjening(
                    omsorgsyter.fnr,
                    omsorgsAr - 1,
                    omsorgstype
                ).let { pensjonspoeng ->
                    if (pensjonspoeng.poeng == 0.0) {
                        return Brevopplysninger.InfobrevOmsorgsyterForHjelpestønadsmottaker(BrevÅrsak.OMSORGSYTER_INGEN_PENSJONSPOENG_FORRIGE_ÅR)
                    }
                }

                when (val foreldre = omsorgsmottaker.finnForeldre()) {
                    is Foreldre.Identifisert -> {
                        val farEllerMedMorErOmsorgsyter = foreldre.farEllerMedmor.ident == omsorgsyter.fnr
                        val morErOmsorgsyter = foreldre.mor.ident == omsorgsyter.fnr

                        if (!(farEllerMedMorErOmsorgsyter || morErOmsorgsyter)) {
                            return Brevopplysninger.InfobrevOmsorgsyterForHjelpestønadsmottaker(BrevÅrsak.OMSORGSYTER_IKKE_FORELDER_AV_OMSORGSMOTTAKER)
                        } else {
                            if (farEllerMedMorErOmsorgsyter && Pensjonspoeng.opptjenesForOmsorg() > hentPensjonspoengForInntekt(
                                    foreldre.mor.ident,
                                    omsorgsAr,
                                ).poeng
                            ) {
                                return Brevopplysninger.InfobrevOmsorgsyterForHjelpestønadsmottaker(BrevÅrsak.ANNEN_FORELDER_HAR_LAVERE_PENSJONSPOENG)
                            }
                            if (morErOmsorgsyter && Pensjonspoeng.opptjenesForOmsorg() > hentPensjonspoengForInntekt(
                                    foreldre.farEllerMedmor.ident,
                                    omsorgsAr,
                                ).poeng
                            ) {
                                return Brevopplysninger.InfobrevOmsorgsyterForHjelpestønadsmottaker(BrevÅrsak.ANNEN_FORELDER_HAR_LAVERE_PENSJONSPOENG)
                            }
                        }
                    }

                    Foreldre.Ukjent -> {
                        return Brevopplysninger.InfobrevOmsorgsyterForHjelpestønadsmottaker(BrevÅrsak.FORELDRE_ER_UKJENT)
                    }
                }

                return Brevopplysninger.Ingen
            }
        }
    }
}