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

    @OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
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

    fun omsorgsarbeidPerioder(omsorgsyter: Person): List<OmsorgsarbeidPeriode> {
        return omsorgsarbeidPerioder().filter { omsorgsyter isIn it.omsorgsytere }
    }

    fun omsorgsarbeidPerioder(omsorgsyter: Person, omsorgsmottaker: Person): List<OmsorgsarbeidPeriode> {
        return omsorgsarbeidPerioder(omsorgsyter).filter { periode ->
            periode.omsorgsmottakere.any {
                it.erSammePerson(
                    omsorgsmottaker
                )
            }
        }
    }

    fun omsorgsarbeidPerioder(omsorgsyter: Person, omsorgsmottaker: Person, prosent: Int): List<OmsorgsarbeidPeriode> {
        return omsorgsarbeidPerioder(omsorgsyter, omsorgsmottaker).filter { it.prosent == prosent }
    }

    fun getOmsorgsmottakere(omsorgsyter: Person): List<Person> {
        return omsorgsarbeidPerioder(omsorgsyter).flatMap { barn -> barn.omsorgsmottakere }
            .distinctBy { it.gjeldendeFnr }
    }

    fun getRelaterteOmsorgsytere() = omsorgsarbeidPerioder(omsorgsyter)
        .flatMap { barn -> barn.omsorgsmottakere }
        .distinctBy { it.gjeldendeFnr }
        .filter { !it.erSammePerson(omsorgsyter) }

    private fun omsorgsarbeidPerioder(): List<OmsorgsarbeidPeriode> {
        return omsorgsarbeidSaker.flatMap { sak -> sak.omsorgsarbeidPerioder }
    }
}