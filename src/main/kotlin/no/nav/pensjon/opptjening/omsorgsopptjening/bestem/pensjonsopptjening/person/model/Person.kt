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
    @OneToMany(mappedBy = "person", fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    var alleFnr: MutableSet<Fnr> = mutableSetOf<Fnr>(),
    @Column(name = "FODSELSAR", nullable = false)
    var fodselsAr: Int? = null,
    @Column(name = "TIMESTAMP", nullable = false)
    var timestamp: LocalDateTime = LocalDateTime.now()
) {

    val gjeldendeFnr get() = alleFnr.first { it.gjeldende }

    val historiskeFnr get() = alleFnr.filter { !it.gjeldende }

    infix fun erSammePerson(annenPerson: Person) = (annenPerson.alleFnr intersect alleFnr).isNotEmpty()

    infix fun identifiseresAv(fnr: Fnr) = alleFnr.contains(fnr)

    /**
     * Legg til fnr dersom fnr ikke finnes i DB fra før
     **/
    fun oppdaterGjeldendeFnr(fnr: String) {
        val eksisterendeGjeldendeFnr: Fnr? = alleFnr.firstOrNull { it.gjeldende }
        if (eksisterendeGjeldendeFnr == null) {
            // Legger til gjeldende fnr på en person som ikke har gjeldende fnr fra før
            alleFnr.add(Fnr(fnr = fnr, gjeldende = true, person = this))
        } else {
            // Sletter nåværende gjeldende fnr før nytt gjeldende overtar
            alleFnr.remove(eksisterendeGjeldendeFnr)
            alleFnr.add(Fnr(fnr = fnr, gjeldende = true, person = this))
        }
    }

    /**
     * Dersom historisk fnr fra PDL ikke eksisterer i DB legges den til
     * Dersom historisk fnr eksisterer i DB men ikke i PDL fjernes den fra DB
     */
    fun oppdaterHistoriskeFnr(fnrListe: List<String>) {
        val eksisterendeHistoriskeFnr: Set<Fnr?> = alleFnr.filter { !it.gjeldende }.toSet()
        eksisterendeHistoriskeFnr.containsAll(fnrListe.map { Fnr(fnr = it) })

        fnrListe.forEach {
            if(!eksisterendeHistoriskeFnr.contains(Fnr(fnr = it))) {
                alleFnr.add(Fnr(fnr = it, gjeldende = false, person = this))
            }
        }
    }
}
