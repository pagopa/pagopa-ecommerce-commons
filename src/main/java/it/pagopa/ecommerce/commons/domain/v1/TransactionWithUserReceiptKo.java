package it.pagopa.ecommerce.commons.domain.v1;

import it.pagopa.ecommerce.commons.documents.v1.TransactionRefundRequestedEvent;
import it.pagopa.ecommerce.commons.documents.v1.TransactionUserReceiptAddedEvent;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionClosed;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionWithCompletedAuthorization;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionWithUserReceipt;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * Transaction closed and notified to the user. This state is reached when Nodo
 * sendPaymentResult has KO outcome
 * </p>
 * <p>
 * Starting from this state the only applicable event is
 * {@link TransactionRefundRequestedEvent} to start the refund process
 *
 * @see Transaction
 * @see BaseTransactionWithCompletedAuthorization
 */
@EqualsAndHashCode(callSuper = true)
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public final class TransactionWithUserReceiptKo extends BaseTransactionWithUserReceipt
        implements Transaction {

    /**
     * Main constructor.
     *
     * @param baseTransaction                  transaction to extend with receipt
     *                                         data
     * @param transactionUserReceiptAddedEvent transaction user receipt added event
     */
    public TransactionWithUserReceiptKo(
            BaseTransactionClosed baseTransaction,
            TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent
    ) {
        super(baseTransaction, transactionUserReceiptAddedEvent.getData());
    }

    @Override
    public Transaction applyEvent(Object event) {
        if (event instanceof TransactionRefundRequestedEvent e) {
            return new TransactionWithRefundRequested(this, e);
        }
        return this;

    }

    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.NOTIFIED_KO;
    }
}
