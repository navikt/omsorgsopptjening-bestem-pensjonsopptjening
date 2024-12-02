package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

sealed class VilkårsvurderingUtfall {

    sealed class Innvilget : VilkårsvurderingUtfall() {

        data class Vilkår(val henvisninger: Set<JuridiskHenvisning>) : Innvilget()
    }

    sealed class Avslag : VilkårsvurderingUtfall() {
        data class Vilkår(val henvisninger: Set<JuridiskHenvisning>) : Avslag()
    }

    sealed class Ubestemt : VilkårsvurderingUtfall() {
        data class Vilkår(val henvisninger: Set<JuridiskHenvisning>) : Ubestemt()
    }


    fun erInnvilget(): Boolean {
        return when (this) {
            is Avslag -> false
            is Innvilget -> true
            is Ubestemt -> false
        }
    }

    fun erAvslag(): Boolean {
        return when (this) {
            is Avslag -> true
            is Innvilget -> false
            is Ubestemt -> false
        }
    }

    fun erUbestemt(): Boolean {
        return when (this) {
            is Avslag -> false
            is Innvilget -> false
            is Ubestemt -> true
        }
    }

}

sealed class BehandlingUtfall {
    data object Innvilget : BehandlingUtfall()
    data object Avslag : BehandlingUtfall()
    data object Manuell : BehandlingUtfall()

    fun erInnvilget(): Boolean {
        return when (this) {
            is Avslag -> false
            is Innvilget -> true
            is Manuell -> false
        }
    }

    fun erManuell(): Boolean {
        return when (this) {
            Avslag -> false
            Innvilget -> false
            Manuell -> true
        }
    }

    fun erAvslag(): Boolean {
        return when (this) {
            Avslag -> true
            Innvilget -> false
            Manuell -> false
        }
    }
}

sealed class AggregertBehandlingUtfall {
    data object Innvilget : AggregertBehandlingUtfall()
    data object Avslag : AggregertBehandlingUtfall()
    data object Manuell : AggregertBehandlingUtfall()

    fun erInnvilget(): Boolean {
        return when (this) {
            is Avslag -> false
            is Innvilget -> true
            is Manuell -> false
        }
    }

    fun erManuell(): Boolean {
        return when (this) {
            Avslag -> false
            Innvilget -> false
            Manuell -> true
        }
    }

    fun erAvslag(): Boolean {
        return when (this) {
            Avslag -> true
            Innvilget -> false
            Manuell -> false
        }
    }
}