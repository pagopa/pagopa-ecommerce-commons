package it.pagopa.ecommerce.commons.repositories;

import org.springframework.data.annotation.Id;
import org.springframework.lang.NonNull;

import java.time.OffsetDateTime;

/**
 * Redis structure to hold volatile information about a orderId generated.
 *
 * @param id           New unique id to save
 * @param creationDate Date of creation of the saved item
 */
public record UniqueIdDocument(
        @NonNull @Id String id,
        String creationDate
) {

    /**
     * Constructor.
     *
     * @param id unique id to save
     */
    public UniqueIdDocument(String id) {
        this(id, OffsetDateTime.now().toString());
    }
}
