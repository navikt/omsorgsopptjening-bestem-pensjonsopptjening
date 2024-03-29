package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.time.Month

internal class PersonOppslagTest : SpringContextTest.NoKafka() {

    @Autowired
    lateinit var personOppslag: PersonOppslag

    companion object {
        const val FNR = "11111111111"

        @JvmField
        @RegisterExtension
        val wiremock = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().port(WIREMOCK_PORT))
            .build()!!
    }

    @Test
    fun `Et fnr i bruk - Ett fnr i person`() {
        Mdc.scopedMdc(CorrelationId.generate()) {
            Mdc.scopedMdc(InnlesingId.generate()) {
                wiremock.stubFor(
                    WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(
                        WireMock.aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBodyFile("fnr_1bruk.json")
                    )
                )
                val person: Person = personOppslag.hentPerson(FNR)
                assertEquals("12345678910", person.fnr)
            }
        }
    }

    @Test
    fun `Samme historiske fnr som gjeldende - et fnr i person`() {
        Mdc.scopedMdc(CorrelationId.generate()) {
            Mdc.scopedMdc(InnlesingId.generate()) {
                wiremock.stubFor(
                    WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(
                        WireMock.aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBodyFile("fnr_samme_fnr_gjeldende_og_historisk.json")
                    )
                )
                val person: Person = personOppslag.hentPerson(FNR)
                assertEquals("04010012797", person.fnr)
            }
        }
    }


    @Test
    fun `Et fnr 1 OPPHOERT - kast exception`() {
        Mdc.scopedMdc(CorrelationId.generate()) {
            Mdc.scopedMdc(InnlesingId.generate()) {
                wiremock.stubFor(
                    WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(
                        WireMock.aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBodyFile("fnr_1opphort.json")
                    )
                )
                assertThrows<PersonOppslagException> { personOppslag.hentPerson(FNR) }
            }
        }
    }

    @Test
    fun `Et fnr 0 OPPHOERT 0 BRUK - kast exception`() {
        Mdc.scopedMdc(CorrelationId.generate()) {
            Mdc.scopedMdc(InnlesingId.generate()) {
                wiremock.stubFor(
                    WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(
                        WireMock.aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBodyFile("fnr_0bruk_0opphort.json")
                    )
                )
                assertThrows<PersonOppslagException> { personOppslag.hentPerson(FNR) }
            }
        }
    }


    @Test
    fun `Ingen foedsel - ingen velges`() {
        Mdc.scopedMdc(CorrelationId.generate()) {
            Mdc.scopedMdc(InnlesingId.generate()) {
                wiremock.stubFor(
                    WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(
                        WireMock.aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBodyFile("fodsel_0freg_0pdl.json")
                    )
                )

                assertThrows<PersonOppslagException> { personOppslag.hentPerson(FNR) }
            }
        }
    }

    @Test
    fun `Et foedsel fra freg - det velges`() {
        Mdc.scopedMdc(CorrelationId.generate()) {
            Mdc.scopedMdc(InnlesingId.generate()) {
                wiremock.stubFor(
                    WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(
                        WireMock.aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBodyFile("fodsel_1freg_0pdl.json")
                    )
                )
                val person: Person = personOppslag.hentPerson(FNR)
                assertEquals(LocalDate.of(1992, Month.JANUARY, 1), person.fødselsdato)
            }
        }
    }

    @Test
    fun `To foedsel - begge fra pdl - det med nyeste endring velges`() {
        Mdc.scopedMdc(CorrelationId.generate()) {
            Mdc.scopedMdc(InnlesingId.generate()) {
                wiremock.stubFor(
                    WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(
                        WireMock.aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBodyFile("fodsel_0freg_2pdl.json")
                    )
                )
                val person = personOppslag.hentPerson(FNR)
                assertEquals(LocalDate.of(1990, Month.JANUARY, 1), person.fødselsdato)
            }
        }
    }

    @Test
    fun `Tre foedsel - en fra Freg - to fra PDL - kronologisk PDL, Freg, PDL - den fra Freg velges pga foretrukket informasjonsskilde`() {
        Mdc.scopedMdc(CorrelationId.generate()) {
            Mdc.scopedMdc(InnlesingId.generate()) {
                wiremock.stubFor(
                    WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(
                        WireMock.aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBodyFile("fodsel_1freg_2pdl.json")
                    )
                )
                val person = personOppslag.hentPerson(FNR)
                assertEquals(LocalDate.of(1998, Month.JANUARY, 1), person.fødselsdato)
            }
        }
    }

    @Test
    fun `Tre foedsel - to fra Freg - en fra PDL - kronologisk PDL, Freg, Freg - det siste fra Freg velges`() {
        Mdc.scopedMdc(CorrelationId.generate()) {
            Mdc.scopedMdc(InnlesingId.generate()) {
                wiremock.stubFor(
                    WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(
                        WireMock.aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBodyFile("fodsel_2freg_1pdl.json")
                    )
                )
                val person = personOppslag.hentPerson(FNR)
                assertEquals(LocalDate.of(1995, Month.JANUARY, 1), person.fødselsdato)
            }
        }
    }
}
