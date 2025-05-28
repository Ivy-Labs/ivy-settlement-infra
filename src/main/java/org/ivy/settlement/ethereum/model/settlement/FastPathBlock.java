package org.ivy.settlement.ethereum.model.settlement;

import org.ivy.settlement.infrastructure.bytes.ByteArrayWrapper;
import org.ivy.settlement.infrastructure.bytes.ByteUtil;
import org.ivy.settlement.infrastructure.crypto.HashUtil;
import org.ivy.settlement.infrastructure.datasource.model.Persistable;
import org.ivy.settlement.infrastructure.rlp.RLP;
import org.ivy.settlement.infrastructure.rlp.RLPList;
import org.spongycastle.util.encoders.Hex;
import org.web3j.abi.TypeEncoder;
import org.web3j.abi.datatypes.generated.Int64;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.Map;

/**
 * description:
 * @author carrot
 */
public class FastPathBlock extends Persistable {

    //event id, after execute transaction ,it become include receipts root hash
    private byte[] hash;

    private byte[] parentHash;

    private long height;

    SettlementBlockInfos settlementBlockInfos;

    private byte[] extendRoot;

    //Transient
    Signs blockSign;

    public static FastPathBlock buildWithoutDecode(byte[] rawData) {
        var block = new FastPathBlock(null);
        block.rlpEncoded = rawData;
        return block;
    }

    public FastPathBlock(byte[] rawData) {
        super(rawData);
    }

    public FastPathBlock(byte[] parentHash, long height,
                 SettlementBlockInfos settlementBlockInfos, byte[] extendRoot, Map<ByteArrayWrapper, Signature> signatures) {
        super(null);

        this.parentHash = parentHash;
        this.height = height;
        this.settlementBlockInfos = settlementBlockInfos;
        this.extendRoot = extendRoot;
        this.blockSign = new Signs(signatures);
        this.rlpEncoded = rlpEncoded();
        this.hash = generateHash();

    }

    private byte[] generateHash() {
        return HashUtil.sha3Dynamic(parentHash, Numeric.hexStringToByteArray(TypeEncoder.encodePacked(new Int64(this.height))), calContentRoot(), calExtendRoot());
    }

    private byte[] calExtendRoot() {
        return new byte[0];
    }

    private byte[] calContentRoot() {
        return new byte[0];
    }

    public byte[] getHash() {
        return hash;
    }

    public byte[] getEventId() {
        return hash;
    }


    public long getHeight() {
        return height;
    }


    public SettlementBlockInfos getSettlementBlockInfos() {
        return settlementBlockInfos;
    }


    public Signs getBlockSign() {
        return blockSign;
    }

    protected byte[] rlpEncoded() {
        byte[][] encode = new byte[7][];
        encode[0] = RLP.encodeElement(this.parentHash);
        encode[1] = RLP.encodeBigInteger(BigInteger.valueOf(this.height));
        encode[2] = this.settlementBlockInfos.getEncoded();
        encode[3] = RLP.encodeElement(this.extendRoot);
        encode[4] = this.blockSign.getEncoded();
        return RLP.encodeList(encode);

    }

    protected void rlpDecoded() {
        RLPList params = RLP.decode2(rlpEncoded);
        RLPList block = (RLPList) params.get(0);
        this.parentHash = block.get(0).getRLPData();
        this.height = ByteUtil.byteArrayToLong(block.get(1).getRLPData());
        this.settlementBlockInfos = new SettlementBlockInfos(block.get(2).getRLPData());
        this.extendRoot = block.get(3).getRLPData();
        this.blockSign = new Signs(block.get(4).getRLPData());
        this.hash = generateHash();
    }


    @Override
    public String toString() {
        return "Block{" +
                "hash=" + Hex.toHexString(hash) +
                ", parentHash=" + Hex.toHexString(parentHash) +
                ", height=" + height +
                ", height=" + height +
                ", settlementBlockInfos=" + settlementBlockInfos +
                '}';
    }



}
