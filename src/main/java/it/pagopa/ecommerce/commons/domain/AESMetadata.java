package it.pagopa.ecommerce.commons.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.pagopa.ecommerce.commons.utils.ConfidentialDataManager;

import javax.annotation.Nonnull;
import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

@JsonIgnoreProperties(value = "mode")
public record AESMetadata(
        @Nonnull byte[] salt,
        @Nonnull IvParameterSpec iv
)
        implements
        ConfidentialMetadata {

    public final static int IV_LENGTH = 12;
    public static final int SALT_LENGTH = 16;

    @JsonCreator
    private AESMetadata(
            @JsonProperty("salt") String salt,
            @JsonProperty("iv") String iv
    ) {
        this(Base64.getDecoder().decode(salt), new IvParameterSpec(Base64.getDecoder().decode(iv)));
    }

    @Nonnull
    @Override
    public ConfidentialDataManager.Mode getMode() {
        return ConfidentialDataManager.Mode.AES_GCM_NOPAD;
    }

    public AESMetadata() {
        this(generateSalt(), generateIv());
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AESMetadata other
                && Arrays.equals(salt, other.salt)
                && Arrays.equals(iv.getIV(), other.iv.getIV());
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(salt), Arrays.hashCode(iv.getIV()));
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

    @Nonnull
    @JsonProperty("salt")
    private String getEncodedSalt() {
        return Base64.getEncoder().encodeToString(salt);
    }
}
