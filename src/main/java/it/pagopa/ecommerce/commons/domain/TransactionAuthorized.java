package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.documents.TransactionAuthorizedData;
import it.pagopa.ecommerce.commons.documents.TransactionClosureErrorEvent;
import it.pagopa.ecommerce.commons.documents.TransactionClosedEvent;
import it.pagopa.ecommerce.commons.documents.TransactionExpiredEvent;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionAuthorized;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionWithRequestedAuthorization;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Transaction with a successful authorization.
 * </p>
 * <p>
 * To this class you can apply either a
 * {@link TransactionClosedEvent} to
 * get a {@link it.pagopa.ecommerce.commons.domain.TransactionClosed} or a
 * {@link it.pagopa.ecommerce.commons.documents.TransactionClosureErrorEvent} to
 * get a {@link it.pagopa.ecommerce.commons.domain.TransactionWithClosureError}
 * </p>
 *
 * @see Transaction
 * @see BaseTransactionAuthorized
 */
@EqualsAndHashCode(callSuper = true)
public final class TransactionAuthorized extends BaseTransactionAuthorized implements Transaction {
    /**
     * Primary constructor
     *
     * @param baseTransaction           base transaction
     * @param transactionAuthorizedData transaction authorization data
     */
    public TransactionAuthorized(
            BaseTransactionWithRequestedAuthorization baseTransaction,
            TransactionAuthorizedData transactionAuthorizedData
    ) {
        super(baseTransaction, transactionAuthorizedData);
    }

    @Override
    public Transaction applyEvent(Object event) {
        return switch (event) {
            case TransactionClosedEvent closureSentEvent -> new TransactionClosed(
                    this,
                    closureSentEvent.getData()
            );
            case TransactionClosureErrorEvent closureErrorEvent -> new TransactionWithClosureError(
                    this,
                    closureErrorEvent
            );
            case TransactionExpiredEvent transactionExpiredEvent ->
                    new TransactionExpired(this, transactionExpiredEvent);
            default -> this;
        };
    }

    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.AUTHORIZED;
    }
}
