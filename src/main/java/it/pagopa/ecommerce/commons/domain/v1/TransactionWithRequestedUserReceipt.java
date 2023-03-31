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
 * (by meaning of notifications-service has take in charge successful mail
 * sending to the user)
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
     * @param baseTransaction                transaction to extend with receipt data
     * @param transactionAddUserReceiptEvent transaction user receipt added event
     */
    public TransactionWithRequestedUserReceipt(
            BaseTransactionClosed baseTransaction,
            TransactionAddUserReceiptEvent transactionAddUserReceiptEvent
    ) {
        super(baseTransaction, transactionAddUserReceiptEvent.getData());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transaction applyEvent(Object event) {
        return switch (event) {
            case TransactionUserReceiptAddedEvent e -> {
                if (e.getData().getResponseOutcome().equals(TransactionUserReceiptData.Outcome.OK)) {
                    yield new TransactionWithUserReceiptOk(this, e);
                } else {
                    yield new TransactionWithUserReceiptKo(this, e);
                }
            }
            case TransactionExpiredEvent e -> new TransactionExpired(this, e);
            case TransactionRefundRequestedEvent e -> new TransactionWithRefundRequested(this, e);
            case TransactionUserReceiptAddErrorEvent e -> new TransactionWithUserReceiptError(this, e);
            default -> this;
        };
    }

    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.NOTIFICATION_REQUESTED;
    }
}
