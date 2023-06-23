package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

sealed class Referanse {
    abstract val henvisning: JuridiskHenvisning

    object MåHaMinstHalveÅretMedOmsorg : Referanse() {
        override val henvisning: JuridiskHenvisning =
            JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_a_Første_Punktum
    }

    object OpptjeningKanKunGodskrivesForEtBarnPerÅr : Referanse() {
        override val henvisning: JuridiskHenvisning =
            JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_a_Første_Punktum
    }

    object OmsorgsmottakerErIkkeFylt6FørUtgangAvOpptjeningsår : Referanse() {
        override val henvisning: JuridiskHenvisning =
            JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_a_Første_Punktum
    }

    object UnntakFraMinstHalvtÅrMedOmsorgForFødselår : Referanse() {
        override val henvisning: JuridiskHenvisning =
            JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_a_Andre_Punktum
    }

    object OmsorgsopptjeningGisTilMottakerAvBarnetrygd : Referanse() {
        override val henvisning: JuridiskHenvisning =
            JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_a_Tredje_Punktum
    }

    object OmsorgsopptjeningKanGodskrivesFraOgMedÅretManFyller17 : Referanse() {
        override val henvisning: JuridiskHenvisning = JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Andre_Ledd
    }

    object OmsorgsopptjeningKanGodskrivesFraOgMedÅretManFyller69 : Referanse() {
        override val henvisning: JuridiskHenvisning = JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Andre_Ledd
    }

    object OmsorgsopptjeningGisKunEnOmsorgsyterPerKalenderÅr : Referanse() {
        override val henvisning: JuridiskHenvisning =
            JuridiskHenvisning.Forskrift_Om_Alderspensjon_I_Folketrygden_Kap_3_Paragraf_4_Første_Ledd_Første_Punktum
    }

    object OmsorgsopptjeningKanTidligstGisPåfølgendeKalenderårHvisAlleredeInnvilgetAnnenOmsorgsyter : Referanse() {
        override val henvisning: JuridiskHenvisning =
            JuridiskHenvisning.Forskrift_Om_Alderspensjon_I_Folketrygden_Kap_3_Paragraf_4_Første_Ledd_Andre_Punktum
    }

    object OmsorgsopptjeningGisHvisOmsorgsyterHarFlestManeder : Referanse() {
        override val henvisning: JuridiskHenvisning =
            JuridiskHenvisning.Forskrift_Om_Alderspensjon_I_Folketrygden_Kap_3_Paragraf_4_Tredje_Ledd
    }
}

sealed class JuridiskHenvisning(
    val kortTittel: String? = null,
    val dato: String? = null,
    val kapittel: Int?,
    val paragraf: Int?,
    val ledd: Int?,
    val bokstav: String? = null,
    val punktum: Int? = null,
    val tekst: String?
) {

    class Arkivert(
        kortTittel: String?,
        dato: String?,
        kapittel: Int?,
        paragraf: Int?,
        ledd: Int?,
        bokstav: String? = null,
        punktum: Int? = null,
        tekst: String?
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

    object Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_a_Første_Punktum : JuridiskHenvisning(
        kortTittel = "Folketrygdloven – ftrl",
        dato = "LOV-1997-02-28-19",
        kapittel = 20,
        paragraf = 8,
        ledd = 1,
        bokstav = "a",
        punktum = 1,
        tekst = "Medlemmet har minst halve året hatt den daglige omsorgen for et barn som ikke har fylt seks år innen årets utgang."
    )

    object Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_a_Andre_Punktum : JuridiskHenvisning(
        kortTittel = "Folketrygdloven – ftrl",
        dato = "LOV-1997-02-28-19",
        kapittel = 20,
        paragraf = 8,
        ledd = 1,
        bokstav = "a",
        punktum = 2,
        tekst = "For barnets fødselsår opptjenes det samme beløpet selv om omsorgen har vart mindre enn et halvt år."
    )

    object Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_a_Tredje_Punktum : JuridiskHenvisning(
        kortTittel = "Folketrygdloven – ftrl",
        dato = "LOV-1997-02-28-19",
        kapittel = 20,
        paragraf = 8,
        ledd = 1,
        bokstav = "a",
        punktum = 3,
        tekst = "Opptjening gis den som mottar barnetrygd for barnet etter barnetrygdloven, dersom ikke noe annet er bestemt i forskrift."
    )

    object Folketrygdloven_Kap_20_Paragraf_8_Andre_Ledd : JuridiskHenvisning(
        kortTittel = "Folketrygdloven – ftrl",
        dato = "LOV-1997-02-28-19",
        kapittel = 20,
        paragraf = 8,
        ledd = 2,
        tekst = "Det kan gis pensjonsopptjening etter første ledd fra og med det året vedkommende fyller 17 år, til og med det året vedkommende fyller 69 år."
    )

    object Forskrift_Om_Alderspensjon_I_Folketrygden_Kap_3_Paragraf_4_Første_Ledd_Første_Punktum : JuridiskHenvisning(
        kortTittel = "Forskrift om alderspensjon i folketrygden",
        dato = "FOR-2009-12-22-1810",
        kapittel = 3,
        paragraf = 4,
        ledd = 1,
        punktum = 1,
        tekst = "Omsorgsopptjening på grunnlag av omsorgen for ett barn eller for flere barn som bor sammen, gis bare én omsorgsyter for det enkelte kalenderår."
    )

    object Forskrift_Om_Alderspensjon_I_Folketrygden_Kap_3_Paragraf_4_Første_Ledd_Andre_Punktum : JuridiskHenvisning(
        kortTittel = "Forskrift om alderspensjon i folketrygden",
        dato = "FOR-2009-12-22-1810",
        kapittel = 3,
        paragraf = 4,
        ledd = 1,
        punktum = 2,
        tekst = "Når en omsorgsyter gis omsorgsopptjening i et kalenderår, kan en annen omsorgsyter først gis opptjening for det påfølgende kalenderåret."
    )

    object Forskrift_Om_Alderspensjon_I_Folketrygden_Kap_3_Paragraf_4_Tredje_Ledd : JuridiskHenvisning(
        kortTittel = "Forskrift om alderspensjon i folketrygden",
        dato = "FOR-2009-12-22-1810",
        kapittel = 3,
        paragraf = 4,
        ledd = 3,
        tekst = """
            Når barnet eller barna bor skiftevis og tilnærmet like lenge hos mor og far, og begge omsorgsyterne hver måned får en del av barnetrygden eller får ytelsen utbetalt i tilnærmet lik lang periode av året, må omsorgsyterne sette fram krav om omsorgsopptjening med opplysning om hvem av omsorgsyterne som skal ha opptjeningen for kalenderåret
        """.trimIndent()
    );
}

