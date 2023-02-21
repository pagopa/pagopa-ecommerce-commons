package it.pagopa.ecommerce.commons.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class AESMetadataTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void aesMetadataSerializationIsOk() throws JsonProcessingException {
        AESMetadata metadata = new AESMetadata();

        String serialized = objectMapper.writeValueAsString(metadata);
        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {
        };
        Map<String, Object> jsonData = objectMapper.readValue(serialized, typeRef);

        assertEquals(Set.of("mode", "iv", "salt"), jsonData.keySet());
    }

    @Test
    void aesSerializationRoundtripIsSuccessful() throws JsonProcessingException {
        AESMetadata metadata = new AESMetadata();

        String serialized = objectMapper.writeValueAsString(metadata);

        TypeReference<AESMetadata> typeRef = new TypeReference<>() {
        };
        AESMetadata deserialized = objectMapper.readValue(serialized, typeRef);

        assertArrayEquals(metadata.salt(), deserialized.salt());
        assertArrayEquals(metadata.iv().getIV(), deserialized.iv().getIV());
        assertEquals(metadata, deserialized);
    }
}
