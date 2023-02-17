package it.pagopa.ecommerce.commons.utils;

import com.fasterxml.jackson.annotation.JsonValue;
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

/**
 * <p>
 * Class used to abstract over confidential data management and used to
 * construct and deconstruct {@link Confidential} instances.
 * </p>
 * <p>
 * This class is meant to be the entry point for an application to handle data
 * in and out of {@link Confidential} via the
 * {@link #encrypt(Mode, ConfidentialData)} and {@link #decrypt(Confidential)}
 * methods
 * </p>
 */
public class ConfidentialDataManager {
    /**
     * <p>
     * Mode for ensuring secrecy of confidential data. Each mode corresponds to a
     * different class implementing procedures to encrypt/mask/tokenize/etc
     * confidential data.
     * </p>
     */
    public enum Mode {
        /**
         * This mode implements encryption with an AES cipher in GCM mode without
         * padding. For more details, see {@link AESCipher}.
         */
        AES_GCM_NOPAD("AES/GCM/NoPadding"),

        /**
         * <p>
         * This mode implements encryption with an AES cipher in GCM mode without
         * padding and without salting.
         * </p>
         * <p>
         * <b>NOTA BENE:</b> No salting has security implications which must be
         * considered carefully. If you're unsure, use {@code AES_GCM_NOPAD}
         * </p>
         * For more details, see {@link AESCipher}.
         *
         */
        AES_GCM_NOPAD_NOSALT("AES/GCM/NoPadding");

        /**
         * String representation of the mode. Must be unique for each mode.
         */
        public final String value;

        Mode(String value) {
            this.value = value;
        }
    }

    /**
     * <p>
     * Utility interface that handles conversions {@code T} -> {@link String}.
     * Classes that want to be put inside a {@link Confidential} need to implement
     * this interface, and provide a corresponding method to reconstruct instances
     * from a string.
     * </p>
     */
    public interface ConfidentialData {
        /**
         * A function to serialize the given data. Note that the implementor is not
         * bound to any serialization format as long as it can independently deserialize
         * it.
         *
         * @return serialized data.
         */
        @Nonnull
        String toStringRepresentation();
    }

    private final AESCipher aesCipher;

    /**
     * Primary constructor.
     *
     * @param key AES secret key used to handle
     *            {@code ConfidentialDataManager.Mode.AES_GCM_NOPAD}
     */
    public ConfidentialDataManager(@Nonnull SecretKeySpec key) {
        this.aesCipher = new AESCipher(key);
    }

    /**
     * Encrypts data with the given mode.
     *
     * @param mode the mode used to encrypt data with. See the mode details for more
     *             information.
     * @param data the unencrypted data
     * @return a {@link Confidential} instance containing the encrypted data and the
     *         algorithm metadata
     * @param <T> type of the unencrypted data
     * @throws InvalidAlgorithmParameterException See
     *                                            {@link javax.crypto.Cipher#doFinal(byte[])}
     * @throws IllegalBlockSizeException          See
     *                                            {@link javax.crypto.Cipher#doFinal(byte[])}
     * @throws NoSuchPaddingException             See
     *                                            {@link javax.crypto.Cipher#doFinal(byte[])}
     * @throws BadPaddingException                See
     *                                            {@link javax.crypto.Cipher#doFinal(byte[])}
     * @throws NoSuchAlgorithmException           See
     *                                            {@link javax.crypto.Cipher#doFinal(byte[])}
     * @throws InvalidKeySpecException            See
     *                                            {@link javax.crypto.Cipher#doFinal(byte[])}
     * @throws InvalidKeyException                See
     *                                            {@link javax.crypto.Cipher#doFinal(byte[])}
     */
    @Nonnull
    public <T extends ConfidentialData> Confidential<T> encrypt(
                                                                Mode mode,
                                                                @Nonnull T data
    ) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException,
            NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        if (mode == Mode.AES_GCM_NOPAD) {
            AESMetadata confidentialMetadata = new AESMetadata();
            return new Confidential<T>(
                    confidentialMetadata,
                    encryptData(confidentialMetadata, data.toStringRepresentation())
            );
        } else if (mode == Mode.AES_GCM_NOPAD_NOSALT) {
            AESMetadata confidentialMetadata = AESMetadata.withoutSalt();
            return new Confidential<T>(
                    confidentialMetadata,
                    encryptData(confidentialMetadata, data.toStringRepresentation())
            );

        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Decrypts encrypted data.
     *
     * @param data        the data to be decrypted
     * @param constructor a function to construct {@code T} instances from the
     *                    serialized deciphered data
     * @return a {@code T} instance built from the encrypted data
     * @param <T> the type of the object to be returned
     * @throws InvalidAlgorithmParameterException See
     *                                            {@link javax.crypto.Cipher#doFinal(byte[])}
     * @throws NoSuchPaddingException             See
     *                                            {@link javax.crypto.Cipher#doFinal(byte[])}
     * @throws IllegalBlockSizeException          See
     *                                            {@link javax.crypto.Cipher#doFinal(byte[])}
     * @throws NoSuchAlgorithmException           See
     *                                            {@link javax.crypto.Cipher#doFinal(byte[])}
     * @throws BadPaddingException                See
     *                                            {@link javax.crypto.Cipher#doFinal(byte[])}
     * @throws InvalidKeyException                See
     *                                            {@link javax.crypto.Cipher#doFinal(byte[])}
     */
    @Nonnull
    public <T extends ConfidentialData> T decrypt(
                                                  Confidential<T> data,
                                                  Function<String, T> constructor
    ) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException,
            NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        return constructor.apply(decrypt(data));
    }

    /**
     * Descrypts the data to string without conversion. Prefer {@link ConfidentialDataManager#decrypt(Confidential, Function)} to this.
     * @param data the data to be decrypted
     * @return a {@code T} instance built from the encrypted data
     * @param <T> the type of the object to be returned
     * @throws InvalidAlgorithmParameterException See {@link javax.crypto.Cipher#doFinal(byte[])}
     * @throws NoSuchPaddingException See {@link javax.crypto.Cipher#doFinal(byte[])}
     * @throws IllegalBlockSizeException See {@link javax.crypto.Cipher#doFinal(byte[])}
     * @throws NoSuchAlgorithmException See {@link javax.crypto.Cipher#doFinal(byte[])}
     * @throws BadPaddingException See {@link javax.crypto.Cipher#doFinal(byte[])}
     * @throws InvalidKeyException See {@link javax.crypto.Cipher#doFinal(byte[])}
     */
    @Nonnull
    public <T extends ConfidentialData> String decrypt(Confidential<T> data) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        return switch (data.confidentialMetadata()) {
            case AESMetadata aesMetadata -> this.aesCipher.decrypt(aesMetadata, data.opaqueData());
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
