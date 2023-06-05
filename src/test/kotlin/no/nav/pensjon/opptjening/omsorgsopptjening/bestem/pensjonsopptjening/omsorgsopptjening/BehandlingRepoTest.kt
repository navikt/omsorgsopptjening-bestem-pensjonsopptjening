package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.berik
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Behandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførtBehandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.toDb
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgVedtakPeriode
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsSak
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.Omsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.PersonMedFødselsår
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.mapToJson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.springframework.beans.factory.annotation.Autowired
import java.time.Month
import java.time.YearMonth
import java.util.UUID

class BehandlingRepoTest : SpringContextTest() {

    @Autowired
    private lateinit var repo: BehandlingRepo

    @Test
    fun `store and retrieve behandling`() {
        val behandling = Behandling(
            grunnlag = OmsorgsGrunnlag(
                omsorgsyter = "12345678910",
                omsorgsAr = 2022,
                omsorgstype = Omsorgstype.BARNETRYGD,
                kjoreHash = UUID.randomUUID().toString(),
                kilde = Kilde.BARNETRYGD,
                omsorgsSaker = listOf(
                    OmsorgsSak(
                        omsorgsyter = "12345678910",
                        omsorgVedtakPeriode = listOf(
                            OmsorgVedtakPeriode(
                                fom = YearMonth.of(2022, Month.JANUARY),
                                tom = YearMonth.of(2022, Month.DECEMBER),
                                prosent = 100,
                                omsorgsmottaker = "01018012345"
                            )
                        )
                    )
                )
            ).berik(
                persondata = setOf(
                    PersonMedFødselsår(
                        fnr = "12345678910",
                        fodselsAr = 1980
                    ),
                    PersonMedFødselsår(
                        fnr = "01018012345",
                        fodselsAr = 2020
                    ),
                )
            )
        )
        assertInstanceOf(FullførtBehandling::class.java, repo.persist(behandling))
        assertEquals(1, repo.findAll("12345678910").count())
    }

