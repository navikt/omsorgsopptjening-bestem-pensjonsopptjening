package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*

@Entity
@Table(name = "OMSORGSARBEID_SAK")
data class OmsorgsSak(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "OMSORGSARBEID_SAK_ID", nullable = false)
    val id: Long? = null,

    @OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @JoinColumn(
        name = "OMSORGSARBEID_SAK_ID",
        nullable = false,
        referencedColumnName = "OMSORGSARBEID_SAK_ID"
    )
    @JsonIgnore
    val omsorgVedtakPerioder: List<OmsorgVedtakPeriode>,
)
