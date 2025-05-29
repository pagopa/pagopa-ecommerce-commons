package it.pagopa.ecommerce.commons.domain.v1;

import it.pagopa.ecommerce.commons.documents.v1.*;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionClosed;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionWithRequestedUserReceipt;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * Transaction closed for which email communication process has been requested
 * (this means that notifications-service has taken successfully in charge
 * sending an email to the user)
 * </p>
 * Applicable events with resulting aggregates are:
 * <ul>
 * <li>{@link TransactionUserReceiptAddedEvent} with OK `sendPaymentResult`
 * outcome --> {@link TransactionWithUserReceiptOk}</li>
 * <li>{@link TransactionUserReceiptAddedEvent} with KO send payment result
 * outcome --> {@link TransactionWithUserReceiptKo}</li>
 * <li>{@link TransactionExpiredEvent} --> {@link TransactionExpired}</li>
 * <li>{@link TransactionUserReceiptAddErrorEvent} -->
 * {@link TransactionWithUserReceiptError}</li>
 * </ul>
 * Any other event than the above ones will be discarded.
 *
 * @see Transaction
 * @see BaseTransactionWithRequestedUserReceipt
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
public final class TransactionWithRequestedUserReceipt extends BaseTransactionWithRequestedUserReceipt
        implements Transaction {

    /**
     * Main constructor.
     *
     * @param baseTransaction                      transaction to extend with
     *                                             receipt data
     * @param transactionUserReceiptRequestedEvent transaction user receipt added
     *                                             event
     */
    public TransactionWithRequestedUserReceipt(
            BaseTransactionClosed baseTransaction,
            TransactionUserReceiptRequestedEvent transactionUserReceiptRequestedEvent
    ) {
        super(baseTransaction, transactionUserReceiptRequestedEvent.getData());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transaction applyEvent(Object event) {
        if (event instanceof TransactionUserReceiptAddedEvent e) {
            if (e.getData().getResponseOutcome().equals(TransactionUserReceiptData.Outcome.OK)) {
                return new TransactionWithUserReceiptOk(this, e);
            } else {
                return new TransactionWithUserReceiptKo(this, e);
            }
        } else if (event instanceof TransactionExpiredEvent) {
            return new TransactionExpired(this, (TransactionExpiredEvent) event);
        } else if (event instanceof TransactionUserReceiptAddErrorEvent) {
            return new TransactionWithUserReceiptError(this, (TransactionUserReceiptAddErrorEvent) event);
        } else {
            return this;
        }
    }

    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.NOTIFICATION_REQUESTED;
    }
}
