package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

sealed class Referanse {
    abstract val henvisning: Henvisning

    data class MåHaMinstHalveÅretMedOmsorg(
        override val henvisning: Henvisning = Lovparagraf.FTRL_K20_P8_L1_Ba_pkt1
    ) : Referanse()

    data class OpptjeningKanKunGodskrivesForEtBarnPerÅr(
        override val henvisning: Henvisning = Lovparagraf.FTRL_K20_P8_L1_Ba_pkt1
    ) : Referanse()

    data class OmsorgsmottakerErIkkeFylt6FørUtgangAvOpptjeningsår(
        override val henvisning: Henvisning = Lovparagraf.FTRL_K20_P8_L1_Ba_pkt1
    ) : Referanse()

    data class UnntakFraMinstHalvtÅrMedOmsorgForFødselår(
        override val henvisning: Henvisning = Lovparagraf.FTRL_K20_P8_L1_Ba_pkt2
    ) : Referanse()

    data class OmsorgsopptjeningGisTilMottakerAvBarnetrygd(
        override val henvisning: Henvisning = Lovparagraf.FTRL_K20_P8_L1_Ba_pkt3
    ) : Referanse()

    data class OmsorgsopptjeningKanGodskrivesFraOgMedÅretManFyller17(
        override val henvisning: Henvisning = Lovparagraf.FTRL_K20_P8_L2
    ) : Referanse()

    data class OmsorgsopptjeningKanGodskrivesFraOgMedÅretManFyller69(
        override val henvisning: Henvisning = Lovparagraf.FTRL_K20_P8_L2
    ) : Referanse()

    data class OmsorgsopptjeningGisKunEnOmsorgsyter(
        override val henvisning: Henvisning = Forskrift.FOR_OMSORGSPOENG_K3_P4_L1_pkt1
    ) : Referanse()
}

sealed class Henvisning {

}

sealed class Lovparagraf(
    val kortTittel: String,
    val kapittel: Int?,
    val paragraf: Int?,
    val ledd: Int?,
    val bokstav: String? = null,
    val punktum: Int? = null,
    val tekst: String?
) : Henvisning() {

    object FTRL_K20_P8_L1_Ba_pkt1 : Lovparagraf(
        kortTittel = "",
        kapittel = 20,
        paragraf = 8,
        ledd = 1,
        bokstav = "a",
        punktum = 1,
        tekst = "Medlemmet har minst halve året hatt den daglige omsorgen for et barn som ikke har fylt seks år innen årets utgang"
    )

    object FTRL_K20_P8_L1_Ba_pkt2 : Lovparagraf(
        kortTittel = "",
        kapittel = 20,
        paragraf = 8,
        ledd = 1,
        bokstav = "a",
        punktum = 2,
        tekst = "For barnets fødselsår opptjenes det samme beløpet selv om omsorgen har vart mindre enn et halvt år."
    )

    object FTRL_K20_P8_L1_Ba_pkt3 : Lovparagraf(
        kortTittel = "",
        kapittel = 20,
        paragraf = 8,
        ledd = 1,
        bokstav = "a",
        punktum = 3,
        tekst = "Opptjening gis den som mottar barnetrygd for barnet etter barnetrygdloven, dersom ikke noe annet er bestemt i forskrift"
    )

    object FTRL_K20_P8_L2 : Lovparagraf(
        kortTittel = "Forskrift om alderspensjon i folketrygden",
        kapittel = 20,
        paragraf = 8,
        ledd = 2,
        tekst = "Det kan gis pensjonsopptjening etter første ledd fra og med det året vedkommende fyller 17 år, til og med det året vedkommende fyller 69 år."
    )
}

sealed class Forskrift(
    val kortTittel: String,
    val kapittel: Int?,
    val paragraf: Int?,
    val ledd: Int?,
    val bokstav: String? = null,
    val punktum: Int? = null,
    val tekst: String?
) : Henvisning() {
    object FOR_OMSORGSPOENG_K3_P4_L1_pkt1 : Forskrift(
        kortTittel = "Forskrift om alderspensjon i folketrygden",
        kapittel = 20,
        paragraf = 8,
        ledd = 2,
        tekst = "Det kan gis pensjonsopptjening etter første ledd fra og med det året vedkommende fyller 17 år, til og med det året vedkommende fyller 69 år."
    )
}

