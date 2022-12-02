package it.pagopa.ecommerce.commons.domain.pojos;

import it.pagopa.ecommerce.commons.generated.events.v1.TransactionActivatedData;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * POJO for an activated transaction. As the name implies, the only field added
 * is the payment token.
 * </p>
 * <p>
 * For simplicity we currently include all data associated to
 * {@link it.pagopa.ecommerce.commons.generated.events.v1.TransactionActivatedEvent
 * TransactionActivatedEvent}.
 * </p>
 * <p>
 * To get the payment token, call
 * {@code BaseTransaction.getTransactionActivatedData().getPaymentToken()}
 * </p>
 *
 * @see it.pagopa.ecommerce.commons.domain.pojos.BaseTransaction
 * @see it.pagopa.ecommerce.commons.generated.events.v1.TransactionActivatedEvent
 *      TransactionActivatedEvent
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public abstract class BaseTransactionWithPaymentToken extends BaseTransaction {

    TransactionActivatedData transactionActivatedData;

    /**
     * Main constructor.
     *
     * @param baseTransaction          base transaction to be extended
     * @param transactionActivatedData data generated with the activation event
     */
    protected BaseTransactionWithPaymentToken(
            BaseTransaction baseTransaction,
            TransactionActivatedData transactionActivatedData
    ) {
        super(
                baseTransaction.getTransactionId(),
                baseTransaction.getRptId(),
                baseTransaction.getDescription(),
                baseTransaction.getAmount(),
                baseTransaction.getEmail(),
                baseTransaction.getCreationDate(),
                baseTransaction.getStatus()
        );
        this.transactionActivatedData = transactionActivatedData;
    }
}
