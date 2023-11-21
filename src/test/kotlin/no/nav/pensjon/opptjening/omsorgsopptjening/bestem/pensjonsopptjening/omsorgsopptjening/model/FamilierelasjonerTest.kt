package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FamilierelasjonerTest {
    @Test
    fun `finner far, mor, barn og foreldre for folkeregisterident`() {
        Familierelasjoner(
            relasjoner = listOf(
                Familierelasjon(
                    ident = Ident.FolkeregisterIdent("far"),
                    relasjon = Familierelasjon.Relasjon.FAR
                ),
                Familierelasjon(
                    ident = Ident.FolkeregisterIdent("mor"),
                    relasjon = Familierelasjon.Relasjon.MOR
                ),
                Familierelasjon(
                    ident = Ident.FolkeregisterIdent("barn"),
                    relasjon = Familierelasjon.Relasjon.BARN
                ),
            )
        ).also {
            assertThat(it.erBarn("barn")).isTrue()
            assertThat(it.erBarn("ikkebarn")).isFalse()
            assertThat(it.erForelder("far")).isTrue()
            assertThat(it.erForelder("ikkefar")).isFalse()
            assertThat(it.erForelder("mor")).isTrue()
            assertThat(it.erForelder("ikkemor")).isFalse()
            assertThat(it.finnForeldre()).isEqualTo(
                Foreldre.Identifisert(
                    farEllerMedmor = Ident.FolkeregisterIdent(ident = "far"),
                    mor = Ident.FolkeregisterIdent(ident = "mor")
                )
            )
        }
    }

    @Test
    fun `finner medmor, mor, barn og foreldre for folkeregisterident`() {
        Familierelasjoner(
            relasjoner = listOf(
                Familierelasjon(
                    ident = Ident.FolkeregisterIdent("medmor"),
                    relasjon = Familierelasjon.Relasjon.MEDMOR
                ),
                Familierelasjon(
                    ident = Ident.FolkeregisterIdent("mor"),
                    relasjon = Familierelasjon.Relasjon.MOR
                ),
                Familierelasjon(
                    ident = Ident.FolkeregisterIdent("barn"),
                    relasjon = Familierelasjon.Relasjon.BARN
                ),
            )
        ).also {
            assertThat(it.erBarn("barn")).isTrue()
            assertThat(it.erBarn("ikkebarn")).isFalse()
            assertThat(it.erForelder("medmor")).isTrue()
            assertThat(it.erForelder("ikkeMedmor")).isFalse()
            assertThat(it.erForelder("mor")).isTrue()
            assertThat(it.erForelder("ikkemor")).isFalse()
            assertThat(it.finnForeldre()).isEqualTo(
                Foreldre.Identifisert(
                    farEllerMedmor = Ident.FolkeregisterIdent(ident = "medmor"),
                    mor = Ident.FolkeregisterIdent(ident = "mor")
                )
            )
        }
    }

    @Test
    fun `foreldre er ukjent dersom en av foreldrene ikke har folkeregisterident`() {
        Familierelasjoner(
            relasjoner = listOf(
                Familierelasjon(
                    ident = Ident.Ukjent,
                    relasjon = Familierelasjon.Relasjon.FAR
                ),
                Familierelasjon(
                    ident = Ident.FolkeregisterIdent("mor"),
                    relasjon = Familierelasjon.Relasjon.MOR
                ),
                Familierelasjon(
                    ident = Ident.FolkeregisterIdent("barn"),
                    relasjon = Familierelasjon.Relasjon.BARN
                ),
            )
        ).also {
            assertThat(it.erBarn("barn")).isTrue()
            assertThat(it.erForelder("far")).isFalse()
            assertThat(it.erForelder("mor")).isFalse()
            assertThat(it.finnForeldre()).isEqualTo(Foreldre.Ukjent)
        }
    }

    @Test
    fun `konstruktør lager ukjent type hvis ukjent-konstant oppretter objekt`() {
        Familierelasjon(
            Ident.IDENT_UKJENT,
            relasjon = Familierelasjon.Relasjon.FAR
        ).also {
            assertThat(it.ident).isInstanceOf(Ident.Ukjent::class.java)
        }
    }
}