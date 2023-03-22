package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*

@Entity
@Table(name = "OMSORGSARBEID_SAK")
data class OmsorgsarbeidSak(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "OMSORGSARBEID_SAK_ID", nullable = false)
    var id: Long? = null,

    @OneToMany
    @JoinColumn(name = "OMSORGSARBEID_SAK_ID", referencedColumnName = "OMSORGSARBEID_SAK_ID")
    @JsonIgnore
    val omsorgsarbeidPerioder: List<OmsorgsarbeidPeriode>,
)
