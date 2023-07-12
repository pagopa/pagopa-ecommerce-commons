package it.pagopa.ecommerce.commons.documents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public abstract class BaseTransactionEvent<T> {
    private String id;

    private String transactionId;

    private String creationDate;

    private T data;
}
