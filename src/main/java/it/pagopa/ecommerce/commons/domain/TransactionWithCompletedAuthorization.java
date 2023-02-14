package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.documents.TransactionAuthorizationStatusUpdateData;
import it.pagopa.ecommerce.commons.documents.TransactionClosureErrorEvent;
import it.pagopa.ecommerce.commons.documents.TransactionClosureSentEvent;
import it.pagopa.ecommerce.commons.documents.TransactionExpiredEvent;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionWithCompletedAuthorization;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionWithRequestedAuthorization;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Transaction with a completed authorization.
 * </p>
 * <p>
 * To this class you can apply either a
 * {@link it.pagopa.ecommerce.commons.documents.TransactionClosureSentEvent} to
 * get a {@link it.pagopa.ecommerce.commons.domain.TransactionClosed} or a
 * {@link it.pagopa.ecommerce.commons.documents.TransactionClosureErrorEvent} to
 * get a {@link it.pagopa.ecommerce.commons.domain.TransactionWithClosureError}
 * </p>
 *
 * @see Transaction
 * @see BaseTransactionWithCompletedAuthorization
 */
@EqualsAndHashCode(callSuper = true)
public final class TransactionWithCompletedAuthorization extends BaseTransactionWithCompletedAuthorization
        implements
        Transaction {

    /**
     * Primary constructor
     *
     * @param baseTransaction               base transaction
     * @param authorizationStatusUpdateData data related to authorization status
     *                                      update
     */
    public TransactionWithCompletedAuthorization(
            BaseTransactionWithRequestedAuthorization baseTransaction,
            TransactionAuthorizationStatusUpdateData authorizationStatusUpdateData
    ) {
        super(baseTransaction, authorizationStatusUpdateData);
    }

    /** {@inheritDoc} */
    @Override
    public Transaction applyEvent(Object event) {
        return switch (event) {
            case TransactionClosureSentEvent closureSentEvent -> new TransactionClosed(
                    this,
                    closureSentEvent.getData()
            );
            case TransactionClosureErrorEvent closureErrorEvent -> new TransactionWithClosureError(
                    this,
                    closureErrorEvent
            );
            case TransactionExpiredEvent transactionExpiredEvent ->
                    new TransactionExpired(this, transactionExpiredEvent);
            default -> this;
        };
    }

    /** {@inheritDoc} */
    @Override
    public TransactionStatusDto getStatus() {
        return this.getTransactionAuthorizationStatusUpdateData().getNewTransactionStatus();
    }
}
