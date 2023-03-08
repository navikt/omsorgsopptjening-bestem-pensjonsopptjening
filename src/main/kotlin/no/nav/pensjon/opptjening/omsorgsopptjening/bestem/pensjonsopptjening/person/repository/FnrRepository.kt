package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Fnr
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

@Component
class FnrRepository(private val jpaRepository: FnrJpaRepository) {

    fun findPersonByFnr(fnr: String) = jpaRepository.findByFnr(fnr).firstOrNull()?.person

    fun findByFnrIn(fnrs: List<String>) = jpaRepository.findByFnrIn(fnrs)

    fun deleteByFnrIn(fnrs: List<Fnr>) = jpaRepository.deleteByFnrIn(fnrs.map { it.fnr!! })

}


@Repository
interface FnrJpaRepository : JpaRepository<Fnr, Long> {

    fun findByFnr(fnr: String): List<Fnr>

    fun findByFnrIn(fnrs: List<String>): List<Fnr>

    fun deleteByFnrIn(fnrs: List<String>): List<Fnr>

}
