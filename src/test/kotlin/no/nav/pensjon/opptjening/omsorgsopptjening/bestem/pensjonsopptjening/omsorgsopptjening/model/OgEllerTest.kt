package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Eller.Companion.eller
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Og.Companion.og
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class OgEllerTest {

    private val grunnlag = AldersvurderingsGrunnlag(
        person = Person(
            fødselsdato = LocalDate.now(),
            dødsdato = null,
            familierelasjoner = Familierelasjoner(emptyList()),
            identhistorikk = IdentHistorikk(setOf(Ident.FolkeregisterIdent.Gjeldende("1")))
        ),
        omsorgsAr = 2020
    )
    private val innvilget = OmsorgsyterOppfyllerAlderskrav.Vurdering(
        grunnlag = grunnlag,
        utfall = VilkårsvurderingUtfall.Innvilget.Vilkår(emptySet()),
        gyldigAldersintervall = 0..999
    )
    private val avslag = innvilget.copy(utfall = VilkårsvurderingUtfall.Avslag.Vilkår(emptySet()))
    private val ubestemt = innvilget.copy(utfall = VilkårsvurderingUtfall.Ubestemt.Vilkår(emptySet()))


    @Test
    fun `innvilget hvis nøstet eller har en innvilget`() {
        eller(og(innvilget), og(avslag))
            .also { assertThat(it.erInnvilget()).isTrue() }
    }

    @Test
    fun `innvilget hvis nøstet eller har en ubestemt`() {
        eller(og(innvilget), og(ubestemt))
            .also { assertThat(it.erInnvilget()).isTrue() }
    }

    @Test
    fun `avslag hvis nøstet og har et avslag`() {
        og(eller(innvilget), og(avslag))
            .also { assertThat(it.erAvslått()).isTrue() }
    }

    @Test
    fun `ubestemt hvis nøstet og har et avslag`() {
        og(eller(innvilget), eller(innvilget, ubestemt), eller(avslag, ubestemt))
            .also { assertThat(it.erUbestemt()).isTrue() }
    }

    @Test
    fun `avslag hvis nøstet avslag`() {
        og(innvilget, og(innvilget, innvilget), og(innvilget, eller(avslag, avslag)))
            .also { assertThat(it.erAvslått()).isTrue() }
    }
}