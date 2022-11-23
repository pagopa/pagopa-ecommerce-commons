package it.pagopa.ecommerce.commons.domain.pojos;

import it.pagopa.ecommerce.commons.annotations.AggregateRootId;
import it.pagopa.ecommerce.commons.domain.*;
import it.pagopa.ecommerce.commons.generated.transactions.model.TransactionStatusDto;
import java.time.ZonedDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;

/** POJO meant to serve as a base layer for transaction attributes */
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
