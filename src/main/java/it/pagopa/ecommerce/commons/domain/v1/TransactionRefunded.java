package it.pagopa.ecommerce.commons.domain.v1;

import it.pagopa.ecommerce.commons.documents.v1.TransactionRefundedEvent;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransaction;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionRefunded;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * Transaction refunded. This state means that the transaction is not completed
 * successfully requiring a refund to be performed to the user This is a final
 * state so any event applied to this class will be discarded
 * </p>
 *
 * @see Transaction
 * @see BaseTransaction
 */
@EqualsAndHashCode(callSuper = true)
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public final class TransactionRefunded extends BaseTransactionRefunded implements Transaction {

    /**
     * Primary constructor
     *
     * @param baseTransaction the base transaction
     * @param event           the transaction refunded event
     */
    public TransactionRefunded(
            BaseTransaction baseTransaction,
            TransactionRefundedEvent event
    ) {
        super(baseTransaction, event.getData());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.REFUNDED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transaction applyEvent(Object event) {
        return this;
    }
}
