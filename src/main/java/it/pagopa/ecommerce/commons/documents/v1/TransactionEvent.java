package it.pagopa.ecommerce.commons.documents.v1;

import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import it.pagopa.ecommerce.commons.domain.v1.TransactionEventCode;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

import static java.time.ZonedDateTime.now;

/**
 * Hierarchy root for transaction events.
 *
 * @param <T> additional data type
 */
@Data
@Document(collection = "eventstore")
@Generated
@NoArgsConstructor
@ToString
public abstract sealed class TransactionEvent<T> permits BaseTransactionClosureEvent,TransactionActivatedEvent,TransactionAuthorizationCompletedEvent,TransactionAuthorizationRequestedEvent,TransactionClosureRetriedEvent,TransactionExpiredEvent,TransactionRefundRetriedEvent,TransactionRefundedEvent,TransactionUserCanceledEvent,TransactionUserReceiptAddedEvent {

    @Id
    private String id;

    @PartitionKey
    private String transactionId;

    private TransactionEventCode eventCode;
    private String creationDate;
    private T data;

    TransactionEvent(
            String transactionId,
            TransactionEventCode eventCode,
            String creationDate,
            T data
    ) {
        this.id = UUID.randomUUID().toString();
        this.transactionId = transactionId;
        this.eventCode = eventCode;
        this.data = data;
        this.creationDate = creationDate;
    }

    TransactionEvent(
            String transactionId,
            TransactionEventCode eventCode,
            T data
    ) {
        this.id = UUID.randomUUID().toString();
        this.transactionId = transactionId;
        this.eventCode = eventCode;
        this.data = data;
        this.creationDate = now().toString();
    }
}
