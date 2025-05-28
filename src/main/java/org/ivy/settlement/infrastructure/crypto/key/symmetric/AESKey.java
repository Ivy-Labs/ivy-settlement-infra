package org.ivy.settlement.infrastructure.crypto.key.symmetric;


import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;

/**
 * 类AESKey.java的实现描述：
 *
 * @author xuhao
 */

public class AESKey extends CipherKey {
    //算法名
    public static final String KEY_ALGORITHM = "AES";
    public static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    public final int blockSize;
    private final IvParameterSpec ivSpec;

    private SecretKey key;

    public AESKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
            keyGenerator.init(128);
            key = keyGenerator.generateKey();
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            blockSize = cipher.getBlockSize();
            byte[] initVector = new byte[blockSize];
            ivSpec = new IvParameterSpec(initVector);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("AESKey generate failed, algorithm[" + KEY_ALGORITHM + "] not supported.");
        } catch (Exception e) {
            throw new RuntimeException("AESKey generate failed. ", e);
        }
    }

    public AESKey(SecretKey key) {
        try {
            this.key = key;
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            blockSize = cipher.getBlockSize();
            byte[] initVector = new byte[blockSize];
            ivSpec = new IvParameterSpec(initVector);
        } catch (Exception e) {
            throw new RuntimeException("AESKey generate failed. ", e);
        }
    }


    public static AESKey fromKeyBytes(byte[] keyBytes) {
        SecretKey secretKey = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
        return new AESKey(secretKey);
    }

    @Override
    public byte[] encrypt(byte[] data) {

        if (data == null) {
            return null;
        }

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            return cipher.doFinal(data);
        } catch (Exception e) {
            logger.error("AESKey encrypt error!", e);
            return data;
        }
    }

    @Override
    public byte[] decrypt(byte[] data) {
        try {
            if (data == null) {
                return null;
            }
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
            return cipher.doFinal(data);
        } catch (Exception e) {
            logger.error("AESKey decrypt error!", e);
            return data;
        }
    }

    @Override
    public byte[] getKeyBytes() {
        return key.getEncoded();
    }
}
