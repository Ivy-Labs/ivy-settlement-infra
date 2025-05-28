package org.ivy.settlement.infrastructure.crypto;

import org.ivy.settlement.infrastructure.bytes.ByteUtil;
import org.ivy.settlement.infrastructure.crypto.key.asymmetric.SecureKeyType;
import org.ivy.settlement.infrastructure.crypto.key.asymmetric.SecurePublicKey;
import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;

/**
 * description:
 * @author carrot
 */
public class VerifyingKey {

    byte[] key;

    SecurePublicKey securePublicKey;

    public VerifyingKey(byte[] key) {
        this.key = key;
        this.securePublicKey = SecurePublicKey.generate(key);
    }

    public VerifyingKey(byte[] rawPublicKeyBytes, int type, short shardingNumber) {
        this.securePublicKey = SecurePublicKey.generateFromRaw(rawPublicKeyBytes, type, shardingNumber);
        this.key = this.securePublicKey.getPubKey();
    }

    public static VerifyingKey generateETHKey(byte[] key) {
        return new VerifyingKey(key, SecureKeyType.ETH256K1.getCode(), (short) 1);
    }

    public byte[] getKey() {
        return key;
    }

    public SecurePublicKey getSecurePublicKey() {
        return securePublicKey;
    }

    public VerifyingKey clone() {
        return new VerifyingKey(ByteUtil.copyFrom(this.key));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VerifyingKey that = (VerifyingKey) o;
        return Arrays.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(key);
    }

    @Override
    public String toString() {
        return "VerifyingKey{" +
                "key=" + Hex.toHexString(key) +
                '}';
    }
}
