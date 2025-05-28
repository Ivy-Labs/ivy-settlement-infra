package org.ivy.settlement.ethereum.model.event;

import org.ivy.settlement.ethereum.model.constants.EthLogEventEnum;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint16;
import org.web3j.abi.datatypes.generated.Uint64;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.tx.Contract;

/**
 * description:
 * @author emo
 */
public class AddChainEvent implements ManagerChainEvent {

    int chainId;

    int chainType;

    long comeIntoEffectHeight;

    long srcAddressSize;

    long srcHeight;

    String srcChainLib; // address

    String srcValidationLib; // address

    byte[] srcChainBridge;

    byte[] srcGenesis;

    public AddChainEvent(Uint16 chainId, Uint8 chainType, Uint64 comeIntoEffectHeight, Uint64 srcAddressSize, Uint64 srcHeight, Address srcChainLib, Address srcValidationLib, Bytes32 srcChainBridge, Bytes32 srcGenesis) {
        this.chainId = chainId.getValue().intValue();
        this.chainType = chainType.getValue().intValue();
        this.comeIntoEffectHeight = comeIntoEffectHeight.getValue().longValue();
        this.srcAddressSize = srcAddressSize.getValue().longValue();
        this.srcHeight = srcHeight.getValue().longValue();
        this.srcChainLib = srcChainLib.getValue();
        this.srcValidationLib = srcValidationLib.getValue();
        this.srcChainBridge = srcChainBridge.getValue();
        this.srcGenesis = srcGenesis.getValue();
    }

    public int getChainId() {
        return chainId;
    }

    public int getChainType() {
        return chainType;
    }

    public long getComeIntoEffectHeight() {
        return comeIntoEffectHeight;
    }

    public long getSrcAddressSize() {
        return srcAddressSize;
    }

    public long getSrcHeight() {
        return srcHeight;
    }

    public String getSrcChainLib() {
        return srcChainLib;
    }

    public String getSrcValidationLib() {
        return srcValidationLib;
    }

    public byte[] getSrcChainBridge() {
        return srcChainBridge;
    }

    public byte[] getSrcGenesis() {
        return srcGenesis;
    }

    public static AddChainEvent fromLog(Log log) {
        var eventValues = Contract.staticExtractEventParameters(EthLogEventEnum.ADD_CHAIN_EVENT.getEvent(), log);
        var nonIndexedValues = eventValues.getNonIndexedValues();
        return new AddChainEvent(
                (Uint16) nonIndexedValues.get(0),
                (Uint8) nonIndexedValues.get(1),
                (Uint64) nonIndexedValues.get(2),
                (Uint64) nonIndexedValues.get(3),
                (Uint64) nonIndexedValues.get(4),
                (Address) nonIndexedValues.get(5),
                (Address) nonIndexedValues.get(6),
                (Bytes32) nonIndexedValues.get(7),
                (Bytes32) nonIndexedValues.get(8)
        );
    }
}
