package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.Oppgave
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgskategori


object OmsorgsyterErMedlemIFolketrygden : ParagrafVilkår<OmsorgsyterErMedlemIFolketrygden.Grunnlag>() {
    override fun vilkarsVurder(grunnlag: Grunnlag): Vurdering {
        return Vurdering(
            grunnlag = grunnlag,
            utfall = bestemUtfall(grunnlag),
        )
    }

    override fun <T : Vilkar<Grunnlag>> T.bestemUtfall(grunnlag: Grunnlag): VilkårsvurderingUtfall {
        return when (grunnlag.omsorgsytersOmsorgsmåneder.omsorgstype()) {
            DomainOmsorgskategori.BARNETRYGD -> {
                setOf(
                    JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_a_Første_Punktum
                )
            }

            DomainOmsorgskategori.HJELPESTØNAD -> {
                setOf(
                    JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_b_Første_Punktum
                )
            }
        }.let {
            when {
                grunnlag.erInnvilget() -> {
                    VilkårsvurderingUtfall.Innvilget.Vilkår(it)
                }

                grunnlag.erInnvilgetTilTrossForPerioderUtenMedlemskap() -> {
                    VilkårsvurderingUtfall.Innvilget.Vilkår(it)
                }

                grunnlag.erInnvilgetTilTrossForPerioderMedFrivilligEllerPliktigMedlemskap() -> {
                    VilkårsvurderingUtfall.Innvilget.Vilkår(it)
                }

                grunnlag.manuell() -> {
                    VilkårsvurderingUtfall.Ubestemt(it)
                }

                else -> {
                    VilkårsvurderingUtfall.Avslag.Vilkår(it)
                }
            }
        }
    }


    data class Vurdering(
        override val grunnlag: Grunnlag,
        override val utfall: VilkårsvurderingUtfall
    ) : ParagrafVurdering<Grunnlag>(){

        override fun hentOppgaveopplysninger(behandling: FullførtBehandling): Oppgaveopplysninger {
            return Oppgaveopplysninger.Generell(
                oppgavemottaker = behandling.omsorgsyter,
                oppgaveTekst = Oppgave.perioderMedPliktigEllerFrivilligMedlemskap(behandling.omsorgsmottaker)
            )
        }
    }

    data class Grunnlag(
        val medlemskapsgrunnlag: Medlemskapsgrunnlag,
        val omsorgsytersOmsorgsmåneder: Omsorgsmåneder,
        val antallMånederRegel: AntallMånederRegel,
        val landstilknytningMåneder: Landstilknytningmåneder,
    ) : ParagrafGrunnlag() {

        private val ikkeMedlem = medlemskapsgrunnlag.alleMånederUtenMedlemskap()
        private val medlem = medlemskapsgrunnlag.alleMånederMedMedlemskap()
        private val omsorgsmåneder = omsorgsytersOmsorgsmåneder.alle()
        private val minimumAntallMånederAntattMedlem = antallMånederRegel.antall

        /**
         * Bygger på en antakelse om at personer som mottar barnetrygd til å begynne med er vurdert etter
         * [Barnetrygdloven §4](https://lovdata.no/dokument/NL/lov/2002-03-08-4/KAPITTEL_2#%C2%A74) og/eller
         * [Barnetrygdloven §5](https://lovdata.no/dokument/NL/lov/2002-03-08-4/KAPITTEL_2#%C2%A75).
         * Dersom personen ikke har noen unntaksperioder legger vi til grunn at disse vurderingene i tilstrekkelig
         * grad sannnsynliggjør medlemskap i folketrygden.
         */
        fun erInnvilget(): Boolean {
            val antattMedlem = omsorgsmåneder.minus(ikkeMedlem).minus(medlem)
            return (ikkeMedlem.isEmpty() && medlem.isEmpty()) || antattMedlem.count() >= minimumAntallMånederAntattMedlem
        }

        /**
         * Godtar at en person kan ha x antall unntaksmåneder som ikke-medlem, så lenge det fortsatt er tilstrekkelig
         * antall omsorgsmåneder hvor vi antar at personen er medlem.
         */
        fun erInnvilgetTilTrossForPerioderUtenMedlemskap(): Boolean {
            require(!erInnvilget()) { "Rekkefølgeavhengig" }
            val antattMedlem = omsorgsmåneder.minus(ikkeMedlem)
            return medlem.isEmpty() && antattMedlem.count() >= minimumAntallMånederAntattMedlem
        }

        /**
         * Godtar at en person kan ha x antall unntaksmåneder som pliktig/frivillig medlem, så lenge det fortsatt er tilstrekkelig
         * antall omsorgsmåneder hvor vi antar at personen er medlem.
         */
        fun erInnvilgetTilTrossForPerioderMedFrivilligEllerPliktigMedlemskap(): Boolean {
            require(!erInnvilget() && !erInnvilgetTilTrossForPerioderUtenMedlemskap()) { "Rekkefølgeavhengig" }
            val antattMedlem = omsorgsmåneder.minus(medlem)
            return ikkeMedlem.isEmpty() && antattMedlem.count() >= minimumAntallMånederAntattMedlem
        }

        /**
         * Dersom bruker tilsammen har tilstrekkelig antall omsorgsmåneder hvor man er antatt medlem og/eller har
         * unntaksperioder med frivillig/pliktig medlemskap må det vurderes manuelt. Krever at omsorgsmåneder
         * overlapper med periodene man har vært frivillig/pliktig medlem samt at bruker for den samme perioden hadde
         * landstilknytning norge.
         */
        fun manuell(): Boolean {
            require(!erInnvilget() && !erInnvilgetTilTrossForPerioderUtenMedlemskap() && !erInnvilgetTilTrossForPerioderMedFrivilligEllerPliktigMedlemskap()) { "Rekkefølgeavhengig" }
            val antattMedlem = omsorgsmåneder.minus(ikkeMedlem).minus(medlem).plus(omsorgsmåneder.intersect(medlem))
            return landstilknytningMåneder.erNorge(antattMedlem) && antattMedlem.count() >= minimumAntallMånederAntattMedlem
        }
    }
}