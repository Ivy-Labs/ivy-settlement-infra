package org.ivy.settlement.infrastructure.crypto.key.symmetric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 类CipherKey.java的实现描述：
 *
 * @author xuhao
 */

public abstract class CipherKey {
    protected static final Logger logger = LoggerFactory.getLogger("crypto");

    public static CipherKey fromKeyBytes(byte[] keyBytes, String keyTypeDesc) {
        CipherKeyType keyType = CipherKeyType.getKeyTypeByDescription(keyTypeDesc);
        if (keyType == null) {
            logger.error("CipherKey getInstance error, algorithm [" + keyTypeDesc + "] not supported.");
            throw new RuntimeException("CipherKey getInstance error, algorithm [" + keyTypeDesc + "] not supported.");
        }

        switch (keyType) {
            case AES:
                return AESKey.fromKeyBytes(keyBytes);
            default:
                return new DefaultCipherKey();
        }
    }

    //
    public static CipherKey getInstance(String keyTypeDesc) {
        CipherKeyType keyType = CipherKeyType.getKeyTypeByDescription(keyTypeDesc);
        if (keyType == null) {
            logger.error("CipherKey getInstance error, algorithm [" + keyTypeDesc + "] not supported.");
            throw new RuntimeException("CipherKey getInstance error, algorithm [" + keyTypeDesc + "] not supported.");
        }
        switch (keyType) {
            case AES:
                return new AESKey();
            default:
                return new DefaultCipherKey();
        }
    }

    public abstract byte[] encrypt(byte[] data);

    public abstract byte[] decrypt(byte[] data);

    public abstract byte[] getKeyBytes();
}
