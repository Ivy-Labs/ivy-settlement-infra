package org.ivy.settlement.ethereum.model.settlement;

import org.ivy.settlement.infrastructure.bytes.ByteUtil;
import org.ivy.settlement.infrastructure.datasource.model.Persistable;
import org.ivy.settlement.infrastructure.rlp.RLP;
import org.ivy.settlement.infrastructure.rlp.RLPList;
import org.spongycastle.util.encoders.Hex;

/**
 * description:
 * @author carrot
 */
public class FollowerChainCrossBlock extends Persistable {

    protected int chain;

    protected long blockHeight;

    protected byte[] blockHash;

    protected byte[][] contents;

    public FollowerChainCrossBlock(int chain, long blockHeight, byte[] blockHash, byte[][] contents) {
        super(null);
        this.chain = chain;
        this.blockHeight = blockHeight;

        this.blockHash = blockHash;
        this.contents = contents;
        this.rlpEncoded = rlpEncoded();
    }

    public FollowerChainCrossBlock(byte[] rlpEncoded) {
        super(rlpEncoded);
    }

    public int getChain() {
        return chain;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public byte[] getBlockHash() {
        return blockHash;
    }

    public byte[][] getContents() {
        return contents;
    }

    @Override
    protected byte[] rlpEncoded() {
        byte[][] encode = new byte[3 + contents.length][];
        encode[0] = RLP.encodeInt(this.chain);
        encode[1] = RLP.encodeElement(ByteUtil.longToBytes(this.blockHeight));
        encode[2] = RLP.encodeElement(this.blockHash);
        for (int i = 3; i < 3 + contents.length; i++) {
            encode[i] = RLP.encodeElement(this.contents[i - 3]);
        }
        return RLP.encodeList(encode);
    }

    @Override
    protected void rlpDecoded() {
        var params = RLP.decode2(rlpEncoded);
        var payload = (RLPList) params.get(0);
        this.chain = ByteUtil.byteArrayToInt(payload.get(0).getRLPData());
        this.blockHeight =  ByteUtil.byteArrayToLong(payload.get(1).getRLPData());
        this.blockHash = payload.get(2).getRLPData();
        var contents = new byte[payload.size() - 3][];
        for (var i = 0; i < contents.length; i++) {
            contents[i] = payload.get(i + 3).getRLPData();
        }
        this.contents = contents;
    }

    @Override
    public String toString() {
        return "SourceChainCrossEvent{" +
                "chain=" + chain +
                ", blockHeight=" + blockHeight +
                ", blockHash=" + Hex.toHexString(blockHash) +
                ", contents size=" + contents.length +
                '}';
    }
}
