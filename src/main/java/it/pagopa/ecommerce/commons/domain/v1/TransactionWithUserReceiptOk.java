package it.pagopa.ecommerce.commons.domain.v1;

import it.pagopa.ecommerce.commons.documents.v1.TransactionUserReceiptAddedEvent;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionWithCompletedAuthorization;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

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
@EqualsAndHashCode(callSuper = true)
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public final class TransactionWithUserReceiptOk extends BaseTransactionWithCompletedAuthorization
        implements Transaction {

    TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent;

    /**
     * Main constructor.
     *
     * @param baseTransaction                  transaction to extend with receipt
     *                                         data
     * @param transactionUserReceiptAddedEvent transaction user receipt added event
     */
    public TransactionWithUserReceiptOk(
            BaseTransactionWithCompletedAuthorization baseTransaction,
            TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent
    ) {
        super(baseTransaction, baseTransaction.getTransactionAuthorizationCompletedData());
        this.transactionUserReceiptAddedEvent = transactionUserReceiptAddedEvent;
    }

    @Override
    public Transaction applyEvent(Object event) {
        return this;
    }

    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.NOTIFIED_OK;
    }
}
