package it.pagopa.ecommerce.commons.redis.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import it.pagopa.ecommerce.commons.domain.v1.IdempotencyKey;
import it.pagopa.ecommerce.commons.v1.TransactionTestUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JacksonIdempotencyKeyConverterTest {

    private final JacksonIdempotencyKeyDeserializer jacksonIdempotencyKeyDeserializer = new JacksonIdempotencyKeyDeserializer();
    private final JacksonIdempotencyKeySerializer jacksonIdempotencyKeySerializer = new JacksonIdempotencyKeySerializer();

    private final JsonGenerator jsonGenerator = Mockito.mock(JsonGenerator.class);

    private final SerializerProvider serializerProvider = Mockito.mock(SerializerProvider.class);

    private final JsonParser jsonParser = Mockito.mock(JsonParser.class);

    private final DeserializationContext deserializationContext = Mockito.mock(DeserializationContext.class);

    @Test
    void shouldSerializeIdempotencyKeySuccessfully() throws IOException {
        // pre-requisite
        IdempotencyKey idempotencyKey = new IdempotencyKey(TransactionTestUtils.IDEMPOTENCY_KEY);
        Mockito.doNothing().when(jsonGenerator).writeString(TransactionTestUtils.IDEMPOTENCY_KEY);
        jacksonIdempotencyKeySerializer.serialize(idempotencyKey, jsonGenerator, serializerProvider);
        Mockito.verify(jsonGenerator, Mockito.times(1)).writeString(TransactionTestUtils.IDEMPOTENCY_KEY);
    }

    @Test
    void shouldDeserializeIdempotencyKeySuccessfully() throws IOException {
        String idempotencyKey = TransactionTestUtils.IDEMPOTENCY_KEY;
        Mockito.when(jsonParser.getValueAsString()).thenReturn(idempotencyKey);
        IdempotencyKey deserialized = jacksonIdempotencyKeyDeserializer.deserialize(jsonParser, deserializationContext);
        assertEquals(new IdempotencyKey(idempotencyKey), deserialized);
    }

}
