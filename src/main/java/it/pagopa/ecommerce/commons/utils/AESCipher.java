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
import java.util.Base64;

public class AESCipher {
    private static final int GCM_TAG_BIT_LENGTH = 128;
    private final SecretKeySpec key;

    public AESCipher(SecretKeySpec key) {
        this.key = key;
    }

    public String encrypt(AESMetadata aesMetadata, String data) throws IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(aesMetadata.getMode().value);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_BIT_LENGTH, aesMetadata.iv().getIV());
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

        cipher.updateAAD(aesMetadata.iv().getIV());
        byte[] cipherText = cipher.doFinal(data.getBytes());

        return Base64.getEncoder().encodeToString(cipherText);
    }

    public String decrypt(AESMetadata aesMetadata, String cipherText) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance(aesMetadata.getMode().value);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_BIT_LENGTH, aesMetadata.iv().getIV());
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

        cipher.updateAAD(aesMetadata.iv().getIV());
        byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));

        return new String(plainText);
    }
}
