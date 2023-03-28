package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.repository

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidSnapshot
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository


@Component
class OmsorgsarbeidSnapshotRepository(
    private val jpaRepository: OmsorgsarbeidSnapshotJpaRepository,
) {

    @Autowired
    private lateinit var em: EntityManager

    @Transactional
    fun save(omsorgsarbeidSnapshot: OmsorgsarbeidSnapshot): OmsorgsarbeidSnapshot {
        return em.merge(omsorgsarbeidSnapshot)
    }

    fun findByPerson(vararg persons: Person): List<OmsorgsarbeidSnapshot> {
        return jpaRepository.findByOmsorgsyter_idIn(persons.mapNotNull { it.id })
    }
}

@Repository
interface OmsorgsarbeidSnapshotJpaRepository : JpaRepository<OmsorgsarbeidSnapshot, Long> {

    fun findByOmsorgsyter_idIn(personIdList: List<Long>): List<OmsorgsarbeidSnapshot>
}