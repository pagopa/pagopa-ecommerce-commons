package it.pagopa.ecommerce.commons.domain.pojos;

import it.pagopa.ecommerce.commons.annotations.AggregateRootId;
import it.pagopa.ecommerce.commons.domain.*;
import it.pagopa.ecommerce.commons.generated.transactions.model.TransactionStatusDto;
import java.time.ZonedDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 *     Base POJO for transaction attributes.
 * </p>
 * <p>
 *     Each POJO in the {@link it.pagopa.ecommerce.commons.domain.pojos} package corresponds logically to a transaction
 *     in a specific state (thus with a specific set of attributes).
 * </p>
 * <p>
 *     Given that application of events can only accumulate attributes, attribute inheritance is realized via each POJO
 *     inheriting from the one corresponding to the previous state (the transaction state flowchart is described in {@link Transaction}).
 * </p>
 * <p>
 *     This POJOs are implemented as abstract classes and are meant to be used only as (a) a reference on attributes
 *     available/added from each event application, or (b) a mean to access transaction attributes from a generic transaction.
 * </p>
 * <p>
 *     For example, the "lowest" POJO that defines a transaction status is {@code BaseTransaction}, so if you have a generic
 *     transaction you can check for its status as such:
 *     <pre>
 *     {@code
 *         Transaction t;
 *         TransactionStatusDto status = ((BaseTransaction) t).getStatus();
 *     }
 *     </pre>
 *     Note that because all other POJOs inherit (most of them indirectly) from {@code BaseTransaction}, it does not matter
 *     which POJO class you cast to.
 *     In the same way you can get other attributes by casting the transaction to other POJOs.
 *
 * @see it.pagopa.ecommerce.commons.generated.events.v1.TransactionActivationRequestedEvent TransactionActivationRequestedEvent
 */
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public abstract class BaseTransaction {

    @AggregateRootId
    TransactionId transactionId;

    RptId rptId;
    TransactionDescription description;
    TransactionAmount amount;
    Email email;
    ZonedDateTime creationDate;

    @With
    TransactionStatusDto status;
}
