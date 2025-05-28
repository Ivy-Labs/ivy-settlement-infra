package org.ivy.settlement.ethereum.model.event;

import org.bouncycastle.util.encoders.Hex;
import org.ivy.settlement.infrastructure.datasource.model.EthLogEvent;
import org.ivy.settlement.ethereum.model.constants.EthLogEventEnum;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.tx.Contract;

/**
 * description:
 * @author carrot
 */
public class VoterUpdateEvent implements EthLogEvent {


    public static final int NODE_DEATH = 1;
    public static final int NODE_UPDATE = 2;


    final int opType;

    final private int slotIndex;

    final private byte[] pubKey;

    final long consensusVotingPower;

    final long totalVotingPower;

    final long opEthEpoch;

    public VoterUpdateEvent(Uint8 opType, Uint8 slotIndex, Bytes32 pubKey, Uint256 consensusVotingPower, Uint256 totalVotingPower, Uint256 opEthEpoch) {
        this.opType = opType.getValue().intValue();
        this.slotIndex = slotIndex.getValue().intValue();
        this.pubKey = pubKey.getValue();
        this.consensusVotingPower = consensusVotingPower.getValue().longValue();
        this.totalVotingPower = totalVotingPower.getValue().longValue();
        this.opEthEpoch = opEthEpoch.getValue().longValue();
    }

    public VoterUpdateEvent(int opType, int slotIndex, byte[] pubKey, long consensusVotingPower, long totalVotingPower, long opEthEpoch) {
        this.opType = opType;
        this.slotIndex = slotIndex;
        this.pubKey = pubKey;
        this.consensusVotingPower = consensusVotingPower;
        this.totalVotingPower = totalVotingPower;
        this.opEthEpoch = opEthEpoch;
    }

    public int getOpType() {
        return opType;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public byte[] getPubKey() {
        return pubKey;
    }

    public long getConsensusVotingPower() {
        return consensusVotingPower;
    }

    public long getTotalVotingPower() {
        return totalVotingPower;
    }

    public long getOpEthEpoch() {
        return opEthEpoch;
    }

    @Override
    public String toString() {
        return "UpdateValidatorEvent{" +
                "opType=" + opType +
                ", slotIndex=" + slotIndex +
                ", pubKey=" + Hex.toHexString(pubKey) +
                ", consensusVotingPower=" + consensusVotingPower +
                ", totalVotingPower=" + totalVotingPower +
                '}';
    }

    public static VoterUpdateEvent fromLog(Log log) {
        var eventValues = Contract.staticExtractEventParameters(EthLogEventEnum.VOTER_UPDATE_EVENT.getEvent(), log);
        var nonIndexedValues = eventValues.getNonIndexedValues();
        return new VoterUpdateEvent(
                (Uint8) nonIndexedValues.get(0),
                (Uint8) nonIndexedValues.get(1),
                (Bytes32) nonIndexedValues.get(2),
                (Uint256) nonIndexedValues.get(3),
                (Uint256) nonIndexedValues.get(4),
                (Uint256) nonIndexedValues.get(5)

        );
    }
}
