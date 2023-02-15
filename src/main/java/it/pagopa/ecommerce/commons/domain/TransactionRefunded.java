package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.domain.pojos.BaseTransaction;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.EqualsAndHashCode;

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
public final class TransactionRefunded extends BaseTransaction implements Transaction {

    /**
     * Primary constructor
     *
     * @param baseTransaction the base transaction
     */
    public TransactionRefunded(
            BaseTransaction baseTransaction
    ) {
        super(
                baseTransaction.getTransactionId(),
                baseTransaction.getPaymentNotices(),
                baseTransaction.getEmail(),
                baseTransaction.getCreationDate(),
                baseTransaction.getClientId()
        );
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
