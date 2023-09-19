package it.pagopa.ecommerce.commons.documents.v2;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import it.pagopa.ecommerce.commons.documents.BaseTransactionEvent;
import it.pagopa.ecommerce.commons.documents.v2.serialization.TransactionEventTypeResolver;
import it.pagopa.ecommerce.commons.domain.v2.TransactionEventCode;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

import static java.time.ZonedDateTime.now;

/**
 * Hierarchy root for transaction events.
 *
 * @param <T> additional data type
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "eventstore")
@Generated
@NoArgsConstructor
@ToString
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, property = "eventCode", visible = true)
@JsonTypeIdResolver(TransactionEventTypeResolver.class)
public abstract sealed class TransactionEvent<T> extends
        BaseTransactionEvent<T> permits BaseTransactionClosureEvent, TransactionActivatedEvent, TransactionAuthorizationCompletedEvent, TransactionAuthorizationRequestedEvent, TransactionClosureErrorEvent, TransactionClosureRetriedEvent, TransactionExpiredEvent, TransactionRefundErrorEvent, TransactionRefundRequestedEvent, TransactionRefundRetriedEvent, TransactionRefundedEvent, TransactionUserReceiptRequestedEvent, TransactionUserCanceledEvent, TransactionUserReceiptAddErrorEvent, TransactionUserReceiptAddRetriedEvent, TransactionUserReceiptAddedEvent {

    TransactionEvent(
            String transactionId,
            TransactionEventCode eventCode,
            String creationDate,
            T data
    ) {
        super(UUID.randomUUID().toString(), transactionId, creationDate, data, eventCode.toString());

        /*
         * CHK-1413 -> transaction id length lesser than 35 chars here is checked that
         * transaction id is 32 chars long that is UUID with trimmed '-' chars length
         */

        if (transactionId == null || transactionId.length() != 32) {
            throw new IllegalArgumentException(
                    "Invalid input transaction id: [%s]. Transaction id must be length 32 chars (UUID with trimmed dash)"
                            .formatted(transactionId)
            );
        }
    }

    TransactionEvent(
            String transactionId,
            TransactionEventCode eventCode,
            T data
    ) {
        this(transactionId, eventCode, now().toString(), data);
    }
}