    @Test
    fun `serialization and deserialization innvilget`() {
        val behandling = Behandling(
            grunnlag = OmsorgsGrunnlag(
                omsorgsyter = "01018012345",
                omsorgsAr = 2022,
                omsorgstype = Omsorgstype.BARNETRYGD,
                kjoreHash = "xxx",
                kilde = Kilde.BARNETRYGD,
                omsorgsSaker = listOf(
                    OmsorgsSak(
                        omsorgsyter = "01018012345",
                        omsorgVedtakPeriode = listOf(
                            OmsorgVedtakPeriode(
                                fom = YearMonth.of(2022, Month.JANUARY),
                                tom = YearMonth.of(2022, Month.DECEMBER),
                                prosent = 100,
                                omsorgsmottaker = "01012012345"
                            ),
                            OmsorgVedtakPeriode(
                                fom = YearMonth.of(2022, Month.JANUARY),
                                tom = YearMonth.of(2022, Month.MAY),
                                prosent = 100,
                                omsorgsmottaker = "02022012345"
                            )
                        )
                    )
                )
            ).berik(
                persondata = setOf(
                    PersonMedFødselsår(
                        fnr = "01018012345",
                        fodselsAr = 1980
                    ),
                    PersonMedFødselsår(
                        fnr = "01012012345",
                        fodselsAr = 2020
                    ),
                    PersonMedFødselsår(
                        fnr = "02022012345",
                        fodselsAr = 2020
                    ),
                )
            )
        )

        behandling.toDb().mapToJson().let {
            JSONAssert.assertEquals(
                """
                {
                  "id": null,
                  "omsorgsAr": 2022,
                  "omsorgsyter": "01018012345",
                  "omsorgstype": "BARNETRYGD",
                  "grunnlag": {
                    "omsorgsyter": {
                      "fnr": "01018012345",
                      "fødselsår": 1980
                    },
                    "omsorgsAr": 2022,
                    "omsorgstype": "BARNETRYGD",
                    "kjoreHash": "xxx",
                    "kilde": "BARNETRYGD",
                    "omsorgsSaker": [
                      {
                        "omsorgsyter": {
                          "fnr": "01018012345",
                          "fødselsår": 1980
                        },
                        "omsorgVedtakPeriode": [
                          {
                            "fom": "2022-01",
                            "tom": "2022-12",
                            "prosent": 100,
                            "omsorgsmottaker": {
                              "fnr": "01012012345",
                              "fødselsår": 2020
                            }
                          },
                          {
                            "fom": "2022-01",
                            "tom": "2022-05",
                            "prosent": 100,
                            "omsorgsmottaker": {
                              "fnr": "02022012345",
                              "fødselsår": 2020
                            }
                          }
                        ]
                      }
                    ],
                    "originaltGrunnlag": "{\"omsorgsyter\":\"01018012345\",\"omsorgsAr\":2022,\"omsorgstype\":\"BARNETRYGD\",\"kjoreHash\":\"xxx\",\"kilde\":\"BARNETRYGD\",\"omsorgsSaker\":[{\"omsorgsyter\":\"01018012345\",\"omsorgVedtakPeriode\":[{\"fom\":\"2022-01\",\"tom\":\"2022-12\",\"prosent\":100,\"omsorgsmottaker\":\"01012012345\",\"periode\":{}},{\"fom\":\"2022-01\",\"tom\":\"2022-05\",\"prosent\":100,\"omsorgsmottaker\":\"02022012345\",\"periode\":{}}]}]}"
                  },
                  "vilkårsvurdering": {
                    "type": "Og",
                    "og": [
                      {
                        "type": "OmsorgsyterOver16Ar",
                        "vilkar": "Det kan gis pensjonsopptjening etter første ledd fra og med det året vedkommende fyller 17 år.",
                        "grunnlag": {
                          "type": "OmsorgsyterOgOmsorgsÅr",
                          "omsorgsyter": {
                            "fnr": "01018012345",
                            "fødselsår": 1980
                          },
                          "omsorgsAr": 2022
                        },
                        "utfall": {
                          "type": "OmsorgsyterOver16ArInnvilget",
                          "årsak": "Medlemmet er over 16 år."
                        }
                      },
                      {
                        "type": "OmsorgsyterUnder70Ar",
                        "vilkar": "Det kan gis pensjonsopptjening etter første ledd til og med det året vedkommende fyller 69 år.",
                        "grunnlag": {
                          "type": "OmsorgsyterOgOmsorgsÅr",
                          "omsorgsyter": {
                            "fnr": "01018012345",
                            "fødselsår": 1980
                          },
                          "omsorgsAr": 2022
                        },
                        "utfall": {
                          "type": "OmsorgsyterUnder70ArInnvilget",
                          "årsak": "Medlemmet er under 70 år."
                        }
                      },
                      {
                        "type": "Eller",
                        "eller": [
                          {
                            "type": "FullOmsorgForBarnUnder6",
                            "vilkar": "Medlemmet har minst halve året hatt den daglige omsorgen for et barn",
                            "grunnlag": {
                              "type": "OmsorgForBarnUnder6",
                              "omsorgsAr": 2022,
                              "omsorgsmottaker": {
                                "fnr": "01012012345",
                                "fødselsår": 2020
                              },
                              "antallMånederFullOmsorg": 12
                            },
                            "utfall": {
                              "type": "FullOmsorgForBarnUnder6Innvilget",
                              "årsak": "Medlemmet har et halve år med daglig omsorgen for et barn",
                              "omsorgsmottaker": {
                                "fnr": "01012012345",
                                "fødselsår": 2020
                              }
                            }
                          },
                          {
                            "type": "FullOmsorgForBarnUnder6",
                            "vilkar": "Medlemmet har minst halve året hatt den daglige omsorgen for et barn",
                            "grunnlag": {
                              "type": "OmsorgForBarnUnder6",
                              "omsorgsAr": 2022,
                              "omsorgsmottaker": {
                                "fnr": "02022012345",
                                "fødselsår": 2020
                              },
                              "antallMånederFullOmsorg": 5
                            },
                            "utfall": {
                              "type": "FullOmsorgForBarnUnder6Avslag",
                              "årsaker": [
                                "MINDRE_ENN_6_MND_FULL_OMSORG"
                              ]
                            }
                          }
                        ],
                        "utfall": {
                          "type": "EllerInnvilget",
                          "årsak": "Et av vilkårene var sanne."
                        }
                      }
                    ],
                    "utfall": {
                      "type": "OgInnvilget",
                      "årsak": "Alle vilkår var sanne."
                    }
                  },
                  "utfall": {
                    "type": "AutomatiskGodskrivingInnvilget",
                    "årsak": "",
                    "omsorgsmottaker": {
                      "fnr": "01012012345",
                      "fødselsår": 2020
                    }
                  }
                }
            """.trimIndent(), it, true
            )
        }
    }

