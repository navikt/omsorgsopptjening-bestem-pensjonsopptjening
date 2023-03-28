package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningsGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository


@Component
class OmsorgsopptjeningsGrunnlagRepository(private val jpaRepository: OmsorgsopptjeningsGrunnlagJpaRepository) {

    @Autowired
    private lateinit var em: EntityManager

    @Transactional
    fun save(omsorgsopptjeningsGrunnlag: OmsorgsopptjeningsGrunnlag): OmsorgsopptjeningsGrunnlag {
        findBy(
            personer = omsorgsopptjeningsGrunnlag.involvertePersoner,
            omsorgsAr = omsorgsopptjeningsGrunnlag.omsorgsAr
        ).forEach { it.historisk = true }

        return em.merge(omsorgsopptjeningsGrunnlag)
    }

    fun findBy(personer: List<Person>, omsorgsAr: Int, historisk: Boolean = false): List<OmsorgsopptjeningsGrunnlag> {
        return jpaRepository.findByInvolvertePersoner_idInAndHistoriskAndOmsorgsAr(
            personer = personer.mapNotNull { it.id },
            ar = omsorgsAr,
            historisk = historisk
        )
    }
}

@Repository
interface OmsorgsopptjeningsGrunnlagJpaRepository : JpaRepository<OmsorgsopptjeningsGrunnlag, Long> {

    fun findByInvolvertePersoner_idInAndHistoriskAndOmsorgsAr(
        personer: List<Long>,
        historisk: Boolean,
        ar: Int
    ): List<OmsorgsopptjeningsGrunnlag>

}

