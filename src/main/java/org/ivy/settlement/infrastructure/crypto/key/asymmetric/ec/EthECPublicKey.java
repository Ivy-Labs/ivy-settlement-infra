package org.ivy.settlement.infrastructure.crypto.key.asymmetric.ec;

import org.ivy.settlement.infrastructure.crypto.HashUtil;
import org.ivy.settlement.infrastructure.crypto.key.asymmetric.SecurePublicKey;

import java.security.PublicKey;
import java.util.Arrays;

/**
 * EthECPublicKey.java description：
 *
 * @Author laiyiyu
 */
public class EthECPublicKey extends SecurePublicKey {

    public EthECPublicKey(byte[] rawPublicKeyBytes, int type, short shardingNum) {
        super(rawPublicKeyBytes, type, shardingNum);
    }

    @Override
    protected PublicKey calculatorInner() {
        return ECPublicKey.publicKeyFromRawBytes(this.rawPublicKeyBytes);
    }

    @Override
    protected byte[] computeNodeId() {
        return Arrays.copyOfRange(this.rawPublicKeyBytes, 1, this.rawPublicKeyBytes.length);
    }

    @Override
    protected byte[] computeAddress(byte[] rawPublicKeyBytes) {
        return HashUtil.sha3omit12(
                Arrays.copyOfRange(rawPublicKeyBytes, 1, rawPublicKeyBytes.length));
    }

    @Override
    public boolean verify(byte[] data, byte[] sig) {
        return EthECKey.verify(data, sig, this.rawPublicKeyBytes);
    }
}
