package org.ivy.settlement.follower.model;

import org.ivy.settlement.infrastructure.bytes.ByteUtil;
import org.ivy.settlement.infrastructure.datasource.model.Persistable;
import org.ivy.settlement.infrastructure.rlp.RLP;
import org.ivy.settlement.infrastructure.rlp.RLPList;

import java.math.BigInteger;

/**
 * description:
 * @author carrot
 */
public class FollowerChainSyncOffset extends Persistable {

    int chain;

    long number;

    public FollowerChainSyncOffset(byte[] rlpEncoded) {
        super(rlpEncoded);
    }

    public FollowerChainSyncOffset(int chain, long number) {
        super(null);
        this.chain = chain;
        this.number = number;
        this.rlpEncoded = rlpEncoded();
    }

    @Override
    protected byte[] rlpEncoded() {
        var chain = RLP.encodeInt(this.chain);
        var number = RLP.encodeBigInteger(BigInteger.valueOf(this.number));
        return RLP.encodeList(chain, number);
    }

    @Override
    protected void rlpDecoded() {
        var payload = (RLPList) RLP.decode2(this.rlpEncoded).get(0);
        this.chain = ByteUtil.byteArrayToInt(payload.get(0).getRLPData());
        this.number =  ByteUtil.byteArrayToLong(payload.get(1).getRLPData());
    }

    public int getChain() {
        return chain;
    }

    public long getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return "FollowerChainSyncOffset{" +
                "chain=" + chain +
                ", number=" + number +
                '}';
    }
}
