package it.pagopa.ecommerce.commons.utils;

import it.pagopa.ecommerce.commons.domain.AESMetadata;
import it.pagopa.ecommerce.commons.domain.Confidential;
import it.pagopa.ecommerce.commons.domain.ConfidentialMetadata;
import it.pagopa.ecommerce.commons.domain.PersonalDataVaultMetadata;
import it.pagopa.ecommerce.commons.exceptions.ConfidentialDataException;
import it.pagopa.generated.pdv.v1.api.TokenApi;
import it.pagopa.generated.pdv.v1.dto.PiiResourceDto;
import it.pagopa.generated.pdv.v1.dto.TokenResourceDto;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;
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
         * This mode implements encryption through the external service Personal Data
         * Vault.
         */
        PERSONAL_DATA_VAULT("PersonalDataVault");

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

    private final TokenApi personalDataVaultClient;

    /**
     * Primary constructor.
     *
     * @param key                     AES secret key used to handle
     *                                {@code ConfidentialDataManager.Mode.AES_GCM_NOPAD}
     * @param personalDataVaultClient Client for Personal Data Vault
     */
    public ConfidentialDataManager(
            @Nonnull SecretKeySpec key,
            TokenApi personalDataVaultClient
    ) {
        this.aesCipher = new AESCipher(key);
        this.personalDataVaultClient = personalDataVaultClient;
    }

    /**
     * <p>
     * Encrypts data with the given mode.
     * </p>
     * <br>
     * <p>
     * In case of error the returned {@link Mono} contains a
     * {@link ConfidentialDataException} wrapping one of the following exceptions:
     * <ul>
     * <li>InvalidAlgorithmParameterException See
     * {@link javax.crypto.Cipher#doFinal(byte[])}</li>
     * <li>NoSuchPaddingException See
     * {@link javax.crypto.Cipher#doFinal(byte[])}</li>
     * <li>IllegalBlockSizeException See
     * {@link javax.crypto.Cipher#doFinal(byte[])}</li>
     * <li>NoSuchAlgorithmException See
     * {@link javax.crypto.Cipher#doFinal(byte[])}</li>
     * <li>BadPaddingException See {@link javax.crypto.Cipher#doFinal(byte[])}</li>
     * <li>InvalidKeyException See {@link javax.crypto.Cipher#doFinal(byte[])}</li>
     * </ul>
     *
     * @param mode the mode used to encrypt data with. See the mode details for more
     *             information.
     * @param data the unencrypted data
     * @return a {@link Confidential} instance containing the encrypted data and the
     *         algorithm metadata
     * @param <T> type of the unencrypted data
     */
    @Nonnull
    public <T extends ConfidentialData> Mono<Confidential<T>> encrypt(
                                                                      @Nonnull Mode mode,
                                                                      @Nonnull T data
    ) {
        ConfidentialMetadata metadata = switch (mode) {
            case AES_GCM_NOPAD -> new AESMetadata();
            case PERSONAL_DATA_VAULT -> new PersonalDataVaultMetadata();
            case null -> throw new IllegalArgumentException();
        };

        return encryptData(metadata, data.toStringRepresentation())
                .map(
                        encrypted -> new Confidential<T>(
                                metadata,
                                encrypted
                        )
                );
    }

    /**
     * <p>
     * Decrypts encrypted data.
     * </p>
     * <br>
     * <p>
     * In case of error the returned {@link Mono} contains a
     * {@link ConfidentialDataException} wrapping one of the following exceptions:
     * <ul>
     * <li>InvalidAlgorithmParameterException See
     * {@link javax.crypto.Cipher#doFinal(byte[])}</li>
     * <li>NoSuchPaddingException See
     * {@link javax.crypto.Cipher#doFinal(byte[])}</li>
     * <li>IllegalBlockSizeException See
     * {@link javax.crypto.Cipher#doFinal(byte[])}</li>
     * <li>NoSuchAlgorithmException See
     * {@link javax.crypto.Cipher#doFinal(byte[])}</li>
     * <li>BadPaddingException See {@link javax.crypto.Cipher#doFinal(byte[])}</li>
     * <li>InvalidKeyException See {@link javax.crypto.Cipher#doFinal(byte[])}</li>
     * </ul>
     *
     * @param data        the data to be decrypted
     * @param constructor a function to construct {@code T} instances from the
     *                    serialized deciphered data
     * @return a {@code Mono<T>} instance built from the encrypted data
     * @param <T> the type of the object to be returned
     */
    @Nonnull
    public <T extends ConfidentialData> Mono<T> decrypt(
                                                        Confidential<T> data,
                                                        Function<String, T> constructor
    ) {
        return decrypt(data).map(constructor);
    }

    /**
     * <p>
     * Decrypts the data to string without conversion. Prefer {@link ConfidentialDataManager#decrypt(Confidential, Function)} to this.
     * </p>
     * <br>
     * <p>
     * In case of error the returned {@link Mono} contains a {@link ConfidentialDataException} wrapping one of the following exceptions:
     * <ul>
     *      <li>InvalidAlgorithmParameterException See {@link javax.crypto.Cipher#doFinal(byte[])}</li>
     *      <li>NoSuchPaddingException See {@link javax.crypto.Cipher#doFinal(byte[])}</li>
     *      <li>IllegalBlockSizeException See {@link javax.crypto.Cipher#doFinal(byte[])}</li>
     *      <li>NoSuchAlgorithmException See {@link javax.crypto.Cipher#doFinal(byte[])}</li>
     *      <li>BadPaddingException See {@link javax.crypto.Cipher#doFinal(byte[])}</li>
     *      <li>InvalidKeyException See {@link javax.crypto.Cipher#doFinal(byte[])}</li>
     * </ul>
     *
     * @param data the data to be decrypted
     * @return a {@code Mono<T>} instance built from the encrypted data
     * @param <T> the type of the object to be returned
     */
    @Nonnull
    public <T extends ConfidentialData> Mono<String> decrypt(Confidential<T> data) {

        return switch (data.confidentialMetadata()) {
            case AESMetadata aesMetadata -> Mono.fromCallable(() -> {
                try {
                    return this.aesCipher.decrypt(aesMetadata, data.opaqueData());
                } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException |
                         InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                    throw new ConfidentialDataException(e);
                }
            });
            case PersonalDataVaultMetadata ignored -> this.personalDataVaultClient.findPiiUsingGET(data.opaqueData())
                    .map(PiiResourceDto::getPii)
                    .onErrorMap(WebClientResponseException.class, ConfidentialDataException::new);
            default -> throw new IllegalArgumentException("Unsupported cipher metadata!");
        };
    }

    @Nonnull
    private Mono<String> encryptData(@Nonnull ConfidentialMetadata metadata, @Nonnull String data) {
        return switch (metadata) {
            case AESMetadata aesMetadata -> Mono.fromCallable(() -> {
                try {
                    return this.aesCipher.encrypt(aesMetadata, data);
                } catch (IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException |
                         NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException e) {
                    throw new ConfidentialDataException(e);
                }
            });
            case PersonalDataVaultMetadata ignored -> this.personalDataVaultClient.saveUsingPUT(new PiiResourceDto().pii(data))
                    .map(TokenResourceDto::getToken)
                    .map(UUID::toString)
                    .onErrorMap(WebClientResponseException.class, ConfidentialDataException::new);
            default -> throw new IllegalArgumentException("Unsupported cipher metadata!");
        };
    }
}
