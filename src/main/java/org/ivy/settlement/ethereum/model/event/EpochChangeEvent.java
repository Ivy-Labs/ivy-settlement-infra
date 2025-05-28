package org.ivy.settlement.ethereum.model.event;

import org.ivy.settlement.ethereum.model.constants.EthLogEventEnum;
import org.ivy.settlement.infrastructure.datasource.model.EthLogEvent;
import org.web3j.abi.EventValues;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint64;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.tx.Contract;

import java.util.List;

/**
 * description:
 * @author carrot
 */
public class EpochChangeEvent implements EthLogEvent {

    private long ethEpoch;

    public EpochChangeEvent(Uint64 ethEpoch) {
        this.ethEpoch = ethEpoch.getValue().longValue();
    }

    public long getEthEpoch() {
        return ethEpoch;
    }

    public static EpochChangeEvent fromLog(Log log) {
        EventValues eventValues = Contract.staticExtractEventParameters(EthLogEventEnum.EPOCH_CHANGE_EVENT.getEvent(), log);
        List<Type> nonIndexedValues = eventValues.getNonIndexedValues();
        return new EpochChangeEvent((Uint64)nonIndexedValues.get(0));
    }
}
