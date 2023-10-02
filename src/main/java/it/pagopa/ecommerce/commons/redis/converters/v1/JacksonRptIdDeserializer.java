package it.pagopa.ecommerce.commons.redis.converters.v1;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import it.pagopa.ecommerce.commons.domain.RptId;

import java.io.IOException;

/**
 * {@link RptId} jackson deserializer
 */
public class JacksonRptIdDeserializer extends JsonDeserializer<RptId> {
    /**
     * Deserialize json object to {@link RptId}
     *
     * @param p    Parsed used for reading JSON content
     * @param ctxt Context that can be used to access information about this
     *             deserialization activity.
     * @return the converted RptId
     * @throws IOException - in case an error occurs reading json object value as
     *                     string
     */
    @Override
    public RptId deserialize(
                             JsonParser p,
                             DeserializationContext ctxt
    ) throws IOException {
        return new RptId(p.getValueAsString());
    }
}
