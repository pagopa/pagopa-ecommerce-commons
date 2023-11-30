package it.pagopa.ecommerce.commons.documents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Persistence class for dead letter events
 */
@Document(collection = "dead-letter-events")
@Data
@ToString
@AllArgsConstructor
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
}
