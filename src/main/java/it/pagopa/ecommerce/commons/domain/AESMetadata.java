package it.pagopa.ecommerce.commons.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.pagopa.ecommerce.commons.utils.ConfidentialDataManager;
import org.springframework.data.annotation.PersistenceConstructor;

import javax.annotation.Nonnull;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

/**
 * <p>
 * AES metadata.
 * </p>
 *
 * @param salt the salt used during encryption
 * @param iv   the initialization vector used during encryption
 */
@JsonIgnoreProperties(value = "mode")
public record AESMetadata(
        @Nonnull byte[] salt,
        @Nonnull byte[] iv
)
        implements
        ConfidentialMetadata {

    /**
     * The length of the initialization vector
     */
    public static final int IV_LENGTH = 12;

    /**
     * The length of the salt
     */
    public static final int SALT_LENGTH = 16;

    @JsonCreator
    private AESMetadata(
            @JsonProperty("salt") String salt,
            @JsonProperty("iv") String iv
    ) {
        this(Base64.getDecoder().decode(salt), Base64.getDecoder().decode(iv)); // NOSONAR
    }

    @PersistenceConstructor
    public AESMetadata {
        // used by Mongo for serialization
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public ConfidentialDataManager.Mode getMode() {
        return ConfidentialDataManager.Mode.AES_GCM_NOPAD;
    }

    /**
     * Default constructor. Generates a {@link AESMetadata} with a randomly
     * generated salt and a randomly generated IV.
     */
    public AESMetadata() {
        this(generateSalt(), generateIv());
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AESMetadata other
                && Arrays.equals(salt, other.salt)
                && Arrays.equals(iv, other.iv);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(salt), Arrays.hashCode(iv));
    }

    @Override
    public String toString() {
        return "AESMetadata{" +
                "salt=" + Arrays.toString(salt) +
                ", iv=" + iv +
                '}';
    }

    @Nonnull
    private static byte[] generateIv() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] iv = new byte[IV_LENGTH];

        secureRandom.nextBytes(iv);

        return iv;
    }

    @Nonnull
    private static byte[] generateSalt() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];

        secureRandom.nextBytes(salt);

        return salt;
    }

    @Nonnull
    @JsonProperty("iv")
    private String getEncodedIv() {
        return Base64.getEncoder().encodeToString(iv);
    }

    @Nonnull
    @JsonProperty("salt")
    private String getEncodedSalt() {
        return Base64.getEncoder().encodeToString(salt);
    }
}
