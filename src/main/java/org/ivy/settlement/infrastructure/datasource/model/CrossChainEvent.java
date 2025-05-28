package org.ivy.settlement.infrastructure.datasource.model;

import org.ivy.settlement.infrastructure.bytes.ByteUtil;
import org.ivy.settlement.infrastructure.rlp.RLP;
import org.ivy.settlement.infrastructure.rlp.RLPList;
import org.ivy.settlement.infrastructure.rlp.RLPModel;

import java.math.BigInteger;

/**
 * description:
 * @author carrot
 */
public class CrossChainEvent extends RLPModel implements EthLogEvent {

    private int chain;

    byte[] dstAddress;

    long gasLimit;

    byte[] blockHash;

    byte[] data;

    byte[] transactionProof;

    byte[] payableTo;

    public CrossChainEvent(int chain, byte[] dstAddress, long gasLimit, byte[] blockHash, byte[] data, byte[] transactionProof, byte[] payableTo) {
        super(null);
        this.chain = chain;
        this.dstAddress = dstAddress;
        this.gasLimit = gasLimit;
        this.blockHash = blockHash;
        this.data = data;
        this.transactionProof = transactionProof;
        this.payableTo = payableTo;
        this.rlpEncoded = rlpEncoded();
    }

    public CrossChainEvent(byte[] rlpEncoded) {
        super(rlpEncoded);
    }

    @Override
    protected byte[] rlpEncoded() {
        var chain = RLP.encodeInt(this.chain);
        var dstAddress = RLP.encodeElement(this.dstAddress);
        var gasLimit = RLP.encodeBigInteger(BigInteger.valueOf(this.gasLimit));
        var blockHash = RLP.encodeElement(this.blockHash);
        var data = RLP.encodeElement(this.data);
        var transactionProof = RLP.encodeElement(this.transactionProof);
        var payableTo = RLP.encodeElement(this.payableTo);
        return RLP.encodeList(chain, dstAddress, gasLimit, blockHash, data, transactionProof, payableTo);
    }

    @Override
    protected void rlpDecoded() {
        var payload = (RLPList) RLP.decode2(this.rlpEncoded).get(0);
        this.chain = ByteUtil.byteArrayToInt(payload.get(0).getRLPData());
        this.dstAddress = payload.get(1).getRLPData();
        this.gasLimit =  ByteUtil.byteArrayToLong(payload.get(2).getRLPData());
        this.blockHash = payload.get(3).getRLPData();
        this.data = payload.get(4).getRLPData();
        this.transactionProof = payload.get(5).getRLPData();
        this.payableTo = payload.get(6).getRLPData();
    }

    public int getChain() {
        return chain;
    }

    public byte[] getDstAddress() {
        return dstAddress;
    }

    public long getGasLimit() {
        return gasLimit;
    }

    public byte[] getBlockHash() {
        return blockHash;
    }

    public byte[] getData() {
        return data;
    }

    public byte[] getTransactionProof() {
        return transactionProof;
    }

    public byte[] getPayableTo() {
        return payableTo;
    }
}
