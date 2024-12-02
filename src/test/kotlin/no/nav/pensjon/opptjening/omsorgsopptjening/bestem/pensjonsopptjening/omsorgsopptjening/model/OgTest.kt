package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Og.Companion.og
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class OgTest {

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
    fun `all avslag is avslag`() {
        og(avslag, avslag).also { assertThat(it.erAvslått()).isTrue() }
    }


    @Test
    fun `one avslag is avslag`() {
        og(avslag, innvilget).also { assertThat(it.erAvslått()).isTrue() }
    }

    @Test
    fun `all innvilget is innvilget`() {
        og(innvilget, innvilget).also { assertThat(it.erInnvilget()).isTrue() }
    }

    @Test
    fun `nested og with all avslag is avslag`() {
        og(avslag, og(avslag)).also { assertThat(it.erAvslått()).isTrue() }
    }

    @Test
    fun `nested og with all innvilget is avslag`() {
        og(avslag, og(innvilget)).also { assertThat(it.erAvslått()).isTrue() }
    }

    @Test
    fun `nested all innvilget is innvilget`() {
        og(innvilget, og(innvilget)).also { assertThat(it.erInnvilget()).isTrue() }
    }

    @Test
    fun `all innvilget and single ubestemt is ubestemt`() {
        og(innvilget, ubestemt).also { assertThat(it.erUbestemt()).isTrue() }
    }

    @Test
    fun `all innvilget and multiple ubestemt is ubestemt`() {
        og(innvilget, ubestemt, ubestemt).also { assertThat(it.erUbestemt()).isTrue() }
    }

    @Test
    fun `all innvilget and nested ubestemt is ubestemt`() {
        og(innvilget, og(ubestemt)).also { assertThat(it.erUbestemt()).isTrue() }
    }

    @Test
    fun `single avslag is avslag`() {
        og(innvilget, og(ubestemt, avslag), og(innvilget)).also { assertThat(it.erAvslått()).isTrue() }
    }
}