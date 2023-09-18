package it.pagopa.ecommerce.commons.domain.v2;

import io.vavr.control.Either;
import it.pagopa.ecommerce.commons.annotations.ValueObject;
import org.apache.commons.codec.binary.Base64;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * <p>
 * Value object holding a transaction id.
 * </p>
 *
 * @param uuid the transaction id
 */
@ValueObject
public record TransactionId(UUID uuid) {

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

    /**
     * Get transaction id string value
     *
     * @return transaction id as UUID string without dashes
     */
    public String value() {
        return uuid.toString().replace("-", "");
    }

    /**
     * <p>
     * Create a transaction id from a base64 encoded string.
     * </p>
     * <p>
     * Inverse of {@link TransactionId#base64()}
     * </p>
     *
     * @param base64 base64 encoded transaction id
     * @return an {@link Either} containing a transaction id object or an exception
     */
    public static Either<IllegalArgumentException, TransactionId> fromBase64(String base64) {
        try {
            byte[] bytes = Base64.decodeBase64(base64);
            ByteBuffer bb = ByteBuffer.wrap(bytes);
            return Either.<IllegalArgumentException, UUID>right(new UUID(bb.getLong(), bb.getLong()))
                    .map(TransactionId::new);
        } catch (BufferUnderflowException e) {
            return Either.left(new IllegalArgumentException("Error while decoding transactionId"));
        }
    }

    /**
     * <p>
     * Returns a URL safe, base64 encoding of the transaction id.
     * </p>
     * <p>
     * Inverse of {@link TransactionId#fromBase64(String)}
     * </p>
     *
     * @return the encoded transaction id
     */
    public String base64() {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return Base64.encodeBase64URLSafeString(bb.array());
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
