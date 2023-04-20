package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*

@Entity
@Table(name = "OMSORGS_SAK")
data class OmsorgsSak(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "OMSORGS_SAK_ID", nullable = false)
    val id: Long? = null,

    @OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @JoinColumn(
        name = "OMSORGS_SAK_ID",
        nullable = false,
        referencedColumnName = "OMSORGS_SAK_ID"
    )
    @JsonIgnore
    val omsorgsvedtakPerioder: List<OmsorgsvedtakPeriode>,
)
