package org.ivy.settlement.ethereum.model.event;

import org.ivy.settlement.infrastructure.datasource.model.CrossChainEvent;
import org.ivy.settlement.infrastructure.datasource.model.EthLogEvent;
import org.ivy.settlement.ethereum.model.constants.EthLogEventEnum;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint16;
import org.web3j.abi.datatypes.generated.Uint64;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.tx.Contract;

/**
 * description:
 * @author carrot
 */
public class EthCrossChainEvent implements EthLogEvent {

    CrossChainEvent crossChainEvent;

    public EthCrossChainEvent(int chain, byte[] dstAddress, long gasLimit, byte[] blockHash, byte[] data, byte[] transactionProof, byte[] payableTo) {
        this.crossChainEvent = new CrossChainEvent(chain, dstAddress, gasLimit, blockHash, data, transactionProof, payableTo);
    }

    public CrossChainEvent getCrossChainEvent() {
        return crossChainEvent;
    }

    public static EthCrossChainEvent fromLog(Log log) {
        var eventValues = Contract.staticExtractEventParameters(EthLogEventEnum.CROSS_CHAIN_EVENT.getEvent(), log);
        var nonIndexedValues = eventValues.getNonIndexedValues();
        return new EthCrossChainEvent(
                ((Uint16) nonIndexedValues.get(0)).getValue().intValue(),
                ((Bytes32) nonIndexedValues.get(1)).getValue(),
                ((Uint64) nonIndexedValues.get(2)).getValue().longValue(),
                ((Bytes32) nonIndexedValues.get(3)).getValue(),
                ((DynamicBytes) nonIndexedValues.get(4)).getValue(),
                ((DynamicBytes) nonIndexedValues.get(5)).getValue(),
                ((Bytes32) nonIndexedValues.get(6)).getValue()
        );
    }
}
