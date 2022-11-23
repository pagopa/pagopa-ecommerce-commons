package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.annotations.AggregateRoot;
import it.pagopa.ecommerce.commons.documents.TransactionEvent;

@AggregateRoot
public sealed interface Transaction permits EmptyTransaction,TransactionActivated,TransactionActivationRequested,TransactionWithRequestedAuthorization,TransactionWithCompletedAuthorization,TransactionClosed,TransactionWithClosureError {
    Transaction applyEvent(TransactionEvent<?> event);
}
