package it.pagopa.ecommerce.commons.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class AESMetadataTest {
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module());

    @Test
    void aesMetadataSerializationWithSaltIsOk() throws JsonProcessingException {
        AESMetadata metadata = new AESMetadata();

        String serialized = objectMapper.writeValueAsString(metadata);
        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {
        };
        Map<String, Object> jsonData = objectMapper.readValue(serialized, typeRef);

        assertEquals(Set.of("mode", "iv", "salt"), jsonData.keySet());
    }

    @Test
    void aesMetadataSerializationWithoutSaltIsOk() throws JsonProcessingException {
        AESMetadata metadata = AESMetadata.withoutSalt();

        String serialized = objectMapper.writeValueAsString(metadata);
        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {
        };
        Map<String, Object> jsonData = objectMapper.readValue(serialized, typeRef);

        assertEquals(Set.of("mode", "iv"), jsonData.keySet());
    }

    @Test
    void aesSerializationRoundtripWithSaltIsSuccessful() throws JsonProcessingException {
        AESMetadata metadata = new AESMetadata();

        String serialized = objectMapper.writeValueAsString(metadata);

        TypeReference<AESMetadata> typeRef = new TypeReference<>() {
        };
        AESMetadata deserialized = objectMapper.readValue(serialized, typeRef);

        assertArrayEquals(metadata.salt().get(), deserialized.salt().get());
        assertArrayEquals(metadata.iv().getIV(), deserialized.iv().getIV());
        assertEquals(metadata, deserialized);
    }

    @Test
    void aesSerializationRoundtripWithoutSaltIsSuccessful() throws JsonProcessingException {
        AESMetadata metadata = AESMetadata.withoutSalt();

        String serialized = objectMapper.writeValueAsString(metadata);

        TypeReference<AESMetadata> typeRef = new TypeReference<>() {
        };
        AESMetadata deserialized = objectMapper.readValue(serialized, typeRef);

        assertEquals(metadata.salt(), deserialized.salt());
        assertArrayEquals(metadata.iv().getIV(), deserialized.iv().getIV());
        assertEquals(metadata, deserialized);
    }
}
