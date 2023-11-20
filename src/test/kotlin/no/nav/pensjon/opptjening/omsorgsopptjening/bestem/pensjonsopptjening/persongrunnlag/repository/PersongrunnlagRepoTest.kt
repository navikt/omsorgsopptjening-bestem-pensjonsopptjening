package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.Rådata
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Month
import java.time.YearMonth

class PersongrunnlagRepoTest : SpringContextTest.NoKafka() {

    @Autowired
    private lateinit var persongrunnlagRepo: PersongrunnlagRepo

    @Test
    fun `kan lagre en persongrunnlagmelding`() {
        val innlesingId = InnlesingId.generate()
        val correlationId = CorrelationId.generate()
        val persongrunnlag = persongrunnlag(innlesingId, correlationId)
        persongrunnlagRepo.persist(persongrunnlag)
    }

    @Test
    fun `lagrer ikke den samme meldingen flere ganger`() {
        val innlesingId = InnlesingId.generate()
        val correlationId = CorrelationId.generate()
        val persongrunnlag = persongrunnlag(innlesingId, correlationId)
        val pg1 = persongrunnlagRepo.persist(persongrunnlag)
        val pg2 = persongrunnlagRepo.persist(persongrunnlag)
        assertThat(pg1.id).isEqualTo(pg2.id)
    }

    private fun persongrunnlag(innlesingId: InnlesingId, correlationId: CorrelationId) =
        no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMelding.Lest(
            innhold = PersongrunnlagMelding(
                omsorgsyter = "12345678910",
                persongrunnlag = listOf(
                    PersongrunnlagMelding.Persongrunnlag(
                        omsorgsyter = "12345678910",
                        omsorgsperioder = listOf(
                            PersongrunnlagMelding.Omsorgsperiode(
                                fom = YearMonth.of(2018, Month.JANUARY),
                                tom = YearMonth.of(2030, Month.DECEMBER),
                                omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                omsorgsmottaker = "03041212345",
                                kilde = Kilde.BARNETRYGD,
                                utbetalt = 7234,
                                landstilknytning = Landstilknytning.NORGE
                            ),
                        ),
                        hjelpestønadsperioder = listOf(
                            PersongrunnlagMelding.Hjelpestønadperiode(
                                fom = YearMonth.of(2018, Month.JANUARY),
                                tom = YearMonth.of(2030, Month.DECEMBER),
                                omsorgstype = Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_3,
                                omsorgsmottaker = "03041212345",
                                kilde = Kilde.BARNETRYGD,
                            )
                        )
                    ),
                ),
                rådata = Rådata(),
                innlesingId = innlesingId,
                correlationId = correlationId,
            )
        )
}