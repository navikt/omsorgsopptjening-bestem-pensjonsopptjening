package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import jakarta.persistence.*
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person

@Entity
@Table(name = "OMSORGSOPPTJENINGSGRUNNLAG")
data class OmsorgsopptjeningsGrunnlag(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "OMSORGSOPPTJENINGSGRUNNLAG_ID", nullable = false)
    var id: Long? = null,

    @Column(name = "OMSORGS_AR", nullable = false)
    val omsorgsAr: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    val status: Status,

    @ManyToMany(fetch =  FetchType.EAGER)
    @JoinTable(
        name = "INVOLVERTE_PERSONER",
        joinColumns = [JoinColumn(
            name = "OMSORGSOPPTJENINGSGRUNNLAG_ID",
            referencedColumnName = "OMSORGSOPPTJENINGSGRUNNLAG_ID"
        )],
        inverseJoinColumns = [JoinColumn(name = "PERSON_ID", referencedColumnName = "PERSON_ID")]
    )
    val involvertePersoner: List<Person>
)