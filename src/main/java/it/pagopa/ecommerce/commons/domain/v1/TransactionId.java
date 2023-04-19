package it.pagopa.ecommerce.commons.domain.v1;

import it.pagopa.ecommerce.commons.annotations.ValueObject;

import java.util.UUID;

/**
 * <p>
 * Value object holding a transaction id.
 * </p>
 *
 * @param value the transaction id
 */
@ValueObject
public record TransactionId(UUID value) {

    /**
     * Create transaction id object parsing input String value.
     *
     * @param value - the UUID string without `-` chars
     * @throws IllegalArgumentException if input value is not a valid UUID string
     *                                  without dashes
     */
    public TransactionId(String value) {
        this(TransactionId.fromTrimmedUUIDString(value));

    }

    private static UUID fromTrimmedUUIDString(String trimmedUUIDString) {
        if (trimmedUUIDString == null || trimmedUUIDString.length() != 32) {
            throw new IllegalArgumentException(
                    "Invalid transaction id: [%s]. Transaction id must be not null and 32 chars length"
                            .formatted(trimmedUUIDString)
            );
        }
        char[] uuid = new char[36];
        char[] trimmedUUID = trimmedUUIDString.toCharArray();
        System.arraycopy(trimmedUUID, 0, uuid, 0, 8);
        System.arraycopy(trimmedUUID, 8, uuid, 9, 4);
        System.arraycopy(trimmedUUID, 12, uuid, 14, 4);
        System.arraycopy(trimmedUUID, 16, uuid, 19, 4);
        System.arraycopy(trimmedUUID, 20, uuid, 24, 12);
        uuid[8] = '-';
        uuid[13] = '-';
        uuid[18] = '-';
        uuid[23] = '-';
        return UUID.fromString(new String(uuid));
    }

}
