package it.pagopa.ecommerce.commons.domain.v1;

import it.pagopa.ecommerce.commons.documents.v1.TransactionRefundedEvent;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionRefunded;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionWithRefundRequested;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * Transaction refunded. This state means that the transaction is not completed
 * successfully requiring a refund to be performed to the user. This is a final
 * state so any event applied to this class will be discarded.
 * </p>
 *
 * @see Transaction
 * @see BaseTransactionRefunded
 */
@EqualsAndHashCode(callSuper = true)
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
/*
 * @formatter:off
 *
 * Warning java:S110 - This class has x parents which is greater than 5 authorized
 * Suppressed because the Transaction hierarchy modeled here force TransactionWithUserReceiptOk
 * to be instantiated only starting from a TransactionClosed. The hierarchy dept is strictly correlated
 * to the depth of the graph representing the finite state machine so can be accepted that hierarchy level
 * is deeper than the max authorized level
 *
 * @formatter:on
 */
@SuppressWarnings("java:S110")
public final class TransactionRefunded extends BaseTransactionRefunded implements Transaction {

    /**
     * Primary constructor
     *
     * @param baseTransaction the base transaction with refund requested
     * @param event           the transaction refunded event
     */
    public TransactionRefunded(
            BaseTransactionWithRefundRequested baseTransaction,
            TransactionRefundedEvent event
    ) {
        super(baseTransaction, event.getData());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.REFUNDED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transaction applyEvent(Object event) {
        return this;
    }
}
