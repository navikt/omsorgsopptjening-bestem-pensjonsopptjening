package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.external

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.serialize
import org.junit.jupiter.api.Test
import java.time.Year

class PENBrevSerializationTest {

    @Test
    fun testSerializeRegular() {
        val request = PENBrevClient.SendBrevRequest(omsorgsår = Year.of(2010), eksternReferanseId = "42")
        val json = serialize(request)
        println("2 $json")
    }
}