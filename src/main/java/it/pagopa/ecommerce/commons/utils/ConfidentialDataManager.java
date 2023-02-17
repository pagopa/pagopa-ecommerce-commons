package it.pagopa.ecommerce.commons.utils;

import it.pagopa.ecommerce.commons.domain.AESMetadata;
import it.pagopa.ecommerce.commons.domain.Confidential;
import it.pagopa.ecommerce.commons.domain.ConfidentialMetadata;

import javax.annotation.Nonnull;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.function.Function;

public class ConfidentialDataManager {
    public enum Mode {
        AES_GCM_NOPAD("AES/GCM/NoPadding");

        public final String value;

        Mode(String value) {
            this.value = value;
        }
    }

    public interface ConfidentialData {
        @Nonnull String toStringRepresentation();
    }

    private final AESCipher aesCipher;

    public ConfidentialDataManager(@Nonnull SecretKeySpec key) {
        this.aesCipher = new AESCipher(key);
    }

    @Nonnull
    public <T extends ConfidentialData> Confidential<T> encrypt(@Nonnull ConfidentialMetadata metadata, @Nonnull T data) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        return new Confidential<T>(metadata, encryptData(metadata, data.toStringRepresentation()));
    }

    @Nonnull
    public <T extends ConfidentialData> T decrypt(Confidential<T> data, Function<String, T> constructor) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        return constructor.apply(decrypt(data));
    }

    @Nonnull
    public <T extends ConfidentialData> String decrypt(Confidential<T> data) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        return switch (data.confidentialMetadata()) {
            case AESMetadata aesMetadata -> this.aesCipher.decrypt(aesMetadata, data.encodedCipherText());
            default -> throw new IllegalArgumentException("Unsupported cipher metadata!");
        };
    }

    @Nonnull
    private String encryptData(@Nonnull ConfidentialMetadata metadata, @Nonnull String data) throws IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, InvalidKeyException {
        return switch (metadata) {
            case AESMetadata aesMetadata -> this.aesCipher.encrypt(aesMetadata, data);
            default -> throw new IllegalArgumentException("Unsupported cipher metadata!");
        };
    }
}
