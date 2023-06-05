package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.kafka.OmsorgsopptjeningProducer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketOmsorgsgrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.AutomatiskGodskrivingUtfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Behandling
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("!no-kafka")
class VurderGodskrivOmsorgsopptjeningService(
    private val behandlingRepo: BehandlingRepo,
    private val omsorgsOpptejningProducer: OmsorgsopptjeningProducer,
) {

    fun vurder(omsorgsGrunnlag: BeriketOmsorgsgrunnlag) {
        //TODO vilkår for at andre ikke får for samme barn samme år etc
        behandlingRepo.persist(Behandling(grunnlag = omsorgsGrunnlag)).let {
            when (it.utfall) {
                is AutomatiskGodskrivingUtfall.Avslag -> {
                    //TODO
                }

                is AutomatiskGodskrivingUtfall.Innvilget -> {
                    omsorgsOpptejningProducer.send(it)
                }
            }
        }
    }
}