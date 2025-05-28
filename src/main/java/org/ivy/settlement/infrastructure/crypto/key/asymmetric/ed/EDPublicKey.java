package org.ivy.settlement.infrastructure.crypto.key.asymmetric.ed;

import org.bouncycastle.util.encoders.Hex;
import org.ivy.settlement.infrastructure.bytes.ByteUtil;
import org.ivy.settlement.infrastructure.crypto.CastleProvider;
import org.ivy.settlement.infrastructure.crypto.HashUtil;
import org.ivy.settlement.infrastructure.crypto.key.asymmetric.SecurePublicKey;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

/**
 * 类EDPublicKey.java的实现描述：
 *
 * @author xuhao
 */

public class EDPublicKey extends SecurePublicKey {

    public static final String ALGORITHM = "EdDSA";

    public static final String ED25519 = "Ed25519";

    //key generate from bc-java will has the Ed25519PubKeyPrefix, but  has not preifx from open ssl。
    private static final String Ed25519PubKeyPrefix = "302a300506032b6570032100";//the prefix of ed25519 pub key

    public EDPublicKey(byte[] rawPublicKeyBytes, int type, short shardingNum) {
        super(rawPublicKeyBytes, type, shardingNum);
    }

    @Override
    protected PublicKey calculatorInner() {
        return publicKeyFromRawBytes(rawPublicKeyBytes);
    }

    @Override
    protected byte[] computeNodeId() {
        byte[] pubBytes = rawPubKeyBytesWithoutFormat(this.rawPublicKeyBytes);
        return ByteUtil.merge(pubBytes, pubBytes);
    }

    @Override
    protected byte[] computeAddress(byte[] pubBytes) {
        return HashUtil.sha3omit12(
                Arrays.copyOfRange(pubBytes, 0, pubBytes.length));
    }

    @Override
    public boolean verify(byte[] data, byte[] sig) {
        try {
            Signature signature = Signature.getInstance(ED25519, "BC");
            signature.initVerify(innerPubKey);
            signature.update(data);
            return signature.verify(sig);
        } catch (Exception e) {
            logger.warn("EDPublicKey verify error!", e);
            return false;
        }
    }

    private static PublicKey publicKeyFromRawBytes(byte[] rawPubKeyBytes) {
        if (rawPubKeyBytes == null) {
            return null;
        } else {
            try {
                return KeyFactory.getInstance(ALGORITHM, CastleProvider.getBouncyInstance())
                        .generatePublic(new X509EncodedKeySpec(formatPublic(rawPubKeyBytes)));
            } catch (InvalidKeySpecException ex) {
                throw new AssertionError("Assumed correct key spec statically", ex);
            } catch (NoSuchAlgorithmException ex) {
                throw new AssertionError("Assumed correct algorithm of ed pubKey", ex);

            }
        }
    }

    //公钥去掉prefix
    private static byte[] rawPubKeyBytesWithoutFormat(byte[] rawPubKeyBytes) {
        if (rawPubKeyBytes == null) {
            return null;
        }
        String pubKeyStr = Hex.toHexString(rawPubKeyBytes);
        if (pubKeyStr.startsWith(Ed25519PubKeyPrefix)) {
            pubKeyStr = pubKeyStr.substring(Ed25519PubKeyPrefix.length());
        }
        return Hex.decode(pubKeyStr);
    }


    //公钥标准化，统一加上Ed25519PubKeyPrefix前缀
    private static byte[] formatPublic(byte[] rawPubKeyBytes) {
        if (rawPubKeyBytes == null) {
            return null;
        }
        String pubKeyStr = Hex.toHexString(rawPubKeyBytes);
        if (!pubKeyStr.startsWith(Ed25519PubKeyPrefix)) {
            pubKeyStr = Ed25519PubKeyPrefix + pubKeyStr;
        }
        return Hex.decode(pubKeyStr);
    }
}
