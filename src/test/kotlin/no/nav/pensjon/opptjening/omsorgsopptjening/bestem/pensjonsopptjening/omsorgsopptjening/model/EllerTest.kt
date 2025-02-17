package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Eller.Companion.eller
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class EllerTest {

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
        eller(avslag, avslag).also { assertThat(it.erAvslått()).isTrue() }
    }

    @Test
    fun `one innvilget is innvilget`() {
        eller(avslag, innvilget).also { assertThat(it.erInnvilget()).isTrue() }
    }

    @Test
    fun `all innvilget is innvilget`() {
        eller(innvilget, innvilget).also { assertThat(it.erInnvilget()).isTrue() }
    }

    @Test
    fun `nested eller with all avslag is avslag`() {
        eller(avslag, eller(avslag)).also { assertThat(it.erAvslått()).isTrue() }
    }

    @Test
    fun `nested eller with all innvilget is innvilget`() {
        eller(avslag, eller(innvilget)).also { assertThat(it.erInnvilget()).isTrue() }
    }

    @Test
    fun `avslag og ubestemt er ubestemt`() {
        eller(avslag, ubestemt).also { assertThat(it.erUbestemt()).isTrue() }
    }

    @Test
    fun `innvilget og ubestemt er innvilget`() {
        eller(innvilget, ubestemt).also { assertThat(it.erInnvilget()).isTrue() }
    }

    @Test
    fun `ubestemt og ubestemt er ubestemt`() {
        eller(ubestemt, ubestemt).also { assertThat(it.erUbestemt()).isTrue() }
    }
}