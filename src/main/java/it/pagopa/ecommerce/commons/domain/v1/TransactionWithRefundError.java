package it.pagopa.ecommerce.commons.domain.v1;

import it.pagopa.ecommerce.commons.documents.v1.TransactionRefundErrorEvent;
import it.pagopa.ecommerce.commons.documents.v1.TransactionRefundedEvent;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionRefunded;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionWithRefundRequested;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * Transaction authorized for which a refund operation is applied
 * unsuccessfully. This state can be both final and non-final. In fact, starting
 * from this state, a configurable number of attempts are performed for
 * transaction refund process to be completed and transaction can ends up into
 * this state if no more attempts are available for refund operation to be done
 * </p>
 * <p>
 * Applicable events with resulting aggregates are:
 * {@link TransactionRefundedEvent} --> {@link TransactionRefunded}
 *
 * @see Transaction
 * @see BaseTransactionRefunded
 */
@EqualsAndHashCode(callSuper = true)
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public final class TransactionWithRefundError extends BaseTransactionRefunded implements Transaction {

    TransactionRefundErrorEvent transactionRefundErrorEvent;

    /**
     * Main constructor.
     *
     * @param baseTransaction             transaction to extend with receipt data
     * @param transactionRefundErrorEvent transaction refund error event
     */
    public TransactionWithRefundError(
            BaseTransactionWithRefundRequested baseTransaction,
            TransactionRefundErrorEvent transactionRefundErrorEvent
    ) {
        super(baseTransaction, transactionRefundErrorEvent.getData());
        this.transactionRefundErrorEvent = transactionRefundErrorEvent;
    }

    @Override
    public Transaction applyEvent(Object event) {
        if (event instanceof TransactionRefundedEvent e) {
            return new TransactionRefunded(this, e);
        }
        return this;
    }

    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.REFUND_ERROR;
    }
}
