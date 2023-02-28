package it.pagopa.ecommerce.commons.domain.v1;

import it.pagopa.ecommerce.commons.documents.v1.*;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransaction;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionClosureWithoutAuthorization;
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
 * @see BaseTransactionClosureWithoutAuthorization
 */
@EqualsAndHashCode(callSuper = true)
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public final class TransactionClosedWithoutCompletedAuthorization extends BaseTransactionClosureWithoutAuthorization
        implements Transaction {

    BaseTransactionClosureEvent baseTransactionClosureEvent;

    /**
     * Primary constructor
     *
     * @param baseTransaction             base transaction
     * @param baseTransactionClosureEvent the base transaction closure event
     */
    public TransactionClosedWithoutCompletedAuthorization(
            BaseTransaction baseTransaction,
            BaseTransactionClosureEvent baseTransactionClosureEvent
    ) {
        super(baseTransaction, baseTransactionClosureEvent);
        this.baseTransactionClosureEvent = baseTransactionClosureEvent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transaction applyEvent(Object event) {
        return switch (event) {
            case TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent -> {
                if (this.getTransactionAtPreviousState() instanceof BaseTransactionWithCompletedAuthorization baseTransactionWithCompletedAuthorization && TransactionClosureData.Outcome.OK.equals(this.baseTransactionClosureEvent.getData().getResponseOutcome())) {
                    yield new TransactionWithUserReceipt(baseTransactionWithCompletedAuthorization, transactionUserReceiptAddedEvent);
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
