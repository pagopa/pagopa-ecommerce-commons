package it.pagopa.ecommerce.commons.domain.v2.pojos;

import it.pagopa.ecommerce.commons.documents.v2.TransactionClosureData;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * POJO for a closed transaction
 * </p>
 *
 * @see BaseTransactionWithClosureRequested
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
/*
 * @formatter:off
 *
 * Warning java:S110 - This class has x parents which is greater than 5 authorized
 * Suppressed because the Transaction hierarchy modeled here force BaseTransactionClosed
 * to be instantiated only starting from a TransactionClosureRequested. The hierarchy dept is strictly correlated
 * to the depth of the graph representing the finite state machine so can be accepted that hierarchy level
 * is deeper than the max authorized level
 *
 * @formatter:on
 */
@SuppressWarnings("java:S110")
public abstract class BaseTransactionClosed extends BaseTransactionWithClosureRequested {
    TransactionClosureData transactionClosureData;

    /**
     * Primary constructor
     *
     * @param baseTransaction        base transaction
     * @param transactionClosureData transaction closure data
     */
    protected BaseTransactionClosed(
            BaseTransactionWithClosureRequested baseTransaction,
            TransactionClosureData transactionClosureData
    ) {
        super(
                baseTransaction
        );

        this.transactionClosureData = transactionClosureData;
    }
}
