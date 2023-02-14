package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.OmsorgsopptjeningMockListener
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.OmsorgsarbeidListenerTest.Companion.OMSORGSOPPTJENING_TOPIC
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.KafkaIntegrationTestConfig
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka.OmsorgsarbeidListener
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka

@EmbeddedKafka(partitions = 1, topics = [OMSORGSOPPTJENING_TOPIC])
@SpringBootTest(classes = [App::class])
@Import(KafkaIntegrationTestConfig::class, OmsorgsopptjeningMockListener::class)
internal class OmsorgsarbeidListenerTest {

    @Autowired
    lateinit var embeddedKafka: EmbeddedKafkaBroker

    @Autowired
    lateinit var omsorgsArbeidListener: OmsorgsarbeidListener

    @Autowired
    lateinit var omsorgsarbeidProducer: KafkaTemplate<String, String>

    @Autowired
    lateinit var omsorgsopptjeingListener: OmsorgsopptjeningMockListener


    @Test
    fun `given omsorgsarbeid event then produce omsorgsopptjening event`() {
        omsorgsarbeidProducer.send(OMSORGSOPPTJENING_TOPIC, omsorgsMeldingKey(), omsorgsMeldingValue())

    }

    fun omsorgsMeldingKey(omsorgsyter: String = "12345678910", ar: String = "2020") =
        """
        {
            "omsorgsyterFnr": "12345678910",
            "omsorgsAr": "2005"
        }
        """.trimIndent()


    fun omsorgsMeldingValue(
        omsorgsyter: String = "12345678910",
        ar: String = "2020",
        hash: String = """2023-01-19T15:55:35.766223643"""
    ) =
        """{
              "omsorgsyter": {
                "fnr": "12345678910",
                "utbetalingsperioder": []
              },
              "omsorgsAr": "2005",
              "hash": "2023-01-20T10:35:23.15820754"
        }""".trimIndent()


    companion object {
        const val OMSORGSOPPTJENING_TOPIC = "omsorgsopptjening"
    }
}