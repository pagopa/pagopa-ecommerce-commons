package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.documents.TransactionAuthorizationFailedEvent;
import it.pagopa.ecommerce.commons.documents.TransactionClosureErrorEvent;
import it.pagopa.ecommerce.commons.documents.TransactionClosureSentEvent;
import it.pagopa.ecommerce.commons.documents.TransactionExpiredEvent;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionWithFailedAuthorization;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionWithRequestedAuthorization;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Transaction with a failed authorization.
 * </p>
 * <p>
 * To this class you can apply either a
 * {@link it.pagopa.ecommerce.commons.documents.TransactionClosureSentEvent} to
 * get a {@link it.pagopa.ecommerce.commons.domain.TransactionClosed} or a
 * {@link it.pagopa.ecommerce.commons.documents.TransactionClosureErrorEvent} to
 * get a {@link it.pagopa.ecommerce.commons.domain.TransactionWithClosureError}
 * </p>
 *
 * @see Transaction
 * @see BaseTransactionWithFailedAuthorization
 */
@EqualsAndHashCode(callSuper = true)
public final class TransactionWithFailedAuthorization extends BaseTransactionWithFailedAuthorization
        implements Transaction {
    /**
     * Primary constructor
     *
     * @param baseTransaction base transaction
     * @param event           transaction authorization failure event
     */
    public TransactionWithFailedAuthorization(
            BaseTransactionWithRequestedAuthorization baseTransaction,
            TransactionAuthorizationFailedEvent event
    ) {
        super(baseTransaction, event);
    }

    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.AUTHORIZATION_FAILED;
    }

    @Override
    public Transaction applyEvent(Object event) {
        return switch (event) {
            case TransactionClosureSentEvent closureSentEvent -> new TransactionClosed(
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
}
