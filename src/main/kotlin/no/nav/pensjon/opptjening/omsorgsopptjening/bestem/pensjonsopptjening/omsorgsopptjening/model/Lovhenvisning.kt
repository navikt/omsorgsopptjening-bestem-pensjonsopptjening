package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

enum class Lovhenvisning(
    val kortTittel: String,
    val kapittel: Int?,
    val paragraf: Int?,
    val ledd: Int?,
    val bokstav: String? = null,
    val punktum: Int? = null,
    val tekst: String?
) {
    MINST_HALVT_AR_OMSORG(
        kortTittel = "",
        kapittel = 20,
        paragraf = 8,
        bokstav = "a",
        ledd = 1,
        punktum = 1,
        tekst = "Medlemmet har minst halve året hatt den daglige omsorgen for et barn som ikke har fylt seks år innen årets utgang"
    ),
    KAN_KUN_GODSKRIVES_ET_BARN(
        kortTittel = "",
        kapittel = 20,
        paragraf = 8,
        bokstav = "a",
        ledd = 1,
        punktum = 1,
        tekst = "Medlemmet har minst halve året hatt den daglige omsorgen for et barn som ikke har fylt seks år innen årets utgang"
    ),
    OMSORGSMOTTAKER_IKKE_FYLT_6_AR(
        kortTittel = "",
        kapittel = 20,
        paragraf = 8,
        bokstav = "a",
        ledd = 1,
        punktum = 1,
        tekst = "Medlemmet har minst halve året hatt den daglige omsorgen for et barn som ikke har fylt seks år innen årets utgang"
    ),
    IKKE_KRAV_OM_MINST_HALVT_AR_I_FODSELSAR(
        kortTittel = "",
        kapittel = 20,
        paragraf = 8,
        bokstav = "a",
        ledd = 1,
        punktum = 2,
        tekst = "For barnets fødselsår opptjenes det samme beløpet selv om omsorgen har vart mindre enn et halvt år."
    ),
    OPPTJENING_GIS_BARNETRYGDMOTTAKER(
        kortTittel = "",
        kapittel = 20,
        paragraf = 8,
        bokstav = "a",
        ledd = 1,
        punktum = 3,
        tekst = "Opptjening gis den som mottar barnetrygd for barnet etter barnetrygdloven, dersom ikke noe annet er bestemt i forskrift"
    ),
    FULL_OMSORG_KAN_GODSKRIVES_AUTOMATISK(
        kortTittel = "Forskrift om omsorgspoeng – barn",
        kapittel = 3,
        paragraf = 4,
        ledd = 2,
        tekst = "En person som mottar barnetrygd for barnet alene gis omsorgsopptjening uten å sette fram krav om det"
    ),
    FYLLER_17_AR(
        kortTittel = "Forskrift om alderspensjon i folketrygden",
        kapittel = 20,
        paragraf = 8,
        ledd = 2,
        tekst = "Det kan gis pensjonsopptjening etter første ledd fra og med det året vedkommende fyller 17 år, til og med det året vedkommende fyller 69 år."
    ),

    FYLLER_69_AR(
        kortTittel = "Forskrift om alderspensjon i folketrygden",
        kapittel = 20,
        paragraf = 8,
        ledd = 2,
        tekst = "Det kan gis pensjonsopptjening etter første ledd fra og med det året vedkommende fyller 17 år, til og med det året vedkommende fyller 69 år."
    ),
    OMSORGSOPPTJENING_GIS_KUN_EN_OMSORGSYTER(
        kortTittel = "Forskrift om omsorgspoeng – barn",
        kapittel = null,
        paragraf = 2,
        ledd = 1,
        tekst = "Omsorgspoeng på grunnlag av omsorgen for ett barn eller for flere barn som bor sammen, gis bare én omsorgsyter for det enkelte kalenderår. "
    );

    override fun toString(): String {
        return name
    }
}

