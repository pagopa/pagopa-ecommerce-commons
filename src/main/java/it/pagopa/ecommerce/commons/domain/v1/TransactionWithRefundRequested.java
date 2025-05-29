package it.pagopa.ecommerce.commons.domain.v1;

import it.pagopa.ecommerce.commons.documents.v1.TransactionRefundErrorEvent;
import it.pagopa.ecommerce.commons.documents.v1.TransactionRefundRequestedEvent;
import it.pagopa.ecommerce.commons.documents.v1.TransactionRefundRetriedEvent;
import it.pagopa.ecommerce.commons.documents.v1.TransactionRefundedEvent;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionWithRefundRequested;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionWithRequestedAuthorization;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * Transaction authorized for which a refund is requested (failure in Nodo
 * closePayment with response outcome KO or sendPaymentResult with outcome KO)
 * </p>
 * <p>
 * Applicable events with resulting aggregates are:
 * <ul>
 * <li>{@link TransactionRefundRetriedEvent} -->
 * {@link TransactionWithRefundError}</li>
 * <li>{@link TransactionRefundedEvent} --> {@link TransactionRefunded}</li>
 * </ul>
 *
 * @see Transaction
 * @see BaseTransactionWithRefundRequested
 */
@EqualsAndHashCode(callSuper = true)
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public final class TransactionWithRefundRequested extends BaseTransactionWithRefundRequested
        implements Transaction {

    TransactionRefundRequestedEvent transactionRefundRequestedEvent;

    /**
     * Main constructor.
     *
     * @param baseTransaction                 transaction to extend with receipt
     *                                        data
     * @param transactionRefundRequestedEvent transaction refund requested event
     */
    public TransactionWithRefundRequested(
            BaseTransactionWithRequestedAuthorization baseTransaction,
            TransactionRefundRequestedEvent transactionRefundRequestedEvent
    ) {
        super(baseTransaction);
        this.transactionRefundRequestedEvent = transactionRefundRequestedEvent;
    }

    @Override
    public Transaction applyEvent(Object event) {
        if (event instanceof TransactionRefundedEvent) {
            return new TransactionRefunded(this, (TransactionRefundedEvent) event);
        } else if (event instanceof TransactionRefundErrorEvent) {
            return new TransactionWithRefundError(this, (TransactionRefundErrorEvent) event);
        } else {
            return this;
        }
    }

    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.REFUND_REQUESTED;
    }
}
