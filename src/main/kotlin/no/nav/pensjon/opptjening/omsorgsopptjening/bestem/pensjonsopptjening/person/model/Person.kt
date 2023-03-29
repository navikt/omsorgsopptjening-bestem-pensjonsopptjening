package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model

import jakarta.persistence.*
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.PdlFnr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.PdlPerson
import java.time.LocalDateTime

@Entity
@Table(name = "PERSON")
class Person(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "PERSON_ID", nullable = false)
    var id: Long? = null,
    @OneToMany(mappedBy = "person", fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    val alleFnr: MutableSet<Fnr> = mutableSetOf(),
    @Column(name = "FODSELSAR", nullable = false)
    var fodselsAr: Int,
    @Column(name = "TIMESTAMP", nullable = false)
    var timestamp: LocalDateTime = LocalDateTime.now()
) {

    val gjeldendeFnr get() = alleFnr.first { it.gjeldende }

    val historiskeFnr get() = alleFnr.filter { !it.gjeldende }

    infix fun erSammePerson(annenPerson: Person) = (annenPerson.alleFnr intersect alleFnr).isNotEmpty()

    infix fun identifiseresAv(fnr: Fnr) = alleFnr.contains(fnr)

    infix fun identifiseresAv(fnr: String) = alleFnr.map { it.fnr }.contains(fnr)

    fun oppdaterPerson(pdlPerson: PdlPerson) {
        if (fodselsAr != pdlPerson.fodselsAr) fodselsAr = pdlPerson.fodselsAr
        pdlPerson.alleFnr().forEach { oppdaterFnr(it) }
        alleFnr.removeIf { !pdlPerson.alleFnr().any { pdlFnr -> it.fnr == pdlFnr.fnr } }
    }

    private fun oppdaterFnr(pdlFnr: PdlFnr): Any =
        if (identifiseresAv(pdlFnr.fnr)) {
            alleFnr.first { it.fnr == pdlFnr.fnr }.apply { gjeldende = pdlFnr.gjeldende }
        } else {
            alleFnr.add(Fnr(fnr = pdlFnr.fnr, gjeldende = pdlFnr.gjeldende, person = this))
        }
}
