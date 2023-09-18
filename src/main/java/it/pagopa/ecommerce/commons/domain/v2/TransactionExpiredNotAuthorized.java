package it.pagopa.ecommerce.commons.domain.v2;

import it.pagopa.ecommerce.commons.documents.v2.TransactionExpiredEvent;
import it.pagopa.ecommerce.commons.domain.v2.pojos.BaseTransaction;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * Transaction expired before requesting authorization. This is a final state so
 * any event applied to this class will be discarded.
 * </p>
 *
 * @see Transaction
 * @see BaseTransaction
 */
@EqualsAndHashCode(callSuper = true)
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public final class TransactionExpiredNotAuthorized extends BaseTransaction implements Transaction {

    private final TransactionExpiredEvent transactionExpiredEvent;

    /**
     * Primary constructor
     *
     * @param baseTransaction         the base transaction
     * @param transactionExpiredEvent the transaction expired event
     */
    public TransactionExpiredNotAuthorized(
            BaseTransaction baseTransaction,
            TransactionExpiredEvent transactionExpiredEvent
    ) {
        super(
                baseTransaction.getTransactionId(),
                baseTransaction.getPaymentNotices(),
                baseTransaction.getEmail(),
                baseTransaction.getCreationDate(),
                baseTransaction.getClientId()
        );
        this.transactionExpiredEvent = transactionExpiredEvent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransactionStatusDto getStatus() {
        return TransactionStatusDto.EXPIRED_NOT_AUTHORIZED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transaction applyEvent(Object event) {
        return this;
    }
}
