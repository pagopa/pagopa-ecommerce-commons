package it.pagopa.ecommerce.commons.documents;

import static java.time.ZonedDateTime.now;

import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import it.pagopa.ecommerce.commons.domain.TransactionEventCode;

import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
public abstract sealed class TransactionEvent<T> permits TransactionActivatedEvent,TransactionActivationRequestedEvent,TransactionAuthorizationRequestedEvent,TransactionAuthorizationStatusUpdatedEvent,TransactionClosureErrorEvent,TransactionClosureSentEvent,TransactionExpiredEvent,TransactionRefundedEvent,TransactionUserReceiptAddedEvent {

    @Id
    private String id;

    @PartitionKey
    private String transactionId;

    List<NoticeCode> noticeCodes;
    private TransactionEventCode eventCode;
    private String creationDate;
    private T data;

    TransactionEvent(
            String transactionId,
            List<NoticeCode> noticeCodes,
            TransactionEventCode eventCode,
            String creationDate,
            T data
    ) {
        this.id = UUID.randomUUID().toString();
        this.transactionId = transactionId;
        this.noticeCodes = noticeCodes;
        this.eventCode = eventCode;
        this.data = data;
        this.creationDate = creationDate;
    }

    TransactionEvent(
            String transactionId,
            List<NoticeCode> noticeCodes,
            TransactionEventCode eventCode,
            T data
    ) {
        this.id = UUID.randomUUID().toString();
        this.transactionId = transactionId;
        this.noticeCodes = noticeCodes;
        this.eventCode = eventCode;
        this.data = data;
        this.creationDate = now().toString();
    }
}
