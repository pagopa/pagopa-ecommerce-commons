package it.pagopa.ecommerce.commons.domain.pojos;

import it.pagopa.ecommerce.commons.documents.TransactionAuthorizationRequestData;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@ToString
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public abstract class BaseTransactionWithRequestedAuthorization extends BaseTransactionWithPaymentToken {

    TransactionAuthorizationRequestData transactionAuthorizationRequestData;

    protected BaseTransactionWithRequestedAuthorization(
            BaseTransactionWithPaymentToken baseTransaction,
            TransactionAuthorizationRequestData transactionAuthorizationRequestData
    ) {
        super(baseTransaction, baseTransaction.getTransactionActivatedData());
        this.transactionAuthorizationRequestData = transactionAuthorizationRequestData;
    }
}
