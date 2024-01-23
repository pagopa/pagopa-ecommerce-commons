package it.pagopa.ecommerce.commons.domain.v2;

import it.pagopa.ecommerce.commons.documents.v2.*;
import it.pagopa.ecommerce.commons.domain.v2.pojos.BaseTransactionClosed;
import it.pagopa.ecommerce.commons.domain.v2.pojos.BaseTransactionWithClosureRequested;
import it.pagopa.ecommerce.commons.domain.v2.pojos.BaseTransactionWithCompletedAuthorization;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * Closure requested transaction.
 * </p>
 * Applicable events with resulting aggregates are:
 * <ul>
 * <li>{@link TransactionExpiredEvent} -->
 * {@link it.pagopa.ecommerce.commons.domain.v2.TransactionExpired}</li>
 * <li>{@link TransactionRefundRequestedEvent} -->
 * {@link TransactionWithRefundRequested}</li>
 * <li>{@link TransactionWithClosureRequested} -->
 * {@link it.pagopa.ecommerce.commons.domain.v2.TransactionClosed}</li>
 * </ul>
 * Any other event than the above ones will be discarded.
 *
 * @see it.pagopa.ecommerce.commons.domain.v2.Transaction
 * @see BaseTransactionClosed
 */
@EqualsAndHashCode(callSuper = true)
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public final class TransactionWithClosureRequested extends BaseTransactionWithClosureRequested implements Transaction {

    /**
     * Primary constructor
     *
     * @param baseTransaction base transaction
     */
    public TransactionWithClosureRequested(
            BaseTransactionWithCompletedAuthorization baseTransaction
    ) {
        super(baseTransaction);
    }

    @Override
    public Transaction applyEvent(Object event) {
        boolean wasTransactionAuthorized = wasTransactionAuthorized();
        return switch (event) {
            case TransactionClosedEvent e -> {
                if (wasTransactionAuthorized) {
                    yield new TransactionClosed(this, e);
                } else {
                    yield this;
                }
            }
            case TransactionClosureErrorEvent e -> new TransactionWithClosureError(
                    this,
                    e
            );
            case TransactionClosureFailedEvent e -> {
                if (!wasTransactionAuthorized) {
                    yield new TransactionUnauthorized(this, e);
                } else {
                    yield this;
                }
            }
            case TransactionExpiredEvent e -> new TransactionExpired(this, e);
            default -> this;
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.CLOSED;
    }
}
