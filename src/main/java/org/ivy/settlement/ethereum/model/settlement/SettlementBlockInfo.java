package org.ivy.settlement.ethereum.model.settlement;

import org.ivy.settlement.infrastructure.bytes.ByteUtil;
import org.ivy.settlement.infrastructure.datasource.model.CrossChainEvent;
import org.ivy.settlement.infrastructure.datasource.model.Persistable;
import org.ivy.settlement.infrastructure.rlp.RLP;
import org.ivy.settlement.infrastructure.rlp.RLPList;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * description:
 * @author carrot
 */
public class SettlementBlockInfo extends Persistable {

    private static final int CODEC_OFFSET = 4;

    private int chain;

    private long height;

    private byte[] hash;

    private byte[] receiptRoot;

    private List<CrossChainEvent> crossChainEvents;

    public SettlementBlockInfo(byte[] rlpEncoded) {
        super(rlpEncoded);
    }

    public SettlementBlockInfo(int chain, long height, byte[] hash, byte[] receiptRoot, List<CrossChainEvent> crossChainEvents) {
        super(null);
        this.chain = chain;
        this.height = height;
        this.hash = hash;
        this.receiptRoot = receiptRoot;
        this.crossChainEvents = crossChainEvents;
        this.rlpEncoded = rlpEncoded();
    }

    public int getChain() {
        return chain;
    }

    public long getHeight() {
        return height;
    }

    public byte[] getHash() {
        return hash;
    }

    public byte[] getReceiptRoot() {
        return receiptRoot;
    }

    public List<CrossChainEvent> getCrossChainEvents() {
        return crossChainEvents;
    }

    public byte[] storeKey() {
        return ByteUtil.merge(ByteUtil.intToBytes(this.chain), ByteUtil.longToBytes(this.height));
    }

    @Override
    protected byte[] rlpEncoded() {
        var encodeArray = new byte[CODEC_OFFSET + this.crossChainEvents.size()][];
        encodeArray[0] = RLP.encodeInt(this.chain);
        encodeArray[1] = RLP.encodeBigInteger(BigInteger.valueOf(this.height));
        encodeArray[2] = RLP.encodeElement(this.hash);
        encodeArray[3] = RLP.encodeElement(this.receiptRoot);
        for (var i = 0; i < this.crossChainEvents.size(); i++) {
            encodeArray[i + CODEC_OFFSET] = this.crossChainEvents.get(i).getEncoded();
        }
        return RLP.encodeList(encodeArray);
    }

    @Override
    protected void rlpDecoded() {
        RLPList rlpList = (RLPList) RLP.decode2(rlpEncoded).get(0);
        this.chain = ByteUtil.byteArrayToInt(rlpList.get(0).getRLPData());
        this.height = ByteUtil.byteArrayToLong(rlpList.get(1).getRLPData());
        this.hash = rlpList.get(2).getRLPData();
        this.receiptRoot = rlpList.get(3).getRLPData();
        this.crossChainEvents = new ArrayList<>(rlpList.size() - CODEC_OFFSET);
        for (var i = CODEC_OFFSET; i < rlpList.size(); i++) {
            this.crossChainEvents.add(new CrossChainEvent(rlpList.get(i).getRLPData()));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SettlementBlockInfo that = (SettlementBlockInfo) o;
        return chain == that.chain && height == that.height && Arrays.equals(hash, that.hash) && Arrays.equals(receiptRoot, that.receiptRoot) && Objects.equals(crossChainEvents, that.crossChainEvents);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(chain, height, crossChainEvents);
        result = 31 * result + Arrays.hashCode(hash);
        result = 31 * result + Arrays.hashCode(receiptRoot);
        return result;
    }
}
