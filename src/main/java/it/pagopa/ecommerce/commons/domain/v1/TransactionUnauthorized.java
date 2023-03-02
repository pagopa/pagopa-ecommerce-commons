package it.pagopa.ecommerce.commons.domain.v1;

import it.pagopa.ecommerce.commons.documents.v1.TransactionClosureFailedEvent;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionWithCompletedAuthorization;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * Transaction unauthorized, payment gateway rejected the authorization request.
 * This is a final state so any event applied to this class will be discarded
 * </p>
 *
 * @see Transaction
 * @see BaseTransactionWithCompletedAuthorization
 */
@EqualsAndHashCode(callSuper = true)
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public final class TransactionUnauthorized extends BaseTransactionWithCompletedAuthorization implements Transaction {

    TransactionClosureFailedEvent transactionClosureFailedEvent;

    /**
     * Primary constructor
     *
     * @param baseTransaction               the base transaction with completed
     *                                      authorization information
     * @param transactionClosureFailedEvent the transaction expired event
     */
    public TransactionUnauthorized(
            BaseTransactionWithCompletedAuthorization baseTransaction,
            TransactionClosureFailedEvent transactionClosureFailedEvent
    ) {
        super(
                baseTransaction,
                baseTransaction.getTransactionAuthorizationCompletedData()
        );
        this.transactionClosureFailedEvent = transactionClosureFailedEvent;
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
