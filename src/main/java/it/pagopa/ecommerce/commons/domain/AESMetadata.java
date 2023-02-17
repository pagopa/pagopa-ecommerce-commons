package it.pagopa.ecommerce.commons.domain;

import com.fasterxml.jackson.annotation.*;
import it.pagopa.ecommerce.commons.utils.ConfidentialDataManager;

import javax.annotation.Nonnull;
import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

/**
 * <p>
 * AES metadata.
 * </p>
 *
 * @param salt the salt used during encryption
 * @param iv   the initialization vector used during encryption
 */
@JsonIgnoreProperties(value = "mode")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AESMetadata(
        @Nonnull Optional<byte[]> salt,
        @Nonnull IvParameterSpec iv
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
            @JsonProperty("salt") Optional<String> salt,
            @JsonProperty("iv") String iv
    ) {
        this(salt.map(Base64.getDecoder()::decode), new IvParameterSpec(Base64.getDecoder().decode(iv))); // NOSONAR
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @JsonTypeId
    @Override
    public ConfidentialDataManager.Mode getMode() {
        return salt.isPresent() ? ConfidentialDataManager.Mode.AES_GCM_NOPAD : ConfidentialDataManager.Mode.AES_GCM_NOPAD_NOSALT;
    }

    /**
     * Default constructor. Generates a {@link AESMetadata} with a randomly
     * generated salt and a randomly generated IV.
     */
    public AESMetadata() {
        this(Optional.of(generateSalt()), generateIv());
    }

    /**
     * <p>
     * Constructs AES metadata without salt. DO NOT USE THIS UNLESS YOU KNOW WHAT
     * YOU ARE DOING.
     * </p>
     *
     * @return a {@link AESMetadata} instance initialized with an IV only.
     */
    public static AESMetadata withoutSalt() {
        return new AESMetadata(Optional.empty(), generateIv());
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AESMetadata other
                && salt.flatMap(s1 -> other.salt.map(s2 -> Arrays.equals(s1, s2))).orElse(true)
                && Arrays.equals(iv.getIV(), other.iv.getIV());
    }

    @Override
    public int hashCode() {
        return Objects.hash(salt, Arrays.hashCode(iv.getIV()));
    }

    @Override
    public String toString() {
        return "AESMetadata{" +
                "salt=" + salt +
                ", iv=" + iv +
                '}';
    }

    @Nonnull
    private static IvParameterSpec generateIv() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] iv = new byte[IV_LENGTH];

        secureRandom.nextBytes(iv);

        return new IvParameterSpec(iv);
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
        return Base64.getEncoder().encodeToString(iv.getIV());
    }

    @JsonProperty("salt")
    private String getEncodedSalt() {
        return salt.map(Base64.getEncoder()::encodeToString).orElse(null);
    }
}
