package it.pagopa.ecommerce.commons.domain.pojos;

import it.pagopa.ecommerce.commons.documents.TransactionClosureSendData;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@ToString
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public abstract class BaseTransactionClosed extends BaseTransactionWithCompletedAuthorization {

    TransactionClosureSendData transactionClosureSendData;

    protected BaseTransactionClosed(
            BaseTransactionWithCompletedAuthorization baseTransaction,
            TransactionClosureSendData transactionClosureSendData
    ) {
        super(baseTransaction, baseTransaction.getTransactionAuthorizationStatusUpdateData());
        this.transactionClosureSendData = transactionClosureSendData;
    }
}
