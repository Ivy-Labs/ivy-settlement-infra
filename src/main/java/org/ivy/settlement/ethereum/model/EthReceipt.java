package org.ivy.settlement.ethereum.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.ivy.settlement.infrastructure.bytes.ByteUtil;
import org.ivy.settlement.infrastructure.rlp.RLP;
import org.ivy.settlement.infrastructure.rlp.RLPList;
import org.ivy.settlement.infrastructure.rlp.RLPModel;
import org.ivy.settlement.infrastructure.string.Numeric;
import org.spongycastle.util.encoders.Hex;
import org.web3j.protocol.core.methods.response.Log;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * description:
 * @author emo
 */
public class EthReceipt extends RLPModel {

    private String transactionHash;

    private String transactionIndex;

    private String blockHash;

    private String blockNumber;

    private String cumulativeGasUsed;

    private String gasUsed;

    private String contractAddress;

    private String root;

    private String status;

    private String from;

    private String to;

    private List<Log> logs;

    private String logsBloom;

    private String revertReason;

    private String type;

    private String effectiveGasPrice;

    @JsonCreator
    public EthReceipt(@JsonProperty("transactionHash") String transactionHash, @JsonProperty("transactionIndex") String transactionIndex, @JsonProperty("blockHash") String blockHash, @JsonProperty("blockNumber") String blockNumber, @JsonProperty("cumulativeGasUsed") String cumulativeGasUsed, @JsonProperty("gasUsed") String gasUsed, @JsonProperty("contractAddress") String contractAddress, @JsonProperty("root") String root, @JsonProperty("status") String status, @JsonProperty("from") String from, @JsonProperty("to") String to, @JsonProperty("logs") List<Log> logs, @JsonProperty("logsBloom") String logsBloom, @JsonProperty("revertReason") String revertReason, @JsonProperty("type") String type, @JsonProperty("effectiveGasPrice") String effectiveGasPrice) {
        super(null);
        this.transactionHash = transactionHash;
        this.transactionIndex = transactionIndex;
        this.blockHash = blockHash;
        this.blockNumber = blockNumber;
        this.cumulativeGasUsed = cumulativeGasUsed;
        this.gasUsed = gasUsed;
        this.contractAddress = contractAddress;
        this.root = root;
        this.status = status;
        this.from = from;
        this.to = to;
        this.logs = logs;
        this.logsBloom = logsBloom;
        this.revertReason = revertReason;
        this.type = type;
        this.effectiveGasPrice = effectiveGasPrice;
        this.rlpEncoded = rlpEncoded();
    }

