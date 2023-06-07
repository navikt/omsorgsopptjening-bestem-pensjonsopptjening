package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model

import jakarta.persistence.*
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person


@Entity
@Table(name = "OMSORGS_GRUNNLAG")
data class OmsorgsGrunnlag(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "OMSORGS_GRUNNLAG_ID", nullable = false)
    val id: Long? = null,

    @Column(name = "OMSORGS_AR", nullable = false)
    val omsorgsAr: Int,

    @Column(name = "HISTORISK", nullable = false)
    var historisk: Boolean = false,

    @OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @JoinColumn(
        name = "OMSORGS_GRUNNLAG_ID",
        nullable = false,
        referencedColumnName = "OMSORGS_GRUNNLAG_ID"
    )
    val omsorgsSaker: List<OmsorgsSak>,

    @OneToOne
    @JoinColumn(name = "OMSORGSYTER", nullable = false, referencedColumnName = "PERSON_ID")
    val omsorgsyter: Person,

    @Enumerated(EnumType.STRING)
    @Column(name = "OMSORGSTYPE", nullable = false)
    val omsorgstype: Omsorgstype,

    @Enumerated(EnumType.STRING)
    @Column(name = "KILDE", nullable = false)
    val kilde: Kilde,

    @Column(name = "KJORE_HASHE", nullable = false)
    val kjoreHashe: String,

    ) {

    fun getOmsorgsmottakere(omsorgsyter: Person) =
        getOmsorgsarbeidPerioderForRelevanteAr(omsorgsyter)
            .flatMap { barn -> barn.omsorgsmottakere }
            .distinctBy { it.gjeldendeFnr }

    fun getAndreOmsorgsytere() =
        getOmsorgsarbeidPerioderForRelevanteAr()
            .flatMap { it.omsorgsytere }
            .distinctBy { it.gjeldendeFnr }
            .filter { !it.erSammePerson(omsorgsyter) }


    fun getOmsorgsarbeidPerioderForRelevanteAr(omsorgsyter: Person, omsorgsmottaker: Person, prosent: Int) =
        getOmsorgsarbeidPerioderForRelevanteAr(omsorgsyter, omsorgsmottaker).filter { it.prosent == prosent }

    fun getOmsorgsarbeidPerioderForRelevanteAr(omsorgsyter: Person, omsorgsmottaker: Person) =
        getOmsorgsarbeidPerioderForRelevanteAr(omsorgsyter).filter { periode ->
            periode.omsorgsmottakere.any {
                it.erSammePerson(omsorgsmottaker)
            }
        }

    fun getOmsorgsarbeidPerioderForRelevanteAr(omsorgsyter: Person) =
        getOmsorgsarbeidPerioderForRelevanteAr()
            .filter { omsorgsyter isIn it.omsorgsytere }
            .filter { Periode(it.fom, it.tom).overlapper(omsorgsAr, omsorgsAr + 1) }

    private fun getOmsorgsarbeidPerioderForRelevanteAr() =
        omsorgsSaker.flatMap { sak -> sak.omsorgsvedtakPerioder }


}