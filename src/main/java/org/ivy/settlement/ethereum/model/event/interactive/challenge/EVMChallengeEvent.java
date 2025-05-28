package org.ivy.settlement.ethereum.model.event.interactive.challenge;

import org.ivy.settlement.ethereum.model.constants.EthLogEventEnum;
import org.ivy.settlement.ethereum.model.event.interactive.InteractiveEvent;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.tx.Contract;

/**
 * description:
 * @author carrot
 */
public class EVMChallengeEvent extends InteractiveEvent {

    @Override
    public String getId() {
        return null;
    }

    @Override
    public boolean createInstant() {
        return false;
    }

    @Override
    public byte[] buildInitContent() {
        return new byte[0];
    }

    @Override
    public EthLogEventEnum getEthLogEvent() {
        return EthLogEventEnum.EVM_CHALLENGE_EVENT;
    }

    public static EVMChallengeEvent fromLog(Log log) {
        var eventValues = Contract.staticExtractEventParameters(EthLogEventEnum.EVM_CHALLENGE_EVENT.getEvent(), log);
        var nonIndexedValues = eventValues.getNonIndexedValues();
        return null;
    }
}
