package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model

import jakarta.persistence.*
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.UtbetalingMoneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import java.time.YearMonth


@Entity
@Table(name = "OMSORGSARBEID_PERIODE")
class OmsorgsarbeidPeriode(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "OMSORGSARBEID_PERIODE_ID", nullable = false)
    var id: Long? = null,

    @Convert(converter = YearMonthDateConverter::class)
    @Column(name = "FOM", nullable = false)
    val fom: YearMonth,

    @Convert(converter = YearMonthDateConverter::class)
    @Column(name = "TOM", nullable = true)
    val tom: YearMonth,

    @Column(name = "PROSENT", nullable = false)
    val prosent: Int,

    @OneToOne(fetch =  FetchType.EAGER, cascade = [CascadeType.ALL])
    @JoinColumn(name = "OMSORGSYTER", nullable = false, referencedColumnName = "PERSON_ID")
    val omsorgsyter: Person,

    @ManyToMany(fetch =  FetchType.EAGER, cascade = [CascadeType.ALL])
    @JoinTable(
        name = "OMSORGSARBEIDSMOTTAKER",
        joinColumns = [JoinColumn(name = "OMSORGSARBEID_PERIODE_ID", referencedColumnName = "OMSORGSARBEID_PERIODE_ID")],
        inverseJoinColumns = [JoinColumn(name = "PERSON_ID", referencedColumnName = "PERSON_ID")]
    )
    val omsorgsmottakere: List<Person>,
)

fun List<OmsorgsarbeidPeriode>.getAntallUtbetalingMoneder(omsorgsAr: Int) = (getAlleUtbetalingMoneder() begrensTilAr omsorgsAr).antall()

fun List<OmsorgsarbeidPeriode>.getAlleUtbetalingMoneder(): UtbetalingMoneder {
    val alleUtbetalingsMoneder = map{ UtbetalingMoneder(it.fom, it.tom) }
    return alleUtbetalingsMoneder.fold(initial = UtbetalingMoneder()) { acc, moneder -> acc + moneder }
}
