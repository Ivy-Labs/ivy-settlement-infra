package org.ivy.settlement.ethereum.model.event.interactive.war;

import org.ivy.settlement.ethereum.model.constants.EthLogEventEnum;
import org.ivy.settlement.ethereum.model.event.interactive.InteractiveEvent;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.tx.Contract;

/**
 * description:
 * <p>
 * Author lyy
 */
public class SafetyWarEvent extends InteractiveEvent {

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
        return EthLogEventEnum.SAFETY_WAR_EVENT;
    }

    public static SafetyWarEvent fromLog(Log log) {
        var eventValues = Contract.staticExtractEventParameters(EthLogEventEnum.EVM_CHALLENGE_EVENT.getEvent(), log);
        var nonIndexedValues = eventValues.getNonIndexedValues();
        return null;
    }
}
