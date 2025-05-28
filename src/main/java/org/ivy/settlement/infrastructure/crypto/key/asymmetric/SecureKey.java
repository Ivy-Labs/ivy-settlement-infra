package org.ivy.settlement.infrastructure.crypto.key.asymmetric;

import org.ivy.settlement.infrastructure.bytes.ByteUtil;
import org.ivy.settlement.infrastructure.crypto.key.asymmetric.factory.ECDSAKeyFactory;
import org.ivy.settlement.infrastructure.crypto.key.asymmetric.factory.EDKeyFactory;
import org.ivy.settlement.infrastructure.crypto.key.asymmetric.factory.EthECKeyFactory;
import org.ivy.settlement.infrastructure.crypto.key.asymmetric.factory.SecureKeyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;

/**
 * 类SecureKey.java的实现描述：密钥抽象类
 *
 * @author xuhao
 */

public abstract class SecureKey {

    protected static final Logger logger = LoggerFactory.getLogger("crypto");

    private final static HashMap<SecureKeyType, SecureKeyFactory> keyType2KeyFactoryMap = new HashMap<>();

    //公钥，用于进行验签
    protected SecurePublicKey securePublicKey;

    //默认密钥前缀，采用ECDSA算法，shardingNumber=1
    public static byte[] DEFAULT_KEY_PREFIX = ByteUtil.hexStringToBytes("0x010001");

    private static final SecureRandom secureRandom;

    static {
        keyType2KeyFactoryMap.put(SecureKeyType.ECDSA, new ECDSAKeyFactory());
        keyType2KeyFactoryMap.put(SecureKeyType.ED25519, new EDKeyFactory());
        keyType2KeyFactoryMap.put(SecureKeyType.ETH256K1, new EthECKeyFactory());
        secureRandom = new SecureRandom();
    }

    public static SecureKey fromPrivate(byte[] privKey) {
        int typeCode = ByteUtil.byteArrayToInt(Arrays.copyOfRange(privKey, 0, 1));
        SecureKeyType keyType = SecureKeyType.getKeyTypeByCode(typeCode);
        if (keyType == null) {
            throw new RuntimeException("SecureKey fromPrivate failed, SignAlgorithm with code [" + typeCode + "] not supported.");
        }
        short shardingNumber = ByteUtil.byteArrayToShort(Arrays.copyOfRange(privKey, 1, 3));
        return keyType2KeyFactoryMap.get(keyType).fromRawPrivate(Arrays.copyOfRange(privKey, 3, privKey.length), shardingNumber);
    }

    public static SecureKey getInstance(String keyTypeDesc, int shardingNumber) {
        return getInstance(keyTypeDesc, shardingNumber, secureRandom);
    }

    public static SecureKey getInstance(String keyTypeDesc, int shardingNumber, SecureRandom secureRandom) {
        SecureKeyType keyType = SecureKeyType.getKeyTypeByDescription(keyTypeDesc);
        if (keyType == null) {
            throw new RuntimeException("SecureKey getInstance failed, SignAlgorithm [" + keyTypeDesc + "] not supported.");
        }
        return keyType2KeyFactoryMap.get(keyType).getInstance(secureRandom, (short) shardingNumber);
    }


    public byte[] getNodeId() {
        return securePublicKey.getNodeId();
    }

    public byte[] getAddress() {
        return securePublicKey.getAddress();
    }

    public byte[] getPubKey() {
        return securePublicKey.getPubKey();
    }

    public byte[] getRawPubKeyBytes() {
        return securePublicKey.getRawPubKey();
    }

    public short getShardingNumber() {
        return securePublicKey.getShardingNumber();
    }

    public int getType() {
        return securePublicKey.getType();
    }

    public byte[] getPrivKey() {
        byte[] privKeySrc = getRawPrivKeyBytes();
        return withKeyPrefix(privKeySrc);
    }

    public abstract byte[] getRawPrivKeyBytes();

    public abstract byte[] sign(byte[] messageHash);

    public boolean verify(byte[] data, byte[] signature) {
        return securePublicKey.verify(data, signature);
    }


    public static byte[] withDefaultKeyPrefix(byte[] keySrc) {
        if (keySrc == null) {
            return null;
        }
        byte[] keyBytes = new byte[keySrc.length + 3];
        System.arraycopy(DEFAULT_KEY_PREFIX, 0, keyBytes, 0, DEFAULT_KEY_PREFIX.length);

        System.arraycopy(keySrc, 0, keyBytes, 3, keySrc.length);
        return keyBytes;
    }

    /**
     * 给密钥添加前缀：第1字节：密钥类型，第2~3字节：所属分片号
     *
     * @param keySrc
     * @return
     */
    private byte[] withKeyPrefix(byte[] keySrc) {
        if (keySrc == null) {
            return null;
        }
        byte[] keyBytes = new byte[keySrc.length + 3];
        //第1字节：密钥类型
        keyBytes[0] = (byte) this.getType();
        //第2~3字节：所属分片号
        byte[] shardingNumBytes = ByteBuffer.allocate(Short.BYTES).putShort(this.getShardingNumber()).array();
        System.arraycopy(shardingNumBytes, 0, keyBytes, 1, shardingNumBytes.length);

        System.arraycopy(keySrc, 0, keyBytes, 3, keySrc.length);
        return keyBytes;
    }

    @SuppressWarnings("serial")
    public static class MissingPrivateKeyException extends RuntimeException {
    }
}
