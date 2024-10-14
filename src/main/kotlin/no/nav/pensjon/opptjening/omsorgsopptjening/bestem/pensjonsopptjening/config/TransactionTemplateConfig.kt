package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.NewTransactionTemplate
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.NewTransactionTemplateImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionTemplate


@Configuration
class TransactionTemplateConfig {

    @Bean
    fun transactionTemplate(transactionManager: PlatformTransactionManager): NewTransactionTemplate {
        return NewTransactionTemplateImpl(
            TransactionTemplate(transactionManager).apply {
                propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
            }
        )
    }
}
