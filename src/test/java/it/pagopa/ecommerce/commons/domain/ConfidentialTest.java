package it.pagopa.ecommerce.commons.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import it.pagopa.ecommerce.commons.domain.v1.Email;
import it.pagopa.ecommerce.commons.utils.ConfidentialDataManager;
import it.pagopa.ecommerce.commons.utils.ConfidentialDataManager.Mode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class ConfidentialTest {
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module());
    private final ConfidentialDataManager confidentialDataManager;

    ConfidentialTest() {
        byte[] key = new byte[16];
        new Random().nextBytes(key);

        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        this.confidentialDataManager = new ConfidentialDataManager(secretKey);
    }

    @Test
    void confidentialJsonRepresentationWithSaltIsOK()
            throws InvalidAlgorithmParameterException, IllegalBlockSizeException,
            NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeySpecException,
            InvalidKeyException, JsonProcessingException {
        Email email = new Email("foo@example.com");

        Confidential<Email> confidentialEmail = this.confidentialDataManager.encrypt(Mode.AES_GCM_NOPAD, email);

        String serialized = objectMapper.writeValueAsString(confidentialEmail);

        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {
        };
        Map<String, Object> jsonData = objectMapper.readValue(serialized, typeRef);

        assertEquals(Set.of("data", "metadata"), jsonData.keySet());
        assertEquals(((Map<String, Object>) jsonData.get("metadata")).get("mode"), Mode.AES_GCM_NOPAD.toString());
    }

    @Test
    void roundtripEncryptionDecryptionWithSaltIsSuccessful() throws InvalidAlgorithmParameterException,
            IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException,
            InvalidKeySpecException, InvalidKeyException, JsonProcessingException {
        Email email = new Email("foo@example.com");

        Confidential<Email> confidentialEmail = this.confidentialDataManager.encrypt(Mode.AES_GCM_NOPAD, email);

        String serialized = objectMapper.writeValueAsString(confidentialEmail);

        TypeReference<Confidential<Email>> typeRef = new TypeReference<>() {
        };
        Confidential<Email> deserialized = objectMapper.readValue(serialized, typeRef);

        Email decryptedEmail = confidentialDataManager.decrypt(deserialized, Email::new);

        assertEquals(email, decryptedEmail);
    }

    @Test
    void confidentialJsonRepresentationWithoutSaltIsOK()
            throws InvalidAlgorithmParameterException, IllegalBlockSizeException,
            NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeySpecException,
            InvalidKeyException, JsonProcessingException {
        Email email = new Email("foo@example.com");

        Confidential<Email> confidentialEmail = this.confidentialDataManager.encrypt(Mode.AES_GCM_NOPAD_NOSALT, email);

        String serialized = objectMapper.writeValueAsString(confidentialEmail);

        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {
        };
        Map<String, Object> jsonData = objectMapper.readValue(serialized, typeRef);

        assertEquals(((Map<String, Object>) jsonData.get("metadata")).get("mode"), Mode.AES_GCM_NOPAD_NOSALT.toString());
        assertEquals(Set.of("data", "metadata"), jsonData.keySet());
    }

    @Test
    void roundtripEncryptionDecryptionWithoutSaltIsSuccessful() throws InvalidAlgorithmParameterException,
            IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException,
            InvalidKeySpecException, InvalidKeyException, JsonProcessingException {
        Email email = new Email("foo@example.com");

        Confidential<Email> confidentialEmail = this.confidentialDataManager.encrypt(Mode.AES_GCM_NOPAD_NOSALT, email);

        String serialized = objectMapper.writeValueAsString(confidentialEmail);

        TypeReference<Confidential<Email>> typeRef = new TypeReference<>() {
        };
        Confidential<Email> deserialized = objectMapper.readValue(serialized, typeRef);

        Email decryptedEmail = confidentialDataManager.decrypt(deserialized, Email::new);

        assertEquals(Mode.AES_GCM_NOPAD_NOSALT, deserialized.confidentialMetadata().getMode());
        assertEquals(email, decryptedEmail);
    }

    @Test
    void deserializationFailsOnInvalidMetadata() throws InvalidAlgorithmParameterException, IllegalBlockSizeException,
            NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeySpecException,
            InvalidKeyException, JsonProcessingException {
        Email email = new Email("foo@example.com");

        Confidential<Email> confidentialEmail = this.confidentialDataManager.encrypt(Mode.AES_GCM_NOPAD_NOSALT, email);

        String serialized = objectMapper.writeValueAsString(confidentialEmail);

        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {
        };
        Map<String, Object> jsonData = objectMapper.readValue(serialized, typeRef);

        jsonData.put("metadata", Map.of("mode", Mode.AES_GCM_NOPAD));

        String tamperedValue = objectMapper.writeValueAsString(jsonData);
        TypeReference<Confidential<Email>> confidentialEmailTypeRef = new TypeReference<>() {
        };

        assertThrows(
                ValueInstantiationException.class,
                () -> objectMapper.readValue(tamperedValue, confidentialEmailTypeRef)
        );
    }
}
