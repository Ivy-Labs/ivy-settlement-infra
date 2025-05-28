package org.ivy.settlement.ethereum.model.settlement;

import org.ivy.settlement.infrastructure.bytes.ByteUtil;
import org.spongycastle.util.encoders.Hex;


/**
 * description:
 * @author carrot
 */
public class Signature {

    final byte[] sig;

    public byte[] getSig() {
        return sig;
    }

    public Signature(byte[] sig) {
        this.sig = sig;
    }

    public Signature copy() {
        return new Signature(ByteUtil.copyFrom(this.sig));
    }

    @Override
    public String toString() {
        return "Signature{" +
                "sig=" + Hex.toHexString(sig) +
                '}';
    }
}
