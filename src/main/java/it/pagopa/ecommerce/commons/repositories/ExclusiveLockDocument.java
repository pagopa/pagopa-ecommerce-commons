package it.pagopa.ecommerce.commons.repositories;

import org.springframework.data.annotation.Id;
import org.springframework.lang.NonNull;

import java.time.OffsetDateTime;

/**
 * Exclusive lock Redis document: this document is used to gain and hold
 * information about applicative locks
 *
 * @param id           lock id: id that uniquely identifies the lock
 * @param creationDate lock creation date
 * @param holderName   name of the application/process that hold this lock
 */
public record ExclusiveLockDocument(
        @NonNull @Id String id,
        String creationDate,
        String holderName
) {
    /**
     * Convenience constructor that set creation date to now
     *
     * @param id         lock id: id that uniquely identifies the lock
     * @param holderName name of the application/process that hold this lock
     */
    public ExclusiveLockDocument(
            String id,
            String holderName
    ) {
        this(id, OffsetDateTime.now().toString(), holderName);
    }
}
