package it.pagopa.ecommerce.commons.domain.v1.pojos;

import it.pagopa.ecommerce.commons.annotations.AggregateRootId;
import it.pagopa.ecommerce.commons.documents.v1.Transaction.ClientId;
import it.pagopa.ecommerce.commons.documents.v1.TransactionActivatedEvent;
import it.pagopa.ecommerce.commons.domain.Confidential;
import it.pagopa.ecommerce.commons.domain.Email;
import it.pagopa.ecommerce.commons.domain.PaymentNotice;
import it.pagopa.ecommerce.commons.domain.TransactionId;
import it.pagopa.ecommerce.commons.domain.v1.Transaction;
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
 * Each POJO in the {@link it.pagopa.ecommerce.commons.domain.v1.pojos} package
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
@AllArgsConstructor
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
     * Retrieves the current transaction status
     *
     * @return the transaction status
     */
    public abstract TransactionStatusDto getStatus();
}
