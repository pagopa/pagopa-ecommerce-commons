package it.pagopa.ecommerce.commons.documents;

import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import it.pagopa.ecommerce.commons.documents.v1.serialization.TransactionEventTypeResolver;
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
@AllArgsConstructor
@Document(collection = "eventstore")
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, property = "eventCode")
@JsonTypeIdResolver(TransactionEventTypeResolver.class)
public abstract class BaseTransactionEvent<T> {
    @Id
    private String id;

    @PartitionKey
    private String transactionId;

    private String creationDate;

    private T data;
}
