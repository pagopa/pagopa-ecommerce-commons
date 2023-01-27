package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.documents.TransactionAddReceiptData;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionClosed;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionWithUserReceipt;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
/**
* <p>
 * Transaction closed and notified to the user, but with possible failure in notification.
 * </p>
 * <p>
 * Given that this is a terminal state for a transaction, there are no events
 * that you can meaningfully apply to it. Any event application is thus ignored.
 *
 * @see Transaction
 * @see BaseTransactionClosed
*/


public final class TransactionWithUserReceipt extends BaseTransactionWithUserReceipt implements Transaction {

    /**
     * Main constructor.
     *
     * @param baseTransaction           transaction to extend with
     *                                  authorization data
     * @param transactionAddReceiptData authorization data
     */
    public TransactionWithUserReceipt(BaseTransactionClosed baseTransaction, TransactionAddReceiptData transactionAddReceiptData) {
        super(baseTransaction, transactionAddReceiptData);
    }

    @Override
    public Transaction applyEvent(Object event) {
        return this;
    }

    @Override
    public TransactionStatusDto getStatus() {
        return this.getTransactionAddReceiptData().getNewTransactionStatus();
    }
}
