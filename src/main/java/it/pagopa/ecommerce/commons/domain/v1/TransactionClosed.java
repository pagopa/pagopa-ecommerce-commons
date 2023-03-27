package it.pagopa.ecommerce.commons.domain.v1;

import it.pagopa.ecommerce.commons.documents.v1.*;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionClosed;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionWithCompletedAuthorization;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * Closed transaction.
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
 * @see BaseTransactionClosed
 */
@EqualsAndHashCode(callSuper = true)
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public final class TransactionClosed extends BaseTransactionClosed
        implements Transaction {

    /**
     * Primary constructor
     *
     * @param baseTransaction        base transaction
     * @param transactionClosedEvent the transaction closed event
     */
    public TransactionClosed(
            BaseTransactionWithCompletedAuthorization baseTransaction,
            TransactionClosedEvent transactionClosedEvent
    ) {
        super(baseTransaction, transactionClosedEvent.getData());
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

    /**
     * {@inheritDoc}
     */
    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.CLOSED;
    }
}
