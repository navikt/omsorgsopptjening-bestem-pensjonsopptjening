package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.medlemskap.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.medlemskap.MedlemskapOppslag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.medlemskap.metrics.MedlemskapMetrikker
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Medlemskap
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.sql.ResultSet

@Component
internal class MedlemskapRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val medlemskapMetrikker: MedlemskapMetrikker,
) : MedlemskapOppslag {
    override fun hentMedlemskap(fnr: String): Medlemskap {
        return medlemskapMetrikker.mål {
            jdbcTemplate.query(
                """select pensjonstrygdet from medlemskap where fnr = :fnr""",
                mapOf<String, Any>(
                    "fnr" to fnr
                ),
                MedlemskapMapper()
            ).singleOrNull()
        } ?: Medlemskap.Ukjent(kilde = DomainKilde.INFOTRYGD_UTTREKK_PENSJONSTRYGDET)

    }

    internal class MedlemskapMapper : RowMapper<Medlemskap> {
        override fun mapRow(rs: ResultSet, rowNum: Int): Medlemskap {
            val pensjonstrygdet = rs.getString("pensjonstrygdet")

            return when {
                pensjonstrygdet.isBlank() || pensjonstrygdet.isEmpty() -> {
                    Medlemskap.Ukjent(kilde = DomainKilde.INFOTRYGD_UTTREKK_PENSJONSTRYGDET)
                }

                pensjonstrygdet == "N" -> {
                    Medlemskap.Ukjent(kilde = DomainKilde.INFOTRYGD_UTTREKK_PENSJONSTRYGDET)
                }

                pensjonstrygdet == "J" -> {
                    Medlemskap.Ukjent(kilde = DomainKilde.INFOTRYGD_UTTREKK_PENSJONSTRYGDET)
                }

                else -> {
                    throw RuntimeException("Ukjent verdi for pensjonstrygdet: $pensjonstrygdet")
                }
            }
        }
    }
}