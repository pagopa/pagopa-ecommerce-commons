package it.pagopa.ecommerce.commons.redis.converters.v1;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import it.pagopa.ecommerce.commons.domain.IdempotencyKey;

import java.io.IOException;

/**
 * {@link IdempotencyKey} jackson serializer
 */
public class JacksonIdempotencyKeySerializer extends JsonSerializer<IdempotencyKey> {

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
