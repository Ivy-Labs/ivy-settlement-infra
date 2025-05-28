package org.ivy.settlement.infrastructure.crypto.key.asymmetric;

import org.ivy.settlement.infrastructure.bytes.ByteUtil;
import org.ivy.settlement.infrastructure.crypto.key.asymmetric.ec.ECPublicKey;
import org.ivy.settlement.infrastructure.crypto.key.asymmetric.ec.EthECPublicKey;
import org.ivy.settlement.infrastructure.crypto.key.asymmetric.ed.EDPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.security.PublicKey;
import java.util.Arrays;


/**
 * 类SecureKey.java的实现描述：密钥抽象类
 *
 * @author xuhao
 */

public abstract class SecurePublicKey {

    protected static final Logger logger = LoggerFactory.getLogger("crypto");

    //公钥的第1个字节是算法类型
    private int type;

    //公钥的第2~3个字节是节点所属shardingNumber.
    private short shardingNumber;

    //公钥字符串不带前缀
    public byte[] rawPublicKeyBytes;
    //公钥字符串带前缀【算法类型+节点所属分片号】

    protected byte[] pubKeyBytesWithPrefix;

    protected PublicKey innerPubKey;

    //节点id 64字节
    protected byte[] nodeId;

    //区块链地址
    protected byte[] address;


    public static SecurePublicKey generateFromRaw(byte[] rawPublicKeyBytes, int typeCode, short shardingNumber) {
        SecureKeyType keyType = SecureKeyType.getKeyTypeByCode(typeCode);
        if (keyType == null) {
            String errInfo = String.format("SecurePublicKey generate failed，SignAlgorithm with code[%d] not supported.", typeCode);
            logger.error(errInfo);
            throw new RuntimeException(errInfo);
        }
        SecurePublicKey securePublicKey;
        switch (keyType) {
            case ECDSA:
                securePublicKey = new ECPublicKey(Arrays.copyOfRange(rawPublicKeyBytes, 0, rawPublicKeyBytes.length), typeCode, shardingNumber);
                break;
            case ED25519:
                securePublicKey = new EDPublicKey(Arrays.copyOfRange(rawPublicKeyBytes, 0, rawPublicKeyBytes.length), typeCode, shardingNumber);
                break;
            case ETH256K1:
                securePublicKey = new EthECPublicKey(Arrays.copyOfRange(rawPublicKeyBytes, 0, rawPublicKeyBytes.length), typeCode, shardingNumber);
                break;
            default:
                String errInfo = String.format("SecureKey verify failed，unknown keyType:[%s]", keyType);
                logger.error(errInfo);
                throw new RuntimeException(errInfo);
        }

        return securePublicKey;
    }

    public static SecurePublicKey generate(byte[] pubKeyBytesWithPrefix) {
        int typeCode = ByteUtil.byteArrayToInt(Arrays.copyOfRange(pubKeyBytesWithPrefix, 0, 1));
        short shardingNumber = ByteUtil.byteArrayToShort(Arrays.copyOfRange(pubKeyBytesWithPrefix, 1, 3));
        return generateFromRaw(Arrays.copyOfRange(pubKeyBytesWithPrefix, 3, pubKeyBytesWithPrefix.length), typeCode, shardingNumber);
    }

    protected SecurePublicKey(byte[] rawPublicKeyBytes, int type, short shardingNumber) {
        this.type = type;
        this.shardingNumber = shardingNumber;
        this.rawPublicKeyBytes = rawPublicKeyBytes;
        this.innerPubKey = calculatorInner();
        this.pubKeyBytesWithPrefix = withPrefix();
    }

    protected abstract PublicKey calculatorInner();

    public byte[] getNodeId() {
        if (nodeId == null) {
            nodeId = computeNodeId();
        }
        return nodeId;
    }

    protected abstract byte[] computeNodeId();

    public byte[] getAddress() {
        if (address == null) {
            address = computeAddress(this.rawPublicKeyBytes);
        }
        return address;
    }

    protected abstract byte[] computeAddress(byte[] rawPublicKeyBytes);

    public short getShardingNumber() {
        return shardingNumber;
    }

    public int getType() {
        return type;
    }


    public byte[] getPubKey() {
        return ByteUtil.copyFrom(this.pubKeyBytesWithPrefix);
    }

    public byte[] getRawPubKey() {
        return ByteUtil.copyFrom(this.rawPublicKeyBytes);
    }

    public abstract boolean verify(byte[] data, byte[] sig);


    private byte[] withPrefix() {
        byte[] keyBytes = new byte[this.rawPublicKeyBytes.length + 3];
        keyBytes[0] = (byte) type;
        byte[] shardingNumBytes = ByteBuffer.allocate(Short.BYTES).putShort(shardingNumber).array();
        System.arraycopy(shardingNumBytes, 0, keyBytes, 1, shardingNumBytes.length);

        System.arraycopy(this.rawPublicKeyBytes, 0, keyBytes, 3, this.rawPublicKeyBytes.length);
        return keyBytes;
    }
}
