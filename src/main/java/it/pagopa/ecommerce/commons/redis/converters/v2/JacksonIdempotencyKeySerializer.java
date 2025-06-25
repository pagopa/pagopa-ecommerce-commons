package it.pagopa.ecommerce.commons.redis.converters.v2;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import it.pagopa.ecommerce.commons.domain.v2.IdempotencyKey;

import java.io.IOException;

/**
 * {@link IdempotencyKey} jackson serializer
 */
public class JacksonIdempotencyKeySerializer extends JsonSerializer<IdempotencyKey> {

    /**
     * No-args constructor
     */
    /*
     * @formatter:off
     *
     * Warning java:S1186 - Methods should not be empty
     * Suppressed because this constructor is required by Jackson framework
     * for serialization and should remain empty
     *
     * @formatter:on
     */
    @SuppressWarnings("java:S1186")
    public JacksonIdempotencyKeySerializer() {
    }

    /**
     * @param value       Value to serialize; can <b>not</b> be null.
     * @param gen         Generator used to output resulting Json content
     * @param serializers Provider that can be used to get serializers for
     *                    serializing Objects value contains, if any.
     * @throws IOException - in case an error occurs writing value as json
     */
    @Override
    public void serialize(
                          IdempotencyKey value,
                          JsonGenerator gen,
                          SerializerProvider serializers
    ) throws IOException {
        gen.writeString(value.rawValue());
    }
}
