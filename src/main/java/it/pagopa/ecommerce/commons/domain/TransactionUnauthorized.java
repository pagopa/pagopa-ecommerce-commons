package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.domain.pojos.BaseTransaction;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Transaction unauthorized, payment gateway rejected the authorization request.
 * This is a final state so any event applied to this class will be discarded
 * </p>
 *
 * @see Transaction
 * @see BaseTransaction
 */
@EqualsAndHashCode(callSuper = true)
public final class TransactionUnauthorized extends BaseTransaction implements Transaction {

    /**
     * Primary constructor
     *
     * @param baseTransaction the base transaction
     */
    public TransactionUnauthorized(
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
        return TransactionStatusDto.UNAUTHORIZED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transaction applyEvent(Object event) {
        return this;
    }
}
