package it.pagopa.ecommerce.commons.utils;

import it.pagopa.ecommerce.commons.domain.AESMetadata;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

/**
 * <p>
 * Utility class implementing AES encryption.
 * </p>
 * <p>
 * Currently supports only GCM mode.
 * </p>
 */
public class AESCipher {
    private static final int GCM_TAG_BIT_LENGTH = 128;
    private final SecretKeySpec key;

    /**
     * Constructs a cipher with the given secret key
     *
     * @param key the AES secret key
     */
    public AESCipher(SecretKeySpec key) {
        this.key = key;
    }

    /**
     * Encrypts a piece of data
     *
     * @param aesMetadata algorithm metadata
     * @param data        the data to be encrypted
     * @return the encrypted data
     * @throws IllegalBlockSizeException          See {@link Cipher#doFinal(byte[])}
     * @throws BadPaddingException                See {@link Cipher#doFinal(byte[])}
     * @throws NoSuchPaddingException             See {@link Cipher#doFinal(byte[])}
     * @throws NoSuchAlgorithmException           See {@link Cipher#doFinal(byte[])}
     * @throws InvalidAlgorithmParameterException See {@link Cipher#doFinal(byte[])}
     * @throws InvalidKeyException                See {@link Cipher#doFinal(byte[])}
     */
    public String encrypt(
                          AESMetadata aesMetadata,
                          String data
    ) throws IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException {

        String cipherInstance = aesMetadata.getMode().value;
        Cipher cipher = Cipher.getInstance(cipherInstance);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_BIT_LENGTH, aesMetadata.iv().getIV());
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

        cipher.updateAAD(aesMetadata.iv().getIV());
        byte[] cipherInput = aesMetadata.salt()
                .map(salt -> concatBytes(data.getBytes(), salt))
                .orElse(data.getBytes());
        byte[] cipherText = cipher.doFinal(cipherInput);

        return Base64.getEncoder().encodeToString(cipherText);
    }

    /**
     * Decrypts a piece of encrypted data
     *
     * @param aesMetadata algorithm metadata. Must match the metadata used during
     *                    encryption.
     * @param cipherText  the encrypted data
     * @return the decrypted data
     * @throws NoSuchPaddingException             See {@link Cipher#doFinal(byte[])}
     * @throws NoSuchAlgorithmException           See {@link Cipher#doFinal(byte[])}
     * @throws InvalidAlgorithmParameterException See {@link Cipher#doFinal(byte[])}
     * @throws InvalidKeyException                See {@link Cipher#doFinal(byte[])}
     * @throws BadPaddingException                See {@link Cipher#doFinal(byte[])}
     * @throws IllegalBlockSizeException          See {@link Cipher#doFinal(byte[])}
     */
    public String decrypt(
                          AESMetadata aesMetadata,
                          String cipherText
    ) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        String cipherInstance = aesMetadata.getMode().value;
        Cipher cipher = Cipher.getInstance(cipherInstance);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_BIT_LENGTH, aesMetadata.iv().getIV());
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

        cipher.updateAAD(aesMetadata.iv().getIV());

        int saltLength = aesMetadata.salt().map(salt -> salt.length).orElse(0);
        byte[] decipheredData = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        byte[] plainText = Arrays.copyOfRange(decipheredData, 0, decipheredData.length - saltLength);

        return new String(plainText);
    }

    private static byte[] concatBytes(
                                      byte[] array1,
                                      byte[] array2
    ) {
        byte[] result = Arrays.copyOf(array1, array1.length + array2.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }
}
