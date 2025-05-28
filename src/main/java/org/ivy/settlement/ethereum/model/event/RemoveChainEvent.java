package org.ivy.settlement.ethereum.model.event;

import org.ivy.settlement.ethereum.model.constants.EthLogEventEnum;
import org.web3j.abi.datatypes.generated.Uint16;
import org.web3j.abi.datatypes.generated.Uint64;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.tx.Contract;

/**
 * description:
 * @author emo
 */
public class RemoveChainEvent implements ManagerChainEvent {

    int chainId;

    long comeIntoEffectHeight;


    public RemoveChainEvent(Uint16 chainId, Uint64 comeIntoEffectHeight) {
        this.chainId = chainId.getValue().intValue();
        this.comeIntoEffectHeight = comeIntoEffectHeight.getValue().longValue();
    }

    public int getChainId() {
        return chainId;
    }


    public long getComeIntoEffectHeight() {
        return comeIntoEffectHeight;
    }


    public static RemoveChainEvent fromLog(Log log) {
        var eventValues = Contract.staticExtractEventParameters(EthLogEventEnum.REMOVE_CHAIN_EVENT.getEvent(), log);
        var nonIndexedValues = eventValues.getNonIndexedValues();
        return new RemoveChainEvent(
                (Uint16) nonIndexedValues.get(0),
                (Uint64) nonIndexedValues.get(1)
        );
    }
}
