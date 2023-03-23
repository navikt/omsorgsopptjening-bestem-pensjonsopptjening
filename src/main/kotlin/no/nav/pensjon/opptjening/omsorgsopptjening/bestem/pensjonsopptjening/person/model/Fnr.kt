package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "FNR")
class Fnr(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "FNR_ID", nullable = false)
    var id: Long? = null,
    @Column(name = "FNR", nullable = false)
    val fnr: String,
    @Column(name = "GJELDENDE", nullable = false)
    var gjeldende: Boolean = false,
    @ManyToOne
    @JoinColumn(name = "PERSON_ID", referencedColumnName = "PERSON_ID")
    @JsonIgnore //TODO finn en bedre løsning
    val person: Person? = null,
    @Column(name = "TIMESTAMP", nullable = false)
    var timestamp: LocalDateTime = LocalDateTime.now()
) {

    override fun equals(other: Any?) = other === this || (other is Fnr && other.fnr == fnr)

    override fun hashCode() = fnr.hashCode()
}