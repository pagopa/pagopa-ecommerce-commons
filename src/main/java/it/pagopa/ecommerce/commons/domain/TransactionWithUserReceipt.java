package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionWithCompletedAuthorization;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;

/**
 * <p>
 * Transaction closed and notified to the user, but with possible failure in
 * notification.
 * </p>
 * <p>
 * Given that this is a terminal state for a transaction, there are no events
 * that you can meaningfully apply to it. Any event application is thus ignored.
 *
 * @see Transaction
 * @see BaseTransactionWithCompletedAuthorization
 */

public final class TransactionWithUserReceipt extends BaseTransactionWithCompletedAuthorization implements Transaction {

    /**
     * Main constructor.
     *
     * @param baseTransaction transaction to extend with receipt data
     */
    public TransactionWithUserReceipt(
            BaseTransactionWithCompletedAuthorization baseTransaction
    ) {
        super(baseTransaction, baseTransaction.getTransactionAuthorizationStatusUpdateData());
    }

    @Override
    public Transaction applyEvent(Object event) {
        return this;
    }

    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.NOTIFIED;
    }
}
