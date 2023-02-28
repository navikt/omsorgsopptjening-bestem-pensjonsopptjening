package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "PERSON")
class Person(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "PERSON_ID", nullable = false)
    var id: Long? = null,
    @OneToMany(mappedBy = "person")
    var alleFnr: Set<Fnr> = setOf(),
    @Column(name = "FODSELSAR", nullable = false)
    var fodselsAr: Int? = null,
    @Column(name = "TIMESTAMP", nullable = false)
    var timestamp: LocalDateTime = LocalDateTime.now()
) {

    val gjeldendeFnr get() = alleFnr.first { it.gjeldende }

    val historiskeFnr get() = alleFnr.filter { !it.gjeldende }

    infix fun erSammePerson(annenPerson: Person) = (annenPerson.alleFnr intersect alleFnr).isNotEmpty()

    infix fun identifiseresAv(fnr: Fnr) = alleFnr.contains(fnr)
}