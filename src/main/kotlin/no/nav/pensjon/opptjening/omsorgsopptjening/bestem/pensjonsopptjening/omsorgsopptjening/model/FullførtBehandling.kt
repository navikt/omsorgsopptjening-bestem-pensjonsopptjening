package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.PersonMedFødselsår

data class FullførtBehandling(
    val id: Long,
    val omsorgsAr: Int,
    val omsorgsyter: String,
    val omsorgsmottaker: String,
    val omsorgstype: DomainOmsorgstype,
    val grunnlag: BarnetrygdGrunnlag,
    val utfall: BehandlingUtfall,
    val vilkårsvurdering: VilkarsVurdering<*>
) {
    fun kilde(): DomainKilde {
        return grunnlag.kilde
    }
}