package it.pagopa.ecommerce.commons.domain.v2;

import it.pagopa.ecommerce.commons.documents.v2.TransactionExpiredEvent;
import it.pagopa.ecommerce.commons.domain.v2.pojos.BaseTransactionWithCancellationRequested;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * Transaction that was cancelled by user and cancellation process (Nodo
 * closePaymentV2) is expired
 * </p>
 * <p>
 * This is a final state so any event applied to this aggregate will be ignored
 * </p>
 *
 * @see Transaction
 * @see BaseTransactionWithCancellationRequested
 */
@EqualsAndHashCode(callSuper = true)
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public final class TransactionCancellationExpired extends BaseTransactionWithCancellationRequested
        implements Transaction {

    TransactionExpiredEvent transactionExpiredEvent;

    /**
     * Primary constructor
     *
     * @param baseTransaction         the base transaction
     * @param transactionExpiredEvent the transaction expired event
     */
    public TransactionCancellationExpired(
            BaseTransactionWithCancellationRequested baseTransaction,
            TransactionExpiredEvent transactionExpiredEvent
    ) {
        super(
                baseTransaction
        );
        this.transactionExpiredEvent = transactionExpiredEvent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.CANCELLATION_EXPIRED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transaction applyEvent(Object event) {
        return this;
    }
}
