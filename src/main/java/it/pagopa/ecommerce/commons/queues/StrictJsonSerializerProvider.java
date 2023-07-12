package it.pagopa.ecommerce.commons.queues;

import com.azure.core.serializer.json.jackson.JacksonJsonSerializerBuilder;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProvider;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import java.util.List;
import java.util.Optional;

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
            .activateDefaultTyping(
                    BasicPolymorphicTypeValidator.builder()
                            .allowIfBaseType("it.pagopa.ecommerce")
                            .allowIfBaseType(Optional.class)
                            .allowIfBaseType(List.class)
                            .build(),
                    ObjectMapper.DefaultTyping.EVERYTHING,
                    JsonTypeInfo.As.PROPERTY
            )
            .registerModule(new Jdk8Module())
            .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
            .configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonSerializer createInstance() {
        return new JacksonJsonSerializerBuilder().serializer(OBJECT_MAPPER).build();
    }
}
