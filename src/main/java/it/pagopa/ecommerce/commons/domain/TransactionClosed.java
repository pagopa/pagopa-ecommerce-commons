package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.documents.TransactionClosureSendData;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionClosed;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionWithCompletedAuthorization;
import it.pagopa.generated.transactions.server.model.TransactionStatusDto;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Closed transaction.
 * </p>
 * <p>
 * Given that this is a terminal state for a transaction, there are no events
 * that you can meaningfully apply to it. Any event application is thus ignored.
 * </p>
 *
 * @see Transaction
 * @see BaseTransactionWithCompletedAuthorization
 */
@EqualsAndHashCode(callSuper = true)
public final class TransactionClosed extends BaseTransactionClosed implements Transaction {

    /**
     * Primary constructor
     *
     * @param baseTransaction      base transaction
     * @param closureSentEventData data related to closure sending event
     */
    public TransactionClosed(
            BaseTransactionWithCompletedAuthorization baseTransaction,
            TransactionClosureSendData closureSentEventData
    ) {
        super(baseTransaction, closureSentEventData);
    }

    @Override
    public Transaction applyEvent(Object event) {
        return this;
    }

    /**
     * Change the transaction status
     *
     * @param status new status
     * @return a new transaction with the same data except for the status
     */
    @Override
    public TransactionClosed withStatus(TransactionStatusDto status) {
        return new TransactionClosed(
                new TransactionWithCompletedAuthorization(
                        new TransactionWithRequestedAuthorization(
                                new TransactionActivated(
                                        this.getTransactionId(),
                                        new PaymentToken(this.getTransactionActivatedData().getPaymentToken()),
                                        this.getRptId(),
                                        this.getDescription(),
                                        this.getAmount(),
                                        this.getEmail(),
                                        this.getTransactionActivatedData().getFaultCode(),
                                        this.getTransactionActivatedData().getFaultCodeString(),
                                        this.getCreationDate(),
                                        status
                                ),
                                this.getTransactionAuthorizationRequestData()
                        ),
                        this.getTransactionAuthorizationStatusUpdateData()
                ),
                this.getTransactionClosureSendData()
        );
    }
}
