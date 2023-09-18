package it.pagopa.ecommerce.commons.domain.v2.pojos;

import it.pagopa.ecommerce.commons.documents.v2.TransactionExpiredData;
import it.pagopa.ecommerce.commons.documents.v2.TransactionExpiredEvent;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * POJO for an expired transaction.
 * </p>
 * <p>
 * All accrued data is exposed through the same structure generated by
 * {@link TransactionExpiredEvent TransactionExpiredEvent}, namely
 * {@link TransactionExpiredData TransactionExpiredData}.
 * </p>
 *
 * @see BaseTransactionWithRequestedAuthorization
 * @see TransactionExpiredEvent
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public abstract class BaseTransactionExpired extends BaseTransactionWithRequestedAuthorization {
    TransactionExpiredData transactionExpiredData;
    BaseTransactionWithRequestedAuthorization transactionAtPreviousState;

    /**
     * Primary constructor
     *
     * @param baseTransaction        base transaction
     * @param transactionExpiredData transaction expiration data
     */
    protected BaseTransactionExpired(
            BaseTransactionWithRequestedAuthorization baseTransaction,
            TransactionExpiredData transactionExpiredData
    ) {
        super(
                baseTransaction,
                baseTransaction.getTransactionAuthorizationRequestData()
        );

        this.transactionExpiredData = transactionExpiredData;
        this.transactionAtPreviousState = baseTransaction;
    }
}
