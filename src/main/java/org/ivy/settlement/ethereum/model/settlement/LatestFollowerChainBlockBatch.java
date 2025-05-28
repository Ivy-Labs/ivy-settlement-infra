package org.ivy.settlement.ethereum.model.settlement;

import org.ivy.settlement.infrastructure.bytes.ByteUtil;
import org.ivy.settlement.infrastructure.rlp.RLP;
import org.ivy.settlement.infrastructure.rlp.RLPList;
import org.ivy.settlement.infrastructure.rlp.RLPModel;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * @author carrot
 */
public class LatestFollowerChainBlockBatch extends RLPModel {

    private final static int CODEC_OFFSET = 3;

    private int chain;

    private long startNumber;

    private long endNumber;

    List<SettlementBlockInfo> settlementBlockInfos;

    public LatestFollowerChainBlockBatch(byte[] rlpEncoded) {
        super(rlpEncoded);
    }

    public LatestFollowerChainBlockBatch(int chain, long startNumber, long endNumber, List<SettlementBlockInfo> settlementBlockInfos) {
        super(null);
        this.chain = chain;
        this.startNumber = startNumber;
        this.endNumber = endNumber;
        this.settlementBlockInfos = settlementBlockInfos;
        this.rlpEncoded = rlpEncoded();
    }


    protected byte[] rlpEncoded() {
        byte[][] encode = new byte[CODEC_OFFSET + this.settlementBlockInfos.size()][];
        encode[0] = RLP.encodeInt(this.chain);
        encode[1] = RLP.encodeBigInteger(BigInteger.valueOf(this.startNumber));
        encode[2] = RLP.encodeBigInteger(BigInteger.valueOf(this.endNumber));
        for (var i = 0; i < this.settlementBlockInfos.size(); i++) {
            encode[i + CODEC_OFFSET] = this.settlementBlockInfos.get(i).rlpEncoded();
        }
        return RLP.encodeList(encode);
    }

    protected void rlpDecoded() {
        RLPList params = RLP.decode2(rlpEncoded);
        RLPList block = (RLPList) params.get(0);
        this.chain = ByteUtil.byteArrayToInt(block.get(0).getRLPData());
        this.startNumber = ByteUtil.byteArrayToLong(block.get(1).getRLPData());
        this.endNumber = ByteUtil.byteArrayToLong(block.get(2).getRLPData());
        this.settlementBlockInfos = new ArrayList<>(block.size() - CODEC_OFFSET);
        for (var i = CODEC_OFFSET; i < block.size(); i++) {
            this.settlementBlockInfos.add(new SettlementBlockInfo(block.get(i).getRLPData()));
        }
    }

    public int getChain() {
        return chain;
    }

    public long getEndNumber() {
        return endNumber;
    }

    public List<SettlementBlockInfo> getSettlementBlockInfos() {
        return settlementBlockInfos;
    }
}
