package it.pagopa.ecommerce.commons.domain.v1;

import it.pagopa.ecommerce.commons.documents.v1.TransactionClosedEvent;
import it.pagopa.ecommerce.commons.documents.v1.TransactionClosureErrorEvent;
import it.pagopa.ecommerce.commons.documents.v1.TransactionExpiredEvent;
import it.pagopa.ecommerce.commons.documents.v1.TransactionUserCanceledEvent;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransaction;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * Transaction with cancellation requested by user. This is a transient state.
 * Applicable events with resulting aggregates are:
 * {@link TransactionClosedEvent} --> {@link TransactionUserCanceled}
 * {@link TransactionExpiredEvent} --> {@link TransactionExpired}
 * </p>
 *
 * @see Transaction
 * @see BaseTransaction
 */
@EqualsAndHashCode(callSuper = true)
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public final class TransactionWithCancellationRequested extends BaseTransaction implements Transaction {

    TransactionUserCanceledEvent transactionUserCanceledEvent;

    /**
     * Primary constructor
     *
     * @param baseTransaction              the base transaction
     * @param transactionUserCanceledEvent the transaction expired event
     */
    public TransactionWithCancellationRequested(
            BaseTransaction baseTransaction,
            TransactionUserCanceledEvent transactionUserCanceledEvent
    ) {
        super(
                baseTransaction.getTransactionId(),
                baseTransaction.getPaymentNotices(),
                baseTransaction.getEmail(),
                baseTransaction.getCreationDate(),
                baseTransaction.getClientId()
        );
        this.transactionUserCanceledEvent = transactionUserCanceledEvent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.CANCELLATION_REQUESTED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transaction applyEvent(Object event) {
        return switch (event) {
            case TransactionClosedEvent e -> new TransactionUserCanceled(this, e);
            case TransactionClosureErrorEvent e -> new TransactionWithClosureError(this, e);
            case TransactionExpiredEvent e -> new TransactionExpired(this, e);
            default -> this;
        };

    }
}
