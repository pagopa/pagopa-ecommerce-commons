package it.pagopa.ecommerce.commons.queues;

import com.azure.core.serializer.json.jackson.JacksonJsonSerializerBuilder;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProvider;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

/**
 * <p>
 * {@link JsonSerializerProvider} that provides a JSON serializer with strict
 * typing guarantees.
 * </p>
 */
public class StrictJsonSerializerProvider implements JsonSerializerProvider {
    /**
     * Object mapper associated to the {@link JsonSerializer}
     */
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, true)
            .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true)
            .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonSerializer createInstance() {
        return new JacksonJsonSerializerBuilder().serializer(OBJECT_MAPPER).build();
    }
}
