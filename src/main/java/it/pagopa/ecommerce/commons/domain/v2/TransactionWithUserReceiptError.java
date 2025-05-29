package it.pagopa.ecommerce.commons.domain.v2;

import it.pagopa.ecommerce.commons.documents.v2.*;
import it.pagopa.ecommerce.commons.domain.v2.pojos.BaseTransactionWithRequestedUserReceipt;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * Transaction closed for which an error occurs during user receipt notification
 * </p>
 * Applicable events with resulting aggregates are:
 * <ul>
 * <li>{@link TransactionUserReceiptAddedEvent} with OK send payment result
 * outcome --> {@link TransactionWithUserReceiptOk}</li>
 * <li>{@link TransactionUserReceiptAddedEvent} with KO send payment result
 * outcome --> {@link TransactionWithUserReceiptKo}</li>
 * <li>{@link TransactionExpiredEvent} --> {@link TransactionExpired}</li>
 * <li>{@link TransactionRefundRequestedEvent} -->
 * {@link TransactionWithRefundRequested}</li>
 * </ul>
 * Any other event than the above ones will be discarded.
 *
 * @see it.pagopa.ecommerce.commons.domain.v2.Transaction
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
 * Suppressed because the Transaction hierarchy modeled here force TransactionWithUserReceiptError
 * to be instantiated only starting from a TransactionClosed. The hierarchy dept is strictly correlated
 * to the depth of the graph representing the finite state machine so can be accepted that hierarchy level
 * is deeper than the max authorized level
 *
 * @formatter:on
 */
@SuppressWarnings("java:S110")
public final class TransactionWithUserReceiptError extends BaseTransactionWithRequestedUserReceipt
        implements it.pagopa.ecommerce.commons.domain.v2.Transaction {

    /**
     * Main constructor.
     *
     * @param baseTransaction                     transaction to extend with receipt
     *                                            data
     * @param transactionUserReceiptAddErrorEvent transaction add user receipt error
     *                                            event
     */
    public TransactionWithUserReceiptError(
            BaseTransactionWithRequestedUserReceipt baseTransaction,
            TransactionUserReceiptAddErrorEvent transactionUserReceiptAddErrorEvent
    ) {
        super(baseTransaction, transactionUserReceiptAddErrorEvent.getData());
    }

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
        } else if (event instanceof TransactionRefundRequestedEvent e) {
            if (this.getTransactionUserReceiptData().getResponseOutcome()
                    .equals(TransactionUserReceiptData.Outcome.KO)) {
                return new TransactionWithRefundRequested(this, e);
            }
        }

        return this;
    }

    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.NOTIFICATION_ERROR;
    }
}
