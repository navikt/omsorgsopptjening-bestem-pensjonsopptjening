package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils

import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionOperations
import org.springframework.transaction.support.TransactionTemplate

interface NewTransactionTemplate : TransactionOperations

internal class NewTransactionTemplateImpl(
    private val transactionTemplate: TransactionTemplate
) : NewTransactionTemplate, TransactionOperations by transactionTemplate {
    init {
        require(transactionTemplate.propagationBehavior == TransactionDefinition.PROPAGATION_REQUIRES_NEW)
    }
}