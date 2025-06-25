package it.pagopa.ecommerce.commons.domain.v2.pojos;

import it.pagopa.ecommerce.commons.annotations.AggregateRootId;
import it.pagopa.ecommerce.commons.documents.v2.Transaction.ClientId;
import it.pagopa.ecommerce.commons.documents.v2.TransactionActivatedEvent;
import it.pagopa.ecommerce.commons.domain.Confidential;
import it.pagopa.ecommerce.commons.domain.v2.Email;
import it.pagopa.ecommerce.commons.domain.v2.PaymentNotice;
import it.pagopa.ecommerce.commons.domain.v2.TransactionId;
import it.pagopa.ecommerce.commons.domain.v2.Transaction;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * <p>
 * Base POJO for transaction attributes.
 * </p>
 * <p>
 * Each POJO in the {@link it.pagopa.ecommerce.commons.domain.v2.pojos} package
 * corresponds logically to a transaction in a specific state (thus with a
 * specific set of attributes).
 * </p>
 * <p>
 * Given that application of events can only accumulate attributes, attribute
 * inheritance is realized via each POJO inheriting from the one corresponding
 * to the previous state (the transaction state flowchart is described in
 * {@link Transaction}).
 * </p>
 * <p>
 * This POJOs are implemented as abstract classes and are meant to be used only
 * as (a) a reference on attributes available/added from each event application,
 * or (b) a mean to access transaction attributes from a generic transaction.
 * </p>
 * <p>
 * For example, the "lowest" POJO that defines a transaction status is
 * {@code BaseTransaction}, so if you have a generic transaction you can check
 * for its status as such:
 *
 * <pre>
 * {
 *     &#64;code
 *     Transaction t;
 *     TransactionStatusDto status = ((BaseTransaction) t).getStatus();
 * }
 * </pre>
 * <p>
 * Note that because all other POJOs inherit (most of them indirectly) from
 * {@code BaseTransaction}, it does not matter which POJO class you cast to. In
 * the same way you can get other attributes by casting the transaction to other
 * POJOs.
 *
 * @see TransactionActivatedEvent
 */
@ToString
@EqualsAndHashCode
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public abstract class BaseTransaction {

    @AggregateRootId
    TransactionId transactionId;

    List<PaymentNotice> paymentNotices;
    Confidential<Email> email;
    ZonedDateTime creationDate;

    ClientId clientId;

    /**
     * Primary constructor for the BaseTransaction class.
     *
     * @param transactionId  the unique identifier for this transaction.
     * @param paymentNotices the list of payment notices associated with the
     *                       transaction.
     * @param email          the confidential email of the user.
     * @param creationDate   the timestamp of the transaction's creation.
     * @param clientId       the client ID that initiated the transaction.
     */
    public BaseTransaction(
            TransactionId transactionId,
            List<PaymentNotice> paymentNotices,
            Confidential<Email> email,
            ZonedDateTime creationDate,
            ClientId clientId
    ) {
        this.transactionId = transactionId;
        this.paymentNotices = paymentNotices;
        this.email = email;
        this.creationDate = creationDate;
        this.clientId = clientId;
    }

    /**
     * Retrieves the current transaction status
     *
     * @return the transaction status
     */
    public abstract TransactionStatusDto getStatus();
}
