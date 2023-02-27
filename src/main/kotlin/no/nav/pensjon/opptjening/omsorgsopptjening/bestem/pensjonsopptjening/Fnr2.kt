package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "FNR")
class Fnr2(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "FNR_ID", nullable = false)
    var id: Long? = null,
    @Column(name = "FNR", nullable = false)
    var fnr: String? = null,
    @Column(name = "TIMESTAMP", nullable = false)
    var timestamp: LocalDateTime = LocalDateTime.now()
    )