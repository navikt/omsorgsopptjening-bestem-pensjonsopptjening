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
    @OneToMany(mappedBy = "person", fetch = FetchType.EAGER, cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    var alleFnr: MutableSet<Fnr> = mutableSetOf(),
    @Column(name = "FODSELSAR", nullable = false)
    var fodselsAr: Int? = null,
    @Column(name = "TIMESTAMP", nullable = false)
    var timestamp: LocalDateTime = LocalDateTime.now()
) {

    val gjeldendeFnr get() = alleFnr.first { it.gjeldende }

    val historiskeFnr get() = alleFnr.filter { !it.gjeldende }

    infix fun erSammePerson(annenPerson: Person) = (annenPerson.alleFnr intersect alleFnr).isNotEmpty()

    infix fun identifiseresAv(fnr: Fnr) = alleFnr.contains(fnr)

    infix fun identifiseresAv(fnr: String) = alleFnr.map { it.fnr }.contains(fnr)

    /**
     * Legg til fnr dersom fnr ikke finnes i DB fra før
     **/
    fun oppdaterGjeldendeFnr(fnr: String) {
        alleFnr.firstOrNull { it.gjeldende }?.let {
            alleFnr.remove(it)
        }

        alleFnr.add(Fnr(fnr = fnr, gjeldende = true, person = this))
    }

    /**
     * Dersom historisk fnr fra PDL ikke eksisterer i DB legges den til
     * Dersom historisk fnr eksisterer i DB men ikke i PDL fjernes den fra DB
     */
    fun oppdaterHistoriskeFnr(fnrListe: List<String>) {
        val eksisterendeHistoriskeFnr: Set<Fnr> = alleFnr.filter { !it.gjeldende }.toSet()

        fnrListe.forEach {
            if (!eksisterendeHistoriskeFnr.contains(Fnr(fnr = it))) {
                alleFnr.add(Fnr(fnr = it, gjeldende = false, person = this))
            }
        }
    }
}
