package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketOmsorgsgrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.PersonMedFødselsår

data class FullførtBehandling(
    val omsorgsAr: Int,
    val omsorgsyter: PersonMedFødselsår,
    val omsorgstype: DomainOmsorgstype,
    val grunnlag: BeriketOmsorgsgrunnlag,
    val utfall: BehandlingUtfall,
    val vilkårsvurdering: VilkarsVurdering<*>
){
    fun omsorgsmottaker(): PersonMedFødselsår? {
        return when(utfall){
            is AutomatiskGodskrivingUtfall.Avslag -> {
                null
            }
            is AutomatiskGodskrivingUtfall.Innvilget -> {
                utfall.omsorgsmottaker
            }
        }
    }

    fun kilde(): DomainKilde {
        return grunnlag.kilde
    }
}