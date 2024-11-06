package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

sealed class JuridiskHenvisning(
    open val kortTittel: String? = null,
    open val dato: String? = null,
    open val kapittel: Int?,
    open val paragraf: Int?,
    open val ledd: Int?,
    open val bokstav: String? = null,
    open val punktum: Int? = null,
    open val tekst: String?
) {

    data class Arkivert(
        override val kortTittel: String? = null,
        override val dato: String? = null,
        override val kapittel: Int?,
        override val paragraf: Int?,
        override val ledd: Int?,
        override val bokstav: String? = null,
        override val punktum: Int? = null,
        override val tekst: String?
    ) : JuridiskHenvisning(
        kortTittel = kortTittel,
        dato = dato,
        kapittel = kapittel,
        paragraf = paragraf,
        ledd = ledd,
        bokstav = bokstav,
        punktum = punktum,
        tekst = tekst
    )

    data object Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_a_Første_Punktum : JuridiskHenvisning(
        kortTittel = "Folketrygdloven – ftrl",
        dato = "LOV-1997-02-28-19",
        kapittel = 20,
        paragraf = 8,
        ledd = 1,
        bokstav = null,
        punktum = null,
        tekst = "Medlemmet har minst halve året hatt den daglige omsorgen for et barn som ikke har fylt seks år innen årets utgang."
    )

    data object Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Innledning : JuridiskHenvisning(
        kortTittel = "Folketrygdloven – ftrl",
        dato = "LOV-1997-02-28-19",
        kapittel = 20,
        paragraf = 8,
        ledd = 1,
        bokstav = "a",
        punktum = 1,
        tekst = """
                Et medlem som har utført omsorgsarbeid, får hvert kalenderår en pensjonsopptjening tilsvarende 18,1 prosent av et beløp på 4,5 ganger grunnbeløpet i følgende tilfeller
        """.trimIndent()
    )


    data object Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_a_Andre_Punktum : JuridiskHenvisning(
        kortTittel = "Folketrygdloven – ftrl",
        dato = "LOV-1997-02-28-19",
        kapittel = 20,
        paragraf = 8,
        ledd = 1,
        bokstav = "a",
        punktum = 2,
        tekst = "For barnets fødselsår opptjenes det samme beløpet selv om omsorgen har vart mindre enn et halvt år."
    )

    data object Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_a_Tredje_Punktum : JuridiskHenvisning(
        kortTittel = "Folketrygdloven – ftrl",
        dato = "LOV-1997-02-28-19",
        kapittel = 20,
        paragraf = 8,
        ledd = 1,
        bokstav = "a",
        punktum = 3,
        tekst = "Opptjening gis den som mottar barnetrygd for barnet etter barnetrygdloven, dersom ikke noe annet er bestemt i forskrift."
    )

    data object Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_b_Første_Punktum : JuridiskHenvisning(
        kortTittel = "Folketrygdloven – ftrl",
        dato = "LOV-1997-02-28-19",
        kapittel = 20,
        paragraf = 8,
        ledd = 1,
        bokstav = "b",
        punktum = 1,
        tekst = "Medlemmet har minst halve året utført omsorgsarbeid for en syk, en funksjonshemmet eller en eldre person som selv er medlem i eller mottar pensjon fra folketrygden"
    )

    data object Folketrygdloven_Kap_20_Paragraf_8_Andre_Ledd : JuridiskHenvisning(
        kortTittel = "Folketrygdloven – ftrl",
        dato = "LOV-1997-02-28-19",
        kapittel = 20,
        paragraf = 8,
        ledd = 2,
        tekst = "Det kan gis pensjonsopptjening etter første ledd fra og med det året vedkommende fyller 17 år, til og med det året vedkommende fyller 69 år."
    )

    data object Folketrygdloven_Kap_6_Paragraf_5_Første_Ledd_Første_Punktum : JuridiskHenvisning(
        kortTittel = "Folketrygdloven – ftrl",
        dato = "LOV-1997-02-28-19",
        kapittel = 6,
        paragraf = 5,
        ledd = 1,
        punktum = 1,
        tekst = "Forhøyet hjelpestønad ytes til et medlem under 18 år som har vesentlig større behov for tilsyn, pleie og annen hjelp enn det som hjelpestønad etter § 6-4 dekker"
    )

    data object Forskrift_Om_Alderspensjon_I_Folketrygden_Kap_3_Paragraf_4_Første_Ledd_Første_Punktum :
        JuridiskHenvisning(
            kortTittel = "Forskrift om alderspensjon i folketrygden",
            dato = "FOR-2009-12-22-1810",
            kapittel = 3,
            paragraf = 4,
            ledd = 1,
            punktum = 1,
            tekst = "Omsorgsopptjening på grunnlag av omsorgen for ett barn eller for flere barn som bor sammen, gis bare én omsorgsyter for det enkelte kalenderår."
        )

    data object Forskrift_Om_Alderspensjon_I_Folketrygden_Kap_3_Paragraf_4_Andre_Ledd :
        JuridiskHenvisning(
            kortTittel = "Forskrift om alderspensjon i folketrygden",
            dato = "FOR-2009-12-22-1810",
            kapittel = 3,
            paragraf = 4,
            ledd = 2,
            punktum = 1,
            tekst = "En person som mottar barnetrygd for barnet alene gis omsorgsopptjening uten å sette fram krav om det."
        )

    data object Forskrift_Om_Alderspensjon_I_Folketrygden_Kap_3_Paragraf_4_Tredje_Ledd : JuridiskHenvisning(
        kortTittel = "Forskrift om alderspensjon i folketrygden",
        dato = "FOR-2009-12-22-1810",
        kapittel = 3,
        paragraf = 4,
        ledd = 3,
        tekst = """
            Når barnet eller barna bor skiftevis og tilnærmet like lenge hos mor og far, og begge omsorgsyterne hver måned får en del av barnetrygden eller får ytelsen utbetalt i tilnærmet lik lang periode av året, må omsorgsyterne sette fram krav om omsorgsopptjening med opplysning om hvem av omsorgsyterne som skal ha opptjeningen for kalenderåret
        """.trimIndent()
    )

    data object Forskrift_Om_Alderspensjon_I_Folketrygden_Kap_3_Paragraf_11_Første_Ledd : JuridiskHenvisning(
        kortTittel = "Forskrift om alderspensjon i folketrygden",
        dato = "FOR-2009-12-22-1810",
        kapittel = 3,
        paragraf = 11,
        ledd = 1,
        tekst = """
            Dersom den omsorgstrengende får forhøyet hjelpestønad etter de to høyeste satsene i henhold til folketrygdloven § 6-5, gis en av foreldrene omsorgsopptjening uten at det er nødvendig å sette fram krav.
        """.trimIndent()
    )

    data object Forskrift_Om_Alderspensjon_I_Folketrygden_Kap_3_Paragraf_11_Andre_Ledd_Første_Punktum :
        JuridiskHenvisning(
            kortTittel = "Forskrift om alderspensjon i folketrygden",
            dato = "FOR-2009-12-22-1810",
            kapittel = 3,
            paragraf = 11,
            ledd = 2,
            punktum = 1,
            tekst = """
            Omsorgsopptjening etter første ledd gis den av foreldrene som mottar barnetrygd for barnet.
        """.trimIndent()
        )

    data object Forskrift_Om_Alderspensjon_I_Folketrygden_Kap_3_Paragraf_11_Tredje_Ledd_Første_Punktum :
        JuridiskHenvisning(
            kortTittel = "Forskrift om alderspensjon i folketrygden",
            dato = "FOR-2009-12-22-1810",
            kapittel = 3,
            paragraf = 11,
            ledd = 3,
            punktum = 1,
            tekst = """
            Omsorgsopptjening etter første og andre ledd gis til og med kalenderåret retten til forhøyet hjelpestønad faller bort.
        """.trimIndent()
        )
}