    @Test
    fun `serialization and deserialization avslag`() {
        val behandling = Behandling(
            grunnlag = OmsorgsGrunnlag(
                omsorgsyter = "01018012345",
                omsorgsAr = 2022,
                omsorgstype = Omsorgstype.BARNETRYGD,
                kjoreHash = "xxx",
                kilde = Kilde.BARNETRYGD,
                omsorgsSaker = listOf(
                    OmsorgsSak(
                        omsorgsyter = "01018012345",
                        omsorgVedtakPeriode = listOf(
                            OmsorgVedtakPeriode(
                                fom = YearMonth.of(2022, Month.JANUARY),
                                tom = YearMonth.of(2022, Month.FEBRUARY),
                                prosent = 100,
                                omsorgsmottaker = "01011012345"
                            ),
                            OmsorgVedtakPeriode(
                                fom = YearMonth.of(2022, Month.JANUARY),
                                tom = YearMonth.of(2022, Month.MAY),
                                prosent = 100,
                                omsorgsmottaker = "02022012345"
                            )
                        )
                    )
                )
            ).berik(
                persondata = setOf(
                    PersonMedFødselsår(
                        fnr = "01018012345",
                        fodselsAr = 1980
                    ),
                    PersonMedFødselsår(
                        fnr = "01011012345",
                        fodselsAr = 2010
                    ),
                    PersonMedFødselsår(
                        fnr = "02022012345",
                        fodselsAr = 2020
                    ),
                )
            )
        )

        behandling.toDb().mapToJson().let {
            JSONAssert.assertEquals(
                """
                {
                  "id": null,
                  "omsorgsAr": 2022,
                  "omsorgsyter": "01018012345",
                  "omsorgstype": "BARNETRYGD",
                  "grunnlag": {
                    "omsorgsyter": {
                      "fnr": "01018012345",
                      "fødselsår": 1980
                    },
                    "omsorgsAr": 2022,
                    "omsorgstype": "BARNETRYGD",
                    "kjoreHash": "xxx",
                    "kilde": "BARNETRYGD",
                    "omsorgsSaker": [
                      {
                        "omsorgsyter": {
                          "fnr": "01018012345",
                          "fødselsår": 1980
                        },
                        "omsorgVedtakPeriode": [
                          {
                            "fom": "2022-01",
                            "tom": "2022-02",
                            "prosent": 100,
                            "omsorgsmottaker": {
                              "fnr": "01011012345",
                              "fødselsår": 2010
                            }
                          },
                          {
                            "fom": "2022-01",
                            "tom": "2022-05",
                            "prosent": 100,
                            "omsorgsmottaker": {
                              "fnr": "02022012345",
                              "fødselsår": 2020
                            }
                          }
                        ]
                      }
                    ],
                    "originaltGrunnlag": "{\"omsorgsyter\":\"01018012345\",\"omsorgsAr\":2022,\"omsorgstype\":\"BARNETRYGD\",\"kjoreHash\":\"xxx\",\"kilde\":\"BARNETRYGD\",\"omsorgsSaker\":[{\"omsorgsyter\":\"01018012345\",\"omsorgVedtakPeriode\":[{\"fom\":\"2022-01\",\"tom\":\"2022-02\",\"prosent\":100,\"omsorgsmottaker\":\"01011012345\",\"periode\":{}},{\"fom\":\"2022-01\",\"tom\":\"2022-05\",\"prosent\":100,\"omsorgsmottaker\":\"02022012345\",\"periode\":{}}]}]}"
                  },
                  "vilkårsvurdering": {
                    "type": "Og",
                    "og": [
                      {
                        "type": "OmsorgsyterOver16Ar",
                        "vilkar": "Det kan gis pensjonsopptjening etter første ledd fra og med det året vedkommende fyller 17 år.",
                        "grunnlag": {
                          "type": "OmsorgsyterOgOmsorgsÅr",
                          "omsorgsyter": {
                            "fnr": "01018012345",
                            "fødselsår": 1980
                          },
                          "omsorgsAr": 2022
                        },
                        "utfall": {
                          "type": "OmsorgsyterOver16ArInnvilget",
                          "årsak": "Medlemmet er over 16 år."
                        }
                      },
                      {
                        "type": "OmsorgsyterUnder70Ar",
                        "vilkar": "Det kan gis pensjonsopptjening etter første ledd til og med det året vedkommende fyller 69 år.",
                        "grunnlag": {
                          "type": "OmsorgsyterOgOmsorgsÅr",
                          "omsorgsyter": {
                            "fnr": "01018012345",
                            "fødselsår": 1980
                          },
                          "omsorgsAr": 2022
                        },
                        "utfall": {
                          "type": "OmsorgsyterUnder70ArInnvilget",
                          "årsak": "Medlemmet er under 70 år."
                        }
                      },
                      {
                        "type": "Eller",
                        "eller": [
                          {
                            "type": "FullOmsorgForBarnUnder6",
                            "vilkar": "Medlemmet har minst halve året hatt den daglige omsorgen for et barn",
                            "grunnlag": {
                              "type": "OmsorgForBarnUnder6",
                              "omsorgsAr": 2022,
                              "omsorgsmottaker": {
                                "fnr": "01011012345",
                                "fødselsår": 2010
                              },
                              "antallMånederFullOmsorg": 2
                            },
                            "utfall": {
                              "type": "FullOmsorgForBarnUnder6Avslag",
                              "årsaker": [
                                "MINDRE_ENN_6_MND_FULL_OMSORG",
                                "BARN_IKKE_MELLOM_1_OG_5"
                              ]
                            }
                          },
                          {
                            "type": "FullOmsorgForBarnUnder6",
                            "vilkar": "Medlemmet har minst halve året hatt den daglige omsorgen for et barn",
                            "grunnlag": {
                              "type": "OmsorgForBarnUnder6",
                              "omsorgsAr": 2022,
                              "omsorgsmottaker": {
                                "fnr": "02022012345",
                                "fødselsår": 2020
                              },
                              "antallMånederFullOmsorg": 5
                            },
                            "utfall": {
                              "type": "FullOmsorgForBarnUnder6Avslag",
                              "årsaker": [
                                "MINDRE_ENN_6_MND_FULL_OMSORG"
                              ]
                            }
                          }
                        ],
                        "utfall": {
                          "type": "EllerAvslått",
                          "årsaker": [
                            "MINST_ET_VILKÅR_MÅ_VÆRE_OPPFYLT"
                          ]
                        }
                      }
                    ],
                    "utfall": {
                      "type": "OgAvslått",
                      "årsaker": [
                        "ALLE_VILKÅR_MÅ_VÆRE_OPPFYLT"
                      ]
                    }
                  },
                  "utfall": {
                    "type": "AutomatiskGodskrivingAvslag",
                    "årsaker": [
                      "MINDRE_ENN_6_MND_FULL_OMSORG",
                      "BARN_IKKE_MELLOM_1_OG_5",
                      "MINDRE_ENN_6_MND_FULL_OMSORG"
                    ]
                  }
                }
            """.trimIndent(), it, true
            )
        }
    }
}