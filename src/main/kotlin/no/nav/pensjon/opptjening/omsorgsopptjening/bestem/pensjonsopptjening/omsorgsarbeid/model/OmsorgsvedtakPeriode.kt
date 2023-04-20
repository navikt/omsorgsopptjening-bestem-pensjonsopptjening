package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model

import jakarta.persistence.*
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import java.time.YearMonth


@Entity
@Table(name = "OMSORGSVEDTAK_PERIODE")
class OmsorgsvedtakPeriode(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "OMSORGSVEDTAK_PERIODE_ID", nullable = false)
    var id: Long? = null,

    @Convert(converter = YearMonthDateConverter::class)
    @Column(name = "FOM", nullable = false)
    val fom: YearMonth,

    @Convert(converter = YearMonthDateConverter::class)
    @Column(name = "TOM", nullable = true)
    val tom: YearMonth,

    @Column(name = "PROSENT", nullable = false)
    val prosent: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "LANDSTILKNYTNING", nullable = false)
    val landstilknytning: Landstilknytning,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "OMSORGSYTER",
        joinColumns = [JoinColumn(
            name = "OMSORGSVEDTAK_PERIODE_ID",
            referencedColumnName = "OMSORGSVEDTAK_PERIODE_ID"
        )],
        inverseJoinColumns = [JoinColumn(name = "PERSON_ID", referencedColumnName = "PERSON_ID")]
    )
    val omsorgsytere: List<Person> = listOf(),

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "OMSORGSARBEIDSMOTTAKER",
        joinColumns = [JoinColumn(
            name = "OMSORGSVEDTAK_PERIODE_ID",
            referencedColumnName = "OMSORGSVEDTAK_PERIODE_ID"
        )],
        inverseJoinColumns = [JoinColumn(name = "PERSON_ID", referencedColumnName = "PERSON_ID")]
    )
    val omsorgsmottakere: List<Person> = listOf(),
)

fun List<OmsorgsvedtakPeriode>.getAntallUtbetalingMoneder(ar: Int) =
    (mergeAllePerioder() begrensTilAr ar).antallMoneder()

fun List<OmsorgsvedtakPeriode>.mergeAllePerioder(): Periode {
    val alleUtbetalingsPeriode = map { Periode(it.fom, it.tom) }
    return alleUtbetalingsPeriode.fold(initial = Periode()) { accPeriod, newPeriode -> accPeriod + newPeriode }
}
