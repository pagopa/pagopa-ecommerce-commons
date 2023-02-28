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
 * a {@link TransactionWithUserReceiptOk}.
 * </p>
 *
 * @see Transaction
 * @see BaseTransactionWithCompletedAuthorization
 */
@EqualsAndHashCode(callSuper = true)
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public final class TransactionClosed extends BaseTransactionWithCompletedAuthorization
        implements Transaction {

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
            case TransactionUserReceiptAddedEvent e -> {
                if (TransactionUserReceiptData.Outcome.OK.equals(e.getData().getResponseOutcome())) {
                    yield new TransactionWithUserReceiptOk(this, e);
                } else {
                    yield new TransactionWithUserReceiptKo(this, e);
                }
            }
            case TransactionExpiredEvent e -> new TransactionExpired(this, e);
            case TransactionRefundRequestedEvent e -> new TransactionWithRefundRequested(this, e);
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
