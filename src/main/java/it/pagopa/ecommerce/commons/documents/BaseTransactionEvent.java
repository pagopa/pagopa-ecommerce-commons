package it.pagopa.ecommerce.commons.documents;

import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * <p>
 * Base class for all eCommerce events, regardless of event version.
 * </p>
 *
 * @param <T> type parameter for additional event data
 */
@Data
@NoArgsConstructor
@Document(collection = "eventstore")
public abstract class BaseTransactionEvent<T> {
    @Id
    private String id;

    @PartitionKey
    private String transactionId;

    private String creationDate;

    private T data;

    private String eventCode;

    /**
     * Primary constructor for a base transaction event.
     *
     * @param id            the unique ID of the event document
     * @param transactionId the transaction identifier
     * @param creationDate  the creation timestamp for the event
     * @param data          the specific data payload for this event
     * @param eventCode     the unique code identifying the event type
     */
    public BaseTransactionEvent(
            String id,
            String transactionId,
            String creationDate,
            T data,
            String eventCode
    ) {
        this.id = id;
        this.transactionId = transactionId;
        this.creationDate = creationDate;
        this.data = data;
        this.eventCode = eventCode;
    }
}
