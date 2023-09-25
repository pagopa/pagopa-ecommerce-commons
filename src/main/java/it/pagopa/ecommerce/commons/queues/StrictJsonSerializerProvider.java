package it.pagopa.ecommerce.commons.queues;

import com.azure.core.serializer.json.jackson.JacksonJsonSerializerBuilder;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProvider;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, true)
            .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true)
            .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonSerializer createInstance() {
        return new JacksonJsonSerializerBuilder().serializer(objectMapper).build();
    }

    /**
     * Add mixin classes for override jackson annotations
     *
     * @param target    target class to enrich
     * @param mixSource mix source class
     * @return this instance
     * @see ObjectMapper#addMixIn(Class, Class)
     */
    public StrictJsonSerializerProvider addMixIn(
                                                 Class<?> target,
                                                 Class<?> mixSource
    ) {
        objectMapper.addMixIn(target, mixSource);
        return this;
    }

    /**
     * Return object mapper instance wrapped instance
     *
     * @return the object mapper instance
     */
    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

}