    public EthReceipt(byte[] rlpEncoded) {
        super(rlpEncoded);
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public String getTransactionIndex() {
        return transactionIndex;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public String getBlockNumber() {
        return blockNumber;
    }

    public String getCumulativeGasUsed() {
        return cumulativeGasUsed;
    }

    public String getGasUsed() {
        return gasUsed;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public String getRoot() {
        return root;
    }

    public String getStatus() {
        return status;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public List<Log> getLogs() {
        return logs;
    }

    public String getLogsBloom() {
        return logsBloom;
    }

    public String getRevertReason() {
        return revertReason;
    }

    public String getType() {
        return type;
    }

    public String getEffectiveGasPrice() {
        return effectiveGasPrice;
    }

    @Override
    protected byte[] rlpEncoded() {
        byte[][] encodeArray = new byte[16][];

        encodeArray[0] = this.transactionHash == null ? RLP.encodeElement(ByteUtil.EMPTY_BYTE_ARRAY) : RLP.encodeString(this.transactionHash);
        encodeArray[1] = this.transactionIndex == null ? RLP.encodeElement(ByteUtil.EMPTY_BYTE_ARRAY) : RLP.encodeString(this.transactionIndex);
        encodeArray[2] = this.blockHash == null ? RLP.encodeElement(ByteUtil.EMPTY_BYTE_ARRAY) : RLP.encodeString(this.blockHash);
        encodeArray[3] = this.blockNumber == null ? RLP.encodeElement(ByteUtil.EMPTY_BYTE_ARRAY) : RLP.encodeString(this.blockNumber);
        encodeArray[4] = this.cumulativeGasUsed == null ? RLP.encodeElement(ByteUtil.EMPTY_BYTE_ARRAY) : RLP.encodeString(this.cumulativeGasUsed);
        encodeArray[5] = this.gasUsed == null ? RLP.encodeElement(ByteUtil.EMPTY_BYTE_ARRAY) : RLP.encodeString(this.gasUsed);
        encodeArray[6] = this.contractAddress == null ? RLP.encodeElement(ByteUtil.EMPTY_BYTE_ARRAY) : RLP.encodeString(this.contractAddress);
        encodeArray[7] = this.root == null ? RLP.encodeElement(ByteUtil.EMPTY_BYTE_ARRAY) : RLP.encodeString(this.root);
        encodeArray[8] = this.status == null ? RLP.encodeElement(ByteUtil.EMPTY_BYTE_ARRAY) : RLP.encodeString(this.status);
        encodeArray[9] = this.from == null ? RLP.encodeElement(ByteUtil.EMPTY_BYTE_ARRAY) : RLP.encodeString(this.from);
        encodeArray[10] = this.to == null ? RLP.encodeElement(ByteUtil.EMPTY_BYTE_ARRAY) : RLP.encodeString(this.to);

        byte[][] logsEncoded = null;
        if (this.logs != null) {
            logsEncoded = new byte[this.logs.size()][];
            int i = 0;
            for (var logIter = this.logs.iterator(); logIter.hasNext(); ++i) {
                logsEncoded[i] = RLP.encodeElement(encodeLog(logIter.next()));
            }
        }
        encodeArray[11] = RLP.encodeList(logsEncoded);

        encodeArray[12] = this.logsBloom == null ? RLP.encodeElement(ByteUtil.EMPTY_BYTE_ARRAY) : RLP.encodeString(this.logsBloom);
        encodeArray[13] = this.revertReason == null ? RLP.encodeElement(ByteUtil.EMPTY_BYTE_ARRAY) : RLP.encodeString(this.revertReason);
        encodeArray[14] = this.type == null ? RLP.encodeElement(ByteUtil.EMPTY_BYTE_ARRAY) : RLP.encodeString(this.type);
        encodeArray[15] = this.effectiveGasPrice == null ? RLP.encodeElement(ByteUtil.EMPTY_BYTE_ARRAY) : RLP.encodeString(this.effectiveGasPrice);

        return RLP.encodeList(encodeArray);
    }


    private static byte[] encodeLog(Log log) {
        byte[][] encodeArray = new byte[9 + log.getTopics().size()][];
        encodeArray[0] = log.isRemoved() ? RLP.encodeBigInteger(BigInteger.ONE) : RLP.encodeBigInteger(BigInteger.ZERO);
        encodeArray[1] = log.getLogIndex() == null ? RLP.encodeElement(ByteUtil.EMPTY_BYTE_ARRAY) : RLP.encodeString(log.getLogIndexRaw());
        encodeArray[2] = log.getTransactionIndex() == null ? RLP.encodeElement(ByteUtil.EMPTY_BYTE_ARRAY) : RLP.encodeString(log.getTransactionIndexRaw());
        encodeArray[3] = log.getTransactionHash() == null ? RLP.encodeElement(ByteUtil.EMPTY_BYTE_ARRAY) : RLP.encodeString(log.getTransactionHash());
        encodeArray[4] = log.getBlockHash() == null ? RLP.encodeElement(ByteUtil.EMPTY_BYTE_ARRAY) : RLP.encodeString(log.getBlockHash());
        encodeArray[5] = log.getBlockNumber() == null ? RLP.encodeElement(ByteUtil.EMPTY_BYTE_ARRAY) : RLP.encodeString(log.getBlockNumberRaw());
        encodeArray[6] = log.getAddress() == null ? RLP.encodeElement(ByteUtil.EMPTY_BYTE_ARRAY) : RLP.encodeString(log.getAddress());
        encodeArray[7] = log.getData() == null ? RLP.encodeElement(ByteUtil.EMPTY_BYTE_ARRAY) : RLP.encodeString(log.getData());
        encodeArray[8] = log.getType() == null ? RLP.encodeElement(ByteUtil.EMPTY_BYTE_ARRAY) : RLP.encodeString(log.getType());
        for (var i = 0; i < log.getTopics().size(); i++) {
            encodeArray[9 + i] = RLP.encodeString(log.getTopics().get(i));
        }
        return RLP.encodeList(encodeArray);
    }


    @Override
    protected void rlpDecoded() {
        RLPList rlpList = (RLPList) RLP.decode2(rlpEncoded).get(0);

        this.transactionHash = rlpList.get(0).getRLPData() == null ? null : new String(rlpList.get(0).getRLPData());
        this.transactionIndex = rlpList.get(1).getRLPData() == null ? null : new String(rlpList.get(1).getRLPData());
        this.blockHash = rlpList.get(2).getRLPData() == null ? null : new String(rlpList.get(2).getRLPData());
        this.blockNumber = rlpList.get(3).getRLPData() == null ? null : new String(rlpList.get(3).getRLPData());
        this.cumulativeGasUsed = rlpList.get(4).getRLPData() == null ? null : new String(rlpList.get(4).getRLPData());
        this.gasUsed = rlpList.get(5).getRLPData() == null ? null : new String(rlpList.get(5).getRLPData());
        this.contractAddress = rlpList.get(6).getRLPData() == null ? null : new String(rlpList.get(6).getRLPData());
        this.root = rlpList.get(7).getRLPData() == null ? null : new String(rlpList.get(7).getRLPData());
        this.status = rlpList.get(8).getRLPData() == null ? null : new String(rlpList.get(8).getRLPData());
        this.from = rlpList.get(9).getRLPData() == null ? null : new String(rlpList.get(9).getRLPData());
        this.to = rlpList.get(10).getRLPData() == null ? null : new String(rlpList.get(10).getRLPData());

        var logIter = ((RLPList) rlpList.get(11)).iterator();
        this.logs = new ArrayList<>();
        while (logIter.hasNext()) {
            this.logs.add(decodeLog(logIter.next().getRLPData()));
        }

        this.logsBloom = rlpList.get(12).getRLPData() == null ? null : new String(rlpList.get(12).getRLPData());
        this.revertReason = rlpList.get(13).getRLPData() == null ? null : new String(rlpList.get(13).getRLPData());
        this.type = rlpList.get(14).getRLPData() == null ? null : new String(rlpList.get(14).getRLPData());
        this.effectiveGasPrice = rlpList.get(15).getRLPData() == null ? null : new String(rlpList.get(15).getRLPData());
    }

    private static Log decodeLog(byte[] logEncode) {
        var rlpList = (RLPList) RLP.decode2(logEncode).get(0);
        var removed = (Objects.equals(ByteUtil.bytesToBigInteger(rlpList.get(0).getRLPData()), BigInteger.ONE));
        var logIndex = rlpList.get(1).getRLPData() == null ? null : new String(rlpList.get(1).getRLPData());
        var transactionIndex = rlpList.get(2).getRLPData() == null ? null : new String(rlpList.get(2).getRLPData());
        var transactionHash = rlpList.get(3).getRLPData() == null ? null : new String(rlpList.get(3).getRLPData());
        var blockHash = rlpList.get(4).getRLPData() == null ? null : new String(rlpList.get(4).getRLPData());
        var blockNumber = rlpList.get(5).getRLPData() == null ? null : new String(rlpList.get(5).getRLPData());
        var address = rlpList.get(6).getRLPData() == null ? null : new String(rlpList.get(6).getRLPData());
        var data = rlpList.get(7).getRLPData() == null ? null : new String(rlpList.get(7).getRLPData());
        var type = rlpList.get(8).getRLPData() == null ? null : new String(rlpList.get(8).getRLPData());

        var topics = new ArrayList<String>(rlpList.size() - 9);
        for (var i = 9; i < rlpList.size(); i++) {
            topics.add(new String(rlpList.get(i).getRLPData()));
        }
        return new Log(removed, logIndex, transactionIndex, transactionHash, blockHash, blockNumber, address, data, type, topics);
    }


    public byte[] getReceiptTrieEncoded() {
        var postTxState = Numeric.hexStringToByteArray(this.status);
        if (ByteUtil.isSingleZero(postTxState)) {
            postTxState = null;
        }
        var postTxStateRLP = RLP.encodeElement(postTxState);
        var cumulativeGasRLP = RLP.encodeElement(Numeric.hexStringToByteArray(cumulativeGasUsed));
        var bloomRLP = RLP.encodeElement(Numeric.hexStringToByteArray(logsBloom));

        final byte[] logInfoListRLP;
        var logInfoListE = new byte[this.logs.size()][];

        var i = 0;
        for (var logInfo : logs) {
            logInfoListE[i] = getLogTrieEncode(logInfo);
            ++i;
        }
        logInfoListRLP = RLP.encodeList(logInfoListE);

        return RLP.encodeList(postTxStateRLP, cumulativeGasRLP, bloomRLP, logInfoListRLP);
    }

    private byte[] getLogTrieEncode(Log log) {
        byte[] addressEncoded = RLP.encodeElement(Numeric.hexStringToByteArray(log.getAddress()));

        byte[][] topicsEncoded = null;
        if (log.getTopics() != null) {
            topicsEncoded = new byte[log.getTopics().size()][];
            int i = 0;
            for (var topic : log.getTopics()) {
                topicsEncoded[i] = RLP.encodeElement(Hex.decode(Numeric.cleanHexPrefix((topic))));
                ++i;
            }
        }

        byte[] dataEncoded = RLP.encodeElement(Numeric.hexStringToByteArray(log.getData()));
        return RLP.encodeList(addressEncoded, RLP.encodeList(topicsEncoded), dataEncoded);
    }
}