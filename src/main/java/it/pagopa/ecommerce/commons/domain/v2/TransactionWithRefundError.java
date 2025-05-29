package it.pagopa.ecommerce.commons.domain.v2;

import it.pagopa.ecommerce.commons.documents.v2.TransactionRefundErrorEvent;
import it.pagopa.ecommerce.commons.documents.v2.TransactionRefundRetriedEvent;
import it.pagopa.ecommerce.commons.documents.v2.TransactionRefundedEvent;
import it.pagopa.ecommerce.commons.documents.v2.authorization.TransactionGatewayAuthorizationData;
import it.pagopa.ecommerce.commons.domain.v2.pojos.BaseTransactionWithRefundRequested;
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
 * <ul>
 * <li>{@link TransactionRefundedEvent} --> {@link TransactionRefunded}</li>
 * </ul>
 * Other events than the above ones will be discarded
 *
 * @see Transaction
 * @see BaseTransactionWithRefundRequested
 */
@EqualsAndHashCode(callSuper = true)
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public final class TransactionWithRefundError extends BaseTransactionWithRefundRequested implements Transaction {

    TransactionRefundErrorEvent transactionRefundErrorEvent;

    /**
     * Main constructor.
     *
     * @param baseTransaction             base transaction with refund requested
     * @param transactionRefundErrorEvent transaction refund error event
     */
    public TransactionWithRefundError(
            BaseTransactionWithRefundRequested baseTransaction,
            TransactionRefundErrorEvent transactionRefundErrorEvent
    ) {
        super(baseTransaction, baseTransaction.getRefundRequestedAuthorizationGatewayData());
        this.transactionRefundErrorEvent = transactionRefundErrorEvent;
    }

    /**
     * Convenience constructor used to add gateway data on refund retry event
     *
     * @param transactionWithRefundError          the transaction with refund error
     *                                            instance from which clone data
     * @param transactionGatewayAuthorizationData transaction gateway authorization
     *                                            data
     */
    public TransactionWithRefundError(
            TransactionWithRefundError transactionWithRefundError,
            TransactionGatewayAuthorizationData transactionGatewayAuthorizationData
    ) {
        super(transactionWithRefundError.getTransactionAtPreviousState(), transactionGatewayAuthorizationData);
        this.transactionRefundErrorEvent = transactionWithRefundError.transactionRefundErrorEvent;
    }

    @Override
    public Transaction applyEvent(Object event) {
        if (event instanceof TransactionRefundedEvent) {
            return new TransactionRefunded(this, (TransactionRefundedEvent) event);
        } else if (event instanceof TransactionRefundRetriedEvent e) {
            return new TransactionWithRefundError(this, e.getData().getTransactionGatewayAuthorizationData());
        } else {
            return this;
        }
    }

    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.REFUND_ERROR;
    }
}
