package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model

import jakarta.persistence.*
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person


@Entity
@Table(name = "OMSORGSARBEID_SNAPSHOT")
data class OmsorgsarbeidSnapshot(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "OMSORGSARBEID_SNAPSHOT_ID", nullable = false)
    val id: Long? = null,

    @Column(name = "OMSORGS_AR", nullable = false)
    val omsorgsAr: Int,

    @Column(name = "HISTORISK", nullable = false)
    var historisk: Boolean = false,

    @OneToMany(fetch =  FetchType.EAGER, cascade = [CascadeType.ALL])
    @JoinColumn(
        name = "OMSORGSARBEID_SNAPSHOT_ID",
        nullable = false,
        referencedColumnName = "OMSORGSARBEID_SNAPSHOT_ID"
    )
    val omsorgsarbeidSaker: List<OmsorgsarbeidSak>,

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

    //TODO rename method to omsorgsarbeidPeriode
    fun omsorgsArbeid() = omsorgsarbeidSaker.flatMap { sak -> sak.omsorgsarbeidPerioder }


    //TODO rename method to omsorgsarbeidPeriode
    fun omsorgsArbeid(person: Person): List<OmsorgsarbeidPeriode> =
        omsorgsArbeid().filter { person.erSammePerson(it.omsorgsyter) }


    //TODO rename method to omsorgsarbeidPeriode
    fun omsorgsArbeid(person: Person, omsorgsmottaker: Person): List<OmsorgsarbeidPeriode> =
        omsorgsArbeid(person).filter { periode -> periode.omsorgsmottakere.any { it.erSammePerson(omsorgsmottaker) } }

    fun getOmsorgsmottakere(omsorgsyter: Person): List<Person> =
        omsorgsArbeid(omsorgsyter).flatMap { barn -> barn.omsorgsmottakere }.distinctBy { it.gjeldendeFnr }
}