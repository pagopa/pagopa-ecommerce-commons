package it.pagopa.ecommerce.commons.documents;

import it.pagopa.ecommerce.commons.documents.v2.deadletter.DeadLetterTransactionInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.annotation.Nullable;

/**
 * Persistence class for dead letter events
 */
@Document(collection = "dead-letter-events")
@Data
@ToString
@NoArgsConstructor
public class DeadLetterEvent {

    /**
     * Event unique id
     */
    @Id
    private String id;
    /**
     * Queue name from which dead letter event has been read
     */
    private String queueName;
    /**
     * Event writing date
     */
    private String insertionDate;
    /**
     * Dead letter unhandled read data
     */
    private String data;
    /**
     * Transaction info data
     */
    @Nullable
    private DeadLetterTransactionInfo transactionInfo;

    /**
     * All-args constructor
     *
     * @param id              the event unique id
     * @param queueName       the queue name
     * @param insertionDate   the event insertion date
     * @param data            the event data
     * @param transactionInfo the transaction info
     */
    public DeadLetterEvent(
            String id,
            String queueName,
            String insertionDate,
            String data,
            DeadLetterTransactionInfo transactionInfo
    ) {
        this.id = id;
        this.queueName = queueName;
        this.insertionDate = insertionDate;
        this.data = data;
        this.transactionInfo = transactionInfo;
    }
}
