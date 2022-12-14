package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.documents.TransactionAuthorizationRequestData;
import it.pagopa.ecommerce.commons.documents.TransactionAuthorizationStatusUpdatedEvent;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionWithPaymentToken;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionWithRequestedAuthorization;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Transaction with a requested authorization.
 * </p>
 * <p>
 * To this class you can apply an
 * {@link it.pagopa.ecommerce.commons.documents.TransactionAuthorizationStatusUpdatedEvent}
 * to get a
 * {@link it.pagopa.ecommerce.commons.domain.TransactionWithCompletedAuthorization}
 * </p>
 *
 * @see Transaction
 * @see BaseTransactionWithRequestedAuthorization
 */
@EqualsAndHashCode(callSuper = true)
public final class TransactionWithRequestedAuthorization extends BaseTransactionWithRequestedAuthorization
        implements
        Transaction {

    /**
     * Primary constructor
     *
     * @param transaction              base transaction
     * @param authorizationRequestData data related to authorization request
     */
    public TransactionWithRequestedAuthorization(
            BaseTransactionWithPaymentToken transaction,
            TransactionAuthorizationRequestData authorizationRequestData
    ) {
        super(transaction, authorizationRequestData);
    }

    /** {@inheritDoc} */
    @Override
    public Transaction applyEvent(Object event) {
        if (event instanceof TransactionAuthorizationStatusUpdatedEvent authorizationStatusUpdatedEvent) {
            return new TransactionWithCompletedAuthorization(
                    this.withStatus(
                            TransactionStatusDto.fromValue(
                                    authorizationStatusUpdatedEvent.getData().getNewTransactionStatus().toString()
                            )
                    ),
                    authorizationStatusUpdatedEvent.getData()
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
    public TransactionWithRequestedAuthorization withStatus(TransactionStatusDto status) {
        return new TransactionWithRequestedAuthorization(
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
        );
    }
}
