package org.ivy.settlement.ethereum.model;


import org.apache.tuweni.bytes.Bytes;
import org.ivy.settlement.infrastructure.bytes.ByteUtil;
import org.ivy.settlement.infrastructure.datasource.model.Keyable;
import org.ivy.settlement.infrastructure.datasource.model.Persistable;
import org.ivy.settlement.infrastructure.rlp.RLP;
import org.ivy.settlement.infrastructure.rlp.RLPList;
import org.ivy.settlement.ethereum.log.EthLogParser;
import org.ivy.settlement.ethereum.model.event.SettlementLogEventCollector;
import tech.pegasys.teku.spec.Spec;
import tech.pegasys.teku.spec.datastructures.blocks.SignedBeaconBlock;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * @author emo
 */
public class BeaconBlockRecord extends Persistable implements Keyable {

    private final static int CODEC_OFFSET = 3;

    long number;

    BigInteger slot;

    byte[] signedBeaconBlockBytes;

    SignedBeaconBlock signedBeaconBlock;

    List<EthReceipt> receipts;

    SettlementLogEventCollector logCollector;



    public BeaconBlockRecord(byte[] rlpEncoded) {
        super(rlpEncoded);
    }

    public BeaconBlockRecord(long number, SignedBeaconBlock signedBeaconBlock, List<EthReceipt> receipts) {
        super(null);
        this.number = number;
        this.slot = signedBeaconBlock.getSlot().bigIntegerValue();
        this.signedBeaconBlock = signedBeaconBlock;
        this.signedBeaconBlockBytes = signedBeaconBlock.sszSerialize().toArray();
        this.receipts = receipts;
        this.rlpEncoded = rlpEncoded();
    }

    @Override
    protected byte[] rlpEncoded() {
        var encodeArray = new byte[CODEC_OFFSET + this.receipts.size()][];
        encodeArray[0] = RLP.encodeElement(ByteUtil.longToBytes(this.number));
        encodeArray[1] = RLP.encodeBigInteger(this.slot);
        encodeArray[2] = RLP.encodeElement(this.signedBeaconBlockBytes);
        for (var i = 0; i < this.receipts.size(); i++) {
            encodeArray[i + CODEC_OFFSET] = this.receipts.get(i).getEncoded();
        }
        return RLP.encodeList(encodeArray);
    }

    @Override
    protected void rlpDecoded() {
        RLPList rlpList = (RLPList) RLP.decode2(rlpEncoded).get(0);
        this.number = ByteUtil.byteArrayToLong(rlpList.get(0).getRLPData());
        this.slot = ByteUtil.bytesToBigInteger(rlpList.get(1).getRLPData());
        this.signedBeaconBlockBytes = rlpList.get(2).getRLPData();
        this.receipts = new ArrayList<>(rlpList.size() - CODEC_OFFSET);
        for (var i = CODEC_OFFSET; i < rlpList.size(); i++) {
            this.receipts.add(new EthReceipt(rlpList.get(i).getRLPData()));
        }
    }

    public SignedBeaconBlock getSignedBeaconBlock(Spec spec) {
        if (signedBeaconBlock == null) {
            signedBeaconBlock = spec.deserializeSignedBeaconBlock(Bytes.of(this.signedBeaconBlockBytes));
        }

        return signedBeaconBlock;
    }

    public SettlementLogEventCollector getLogCollector(EthLogParser ethLogParser) {
        if (logCollector == null) logCollector = ethLogParser.parse(this);
        return logCollector;
    }

    public long getNumber() {
        return number;
    }

    public BigInteger getSlot() {
        return slot;
    }

    public List<EthReceipt> getReceipts() {
        return receipts;
    }

    @Override
    public byte[] keyBytes() {
        return ByteUtil.longToBytes(this.number);
    }
}
