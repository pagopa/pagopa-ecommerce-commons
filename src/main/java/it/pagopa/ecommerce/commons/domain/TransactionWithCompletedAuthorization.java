package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.documents.TransactionAuthorizationStatusUpdateData;
import it.pagopa.ecommerce.commons.documents.TransactionClosureErrorEvent;
import it.pagopa.ecommerce.commons.documents.TransactionClosureSentEvent;
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
        if (event instanceof TransactionClosureSentEvent closureSentEvent) {
            return new TransactionClosed(
                    this.withStatus(closureSentEvent.getData().getNewTransactionStatus()),
                    closureSentEvent.getData()
            );
        } else if (event instanceof TransactionClosureErrorEvent closureErrorEvent) {
            return new TransactionWithClosureError(
                    this.withStatus(TransactionStatusDto.CLOSURE_ERROR),
                    closureErrorEvent
            );
        } else {
            return this;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Change the transaction status
     */
    @Override
    public TransactionWithCompletedAuthorization withStatus(TransactionStatusDto status) {
        return new TransactionWithCompletedAuthorization(
                new TransactionWithRequestedAuthorization(
                        new TransactionActivated(
                                this.getTransactionId(),
                                this.getPaymentNotices(),
                                this.getEmail(),
                                this.getTransactionActivatedData().getFaultCode(),
                                this.getTransactionActivatedData().getFaultCodeString(),
                                this.getCreationDate(),
                                status,
                                this.getClientId()
                        ),
                        this.getTransactionAuthorizationRequestData()
                ),
                this.getTransactionAuthorizationStatusUpdateData()
        );
    }
}
