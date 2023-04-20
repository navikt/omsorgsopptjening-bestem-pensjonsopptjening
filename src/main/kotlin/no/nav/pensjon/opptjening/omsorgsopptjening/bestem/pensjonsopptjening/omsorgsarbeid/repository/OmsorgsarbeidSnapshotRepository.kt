package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.repository

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsGrunnlag
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
    fun save(omsorgsGrunnlag: OmsorgsGrunnlag): OmsorgsGrunnlag {
        find(omsorgsGrunnlag.omsorgsyter, omsorgsGrunnlag.omsorgsAr).forEach { it.historisk = true }

        return em.merge(omsorgsGrunnlag)
    }

    fun find(omsorgsyter: Person, omsorgsAr: Int, historisk: Boolean = false) =
        jpaRepository.findByOmsorgsyter_idInAndOmsorgsArAndHistorisk(
            personIdList = omsorgsyter.id?.let { listOf(it) } ?: listOf(),
            omsorgsAr = omsorgsAr,
            historisk = historisk
        )

    fun findByPerson(vararg persons: Person): List<OmsorgsGrunnlag> {
        return jpaRepository.findByOmsorgsyter_idIn(persons.mapNotNull { it.id })
    }
}

@Repository
interface OmsorgsarbeidSnapshotJpaRepository : JpaRepository<OmsorgsGrunnlag, Long> {

    fun findByOmsorgsyter_idInAndOmsorgsArAndHistorisk(
        personIdList: List<Long>,
        omsorgsAr: Int,
        historisk: Boolean
    ): List<OmsorgsGrunnlag>

    fun findByOmsorgsyter_idIn(personIdList: List<Long>): List<OmsorgsGrunnlag>
}