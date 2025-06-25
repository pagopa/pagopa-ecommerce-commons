package it.pagopa.ecommerce.commons.redis.converters.v2;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import it.pagopa.ecommerce.commons.domain.v2.IdempotencyKey;

import java.io.IOException;

/**
 * {@link IdempotencyKey} jackson deserializer
 */
public class JacksonIdempotencyKeyDeserializer extends JsonDeserializer<IdempotencyKey> {
    /**
     * No-args constructor
     */
    /*
     * @formatter:off
     *
     * Warning java:S1186 - Methods should not be empty
     * Suppressed because this constructor is required by Jackson framework
     * for deserialization and should remain empty
     *
     * @formatter:on
     */
    @SuppressWarnings("java:S1186")
    public JacksonIdempotencyKeyDeserializer() {
    }

    /**
     * Deserialize json object to {@link IdempotencyKey}
     *
     * @param p    Parsed used for reading JSON content
     * @param ctxt Context that can be used to access information about this
     *             deserialization activity.
     * @return the converted RptId
     * @throws IOException - in case an error occurs reading json object value as
     *                     string
     */
    @Override
    public IdempotencyKey deserialize(
                                      JsonParser p,
                                      DeserializationContext ctxt
    ) throws IOException {
        return new IdempotencyKey(p.getValueAsString());
    }
}
