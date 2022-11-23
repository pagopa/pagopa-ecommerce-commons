package it.pagopa.ecommerce.commons.documents;

import static java.time.ZonedDateTime.now;

import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import it.pagopa.ecommerce.commons.domain.TransactionEventCode;
import java.util.UUID;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "eventstore")
@Generated
@NoArgsConstructor
@ToString
public abstract sealed class TransactionEvent<T> permits TransactionActivationRequestedEvent,TransactionActivatedEvent,TransactionAuthorizationRequestedEvent,TransactionAuthorizationStatusUpdatedEvent,TransactionClosureErrorEvent,TransactionClosureSentEvent,TransactionUserReceiptAddedEvent {

    @Id
    private String id;

    @PartitionKey
    private String transactionId;

    private String rptId;
    private String paymentToken;
    private TransactionEventCode eventCode;
    private String creationDate;
    private T data;

    TransactionEvent(
            String transactionId,
            String rptId,
            String paymentToken,
            TransactionEventCode eventCode,
            String creationDate,
            T data
    ) {
        this.id = UUID.randomUUID().toString();
        this.transactionId = transactionId;
        this.rptId = rptId;
        this.eventCode = eventCode;
        this.paymentToken = paymentToken;
        this.data = data;
        this.creationDate = creationDate;
    }

    TransactionEvent(
            String transactionId,
            String rptId,
            String paymentToken,
            TransactionEventCode eventCode,
            T data
    ) {
        this.id = UUID.randomUUID().toString();
        this.transactionId = transactionId;
        this.rptId = rptId;
        this.eventCode = eventCode;
        this.paymentToken = paymentToken;
        this.data = data;
        this.creationDate = now().toString();
    }
}
