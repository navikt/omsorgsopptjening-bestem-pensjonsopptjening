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
import org.mockito.BDDMockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.Clock
import java.time.Month
import java.time.YearMonth
import java.time.temporal.ChronoUnit

class PersongrunnlagRepoTest : SpringContextTest.NoKafka() {


    @Autowired
    private lateinit var persongrunnlagRepo: PersongrunnlagRepo

    @MockBean
    private lateinit var clock: Clock

    @Test
    fun `kan lagre en persongrunnlagmelding`() {
        val innlesingId = InnlesingId.generate()
        val correlationId = CorrelationId.generate()
        val persongrunnlag = persongrunnlag(innlesingId, correlationId)

        val id = persongrunnlagRepo.lagre(persongrunnlag)
        val peristert = persongrunnlagRepo.find(id!!)

        assertThat(peristert.correlationId).isEqualTo(correlationId)
        assertThat(peristert.innlesingId).isEqualTo(innlesingId)
        assertThat(peristert.innhold).isEqualTo(persongrunnlag.innhold)
    }

    @Test
    fun `lagrer ikke den samme meldingen flere ganger`() {
        val innlesingId = InnlesingId.generate()
        val correlationId = CorrelationId.generate()
        val persongrunnlag = persongrunnlag(innlesingId, correlationId)

        val pg1 = persongrunnlagRepo.lagre(persongrunnlag)
        val pg2 = persongrunnlagRepo.lagre(persongrunnlag)

        assertThat(pg1).isNotNull()
        assertThat(pg2).isNull()

        val peristert = persongrunnlagRepo.find(pg1!!)

        assertThat(peristert.correlationId).isEqualTo(correlationId)
        assertThat(peristert.innlesingId).isEqualTo(innlesingId)
        assertThat(peristert.innhold).isEqualTo(persongrunnlag.innhold)
    }

    @Test
    fun `gjenåpner låste persongrunnlagmeldinger etter en time`() {

        BDDMockito.given(clock.instant()).willReturn(
            Clock.systemUTC().instant(),
            Clock.systemUTC().instant(),
            Clock.systemUTC().instant().plus(2, ChronoUnit.HOURS), //karantene
        )

        val innlesingId = InnlesingId.generate()
        val correlationId = CorrelationId.generate()
        val persongrunnlag = persongrunnlag(innlesingId, correlationId)

        persongrunnlagRepo.lagre(persongrunnlag)

        val meldinger1 = persongrunnlagRepo.finnNesteMeldingerForBehandling(5)
        persongrunnlagRepo.frigiGamleLåser()
        val meldinger2 = persongrunnlagRepo.finnNesteMeldingerForBehandling(5)
        persongrunnlagRepo.frigiGamleLåser()
        val meldinger3 = persongrunnlagRepo.finnNesteMeldingerForBehandling(5)
        persongrunnlagRepo.frigi(meldinger3)

        assertThat(meldinger1.data).isNotEmpty()
        assertThat(meldinger2.data).isEmpty()
        assertThat(meldinger3.data).isNotEmpty
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