package it.pagopa.ecommerce.commons.domain.v2;

import it.pagopa.ecommerce.commons.documents.v2.TransactionClosedEvent;
import it.pagopa.ecommerce.commons.domain.v2.pojos.BaseTransaction;
import it.pagopa.ecommerce.commons.domain.v2.pojos.BaseTransactionWithCancellationRequested;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * Transaction canceled by user. This is a final state, so any event applied to
 * this class will be discarded.
 * </p>
 *
 * @see Transaction
 * @see BaseTransaction
 */
@EqualsAndHashCode(callSuper = true)
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public final class TransactionUserCanceled extends BaseTransaction implements Transaction {

    TransactionClosedEvent transactionClosedEvent;

    /**
     * Primary constructor
     *
     * @param baseTransaction        the base transaction
     * @param transactionClosedEvent the transaction closed event
     */
    public TransactionUserCanceled(
            BaseTransactionWithCancellationRequested baseTransaction,
            TransactionClosedEvent transactionClosedEvent
    ) {
        super(
                baseTransaction.getTransactionId(),
                baseTransaction.getPaymentNotices(),
                baseTransaction.getEmail(),
                baseTransaction.getCreationDate(),
                baseTransaction.getClientId()
        );
        this.transactionClosedEvent = transactionClosedEvent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.CANCELED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transaction applyEvent(Object event) {
        return this;
    }
}
