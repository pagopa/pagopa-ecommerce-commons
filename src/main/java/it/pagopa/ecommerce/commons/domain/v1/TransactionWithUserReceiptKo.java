package it.pagopa.ecommerce.commons.domain.v1;

import it.pagopa.ecommerce.commons.documents.v1.TransactionRefundRequestedEvent;
import it.pagopa.ecommerce.commons.documents.v1.TransactionUserReceiptAddedEvent;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionClosed;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * Transaction closed and notified to the user. This state is reached when Nodo
 * sendPaymentResult has KO outcome
 * </p>
 * <p>
 * Applicable events with resulting aggregates are:
 * <ul>
 * <li>{@link TransactionRefundRequestedEvent} -->
 * {@link TransactionWithRefundRequested}</li>
 * </ul>
 * Any other event than the above ones will be discarded.
 *
 * @see Transaction
 * @see BaseTransactionClosed
 */
@EqualsAndHashCode(callSuper = true)
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
/*
 * @formatter:off
 *
 * Warning java:S110 - This class has x parents which is greater than 5 authorized
 * Suppressed because the Transaction hierarchy modeled here force TransactionWithUserReceiptKo
 * to be instantiated only starting from a TransactionClosed. The hierarchy dept is strictly correlated
 * to the depth of the graph representing the finite state machine so can be accepted that hierarchy level
 * is deeper than the max authorized level
 *
 * @formatter:on
 */
@SuppressWarnings("java:S110")
public final class TransactionWithUserReceiptKo extends BaseTransactionClosed
        implements Transaction {

    TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent;

    /**
     * Main constructor.
     *
     * @param baseTransaction                  transaction to extend with receipt
     *                                         data
     * @param transactionUserReceiptAddedEvent transaction user receipt added event
     */
    public TransactionWithUserReceiptKo(
            BaseTransactionClosed baseTransaction,
            TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent
    ) {
        super(baseTransaction, baseTransaction.getTransactionClosureData());
        this.transactionUserReceiptAddedEvent = transactionUserReceiptAddedEvent;
    }

    @Override
    public Transaction applyEvent(Object event) {
        if (event instanceof TransactionRefundRequestedEvent e) {
            return new TransactionWithRefundRequested(this, e);
        }
        return this;

    }

    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.NOTIFIED_KO;
    }
}
