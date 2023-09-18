package it.pagopa.ecommerce.commons.redis.v1.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import it.pagopa.ecommerce.commons.domain.v1.RptId;
import it.pagopa.ecommerce.commons.redis.converters.v1.JacksonRptIdDeserializer;
import it.pagopa.ecommerce.commons.redis.converters.v1.JacksonRptIdSerializer;
import it.pagopa.ecommerce.commons.v1.TransactionTestUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JacksonRptIdConverterTest {

    private final JacksonRptIdDeserializer jacksonRptIdDeserializer = new JacksonRptIdDeserializer();
    private final JacksonRptIdSerializer jacksonRptIdSerializer = new JacksonRptIdSerializer();

    private final JsonGenerator jsonGenerator = Mockito.mock(JsonGenerator.class);

    private final SerializerProvider serializerProvider = Mockito.mock(SerializerProvider.class);

    private final JsonParser jsonParser = Mockito.mock(JsonParser.class);

    private final DeserializationContext deserializationContext = Mockito.mock(DeserializationContext.class);

    @Test
    void shouldSerializeRptIdSuccessfully() throws IOException {
        // pre-requisite
        RptId rptId = new RptId(TransactionTestUtils.RPT_ID);
        Mockito.doNothing().when(jsonGenerator).writeString(TransactionTestUtils.RPT_ID);
        jacksonRptIdSerializer.serialize(rptId, jsonGenerator, serializerProvider);
        Mockito.verify(jsonGenerator, Mockito.times(1)).writeString(TransactionTestUtils.RPT_ID);
    }

    @Test
    void shouldDeserializeRptIdSuccessfully() throws IOException {
        String rptId = TransactionTestUtils.RPT_ID;
        Mockito.when(jsonParser.getValueAsString()).thenReturn(rptId);
        RptId deserialized = jacksonRptIdDeserializer.deserialize(jsonParser, deserializationContext);
        assertEquals(new RptId(rptId), deserialized);
    }

}
