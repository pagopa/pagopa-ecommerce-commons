package it.pagopa.ecommerce.commons.domain.v1;

import it.pagopa.ecommerce.commons.documents.v1.*;
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
 * <p>
 * To this class you can apply a {@link TransactionUserReceiptAddedEvent} to get
 * a {@link TransactionWithUserReceipt}.
 * </p>
 *
 * @see Transaction
 * @see BaseTransactionWithCompletedAuthorization
 */
@EqualsAndHashCode(callSuper = true)
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public final class TransactionClosed extends BaseTransactionWithCompletedAuthorization implements Transaction {

    TransactionClosedEvent transactionClosedEvent;

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
        super(baseTransaction, baseTransaction.getTransactionAuthorizationCompletedData());
        this.transactionClosedEvent = transactionClosedEvent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transaction applyEvent(Object event) {
        return switch (event) {
            case TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent -> {
                if (TransactionClosureData.Outcome.OK.equals(this.transactionClosedEvent.getData().getOutcome())) {
                    yield new TransactionWithUserReceipt(this, transactionUserReceiptAddedEvent);
                } else {
                    yield this;
                }
            }
            case TransactionExpiredEvent transactionExpiredEvent ->
                    new TransactionExpired(this, transactionExpiredEvent);
            case TransactionRefundedEvent transactionRefundedEvent ->
                    new TransactionRefunded(this, transactionRefundedEvent);
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
