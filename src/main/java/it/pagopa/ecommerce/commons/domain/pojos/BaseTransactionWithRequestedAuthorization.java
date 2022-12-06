package it.pagopa.ecommerce.commons.domain.pojos;

import it.pagopa.ecommerce.commons.documents.TransactionAuthorizationRequestData;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * POJO for a transaction with an authorization request. This POJOs adds
 * information about authorization such as the chosen payment method, the
 * associated fee, etc.
 * </p>
 * <p>
 * All accrued data is exposed through the same structure generated by
 * {@link it.pagopa.ecommerce.commons.documents.TransactionAuthorizationRequestedEvent
 * TransactionAuthorizationRequestedEvent}, namely
 * {@link it.pagopa.ecommerce.commons.documents.TransactionAuthorizationRequestData
 * TransactionAuthorizationRequestData}.
 * </p>
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public abstract class BaseTransactionWithRequestedAuthorization extends BaseTransactionWithPaymentToken {

    TransactionAuthorizationRequestData transactionAuthorizationRequestData;

    /**
     * Main constructor.
     *
     * @param baseTransaction                     transaction to extend with
     *                                            authorization data
     * @param transactionAuthorizationRequestData authorization data
     */
    protected BaseTransactionWithRequestedAuthorization(
            BaseTransactionWithPaymentToken baseTransaction,
            TransactionAuthorizationRequestData transactionAuthorizationRequestData
    ) {
        super(baseTransaction, baseTransaction.getTransactionActivatedData());
        this.transactionAuthorizationRequestData = transactionAuthorizationRequestData;
    }
}
