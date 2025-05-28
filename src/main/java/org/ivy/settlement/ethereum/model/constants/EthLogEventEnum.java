package org.ivy.settlement.ethereum.model.constants;

import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.generated.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ivy.settlement.infrastructure.anyhow.Assert.ensure;

/**
 * description:
 * @author carrot
 */
public enum EthLogEventEnum {
    UPLOAD_SUCCESS_EVENT(EthLogActionEnum.UPLOAD_BLOB, 1, new Event("UploadBlobEvent", List.of(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint64>() {}, new TypeReference<Bytes32>() {}, new TypeReference<DynamicBytes>() {}, new TypeReference<Uint256>() {}))),
    UPLOAD_RESIGN_EVENT(EthLogActionEnum.UPLOAD_BLOB, 2, new Event("UploadResignEvent", List.of(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Bytes32>() {}, new TypeReference<DynamicBytes>() {}))),
    VOTER_UPDATE_EVENT(EthLogActionEnum.UPDATE_VOTER, 1, new Event("VoterUpdateEvent", List.of(new TypeReference<Uint8>() {}, new TypeReference<Uint8>() {}, new TypeReference<Bytes32>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}))),
    EPOCH_CHANGE_EVENT(EthLogActionEnum.EPOCH_CHANGE, 1, new Event("EpochChangeEvent", List.of(new TypeReference<Uint8>() {}, new TypeReference<Uint8>() {}, new TypeReference<Bytes32>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}))),
    ADD_CHAIN_EVENT(EthLogActionEnum.MANAGER_CHAIN, 1, new Event("AddChainEvent", List.of(new TypeReference<Uint16>() {}, new TypeReference<Uint8>() {},  new TypeReference<Uint64>() {}, new TypeReference<Uint64>() {}, new TypeReference<Uint64>() {}, new TypeReference<Address>() {}, new TypeReference<Address>() {}, new TypeReference<Bytes32>() {}, new TypeReference<Bytes32>() {}))),
    REMOVE_CHAIN_EVENT(EthLogActionEnum.MANAGER_CHAIN, 2, new Event("RemoveChainEvent", List.of(new TypeReference<Uint16>() {},  new TypeReference<Uint64>() {}))),
    EVM_CHALLENGE_EVENT(EthLogActionEnum.INTERACTIVE, 1, new Event("EvmChallengeEvent", List.of(new TypeReference<Uint16>() {}, new TypeReference<Uint64>() {}))),
    SAFETY_WAR_EVENT(EthLogActionEnum.INTERACTIVE, 3, new Event("SafetyWarEvent", List.of(new TypeReference<Uint16>() {}, new TypeReference<Uint64>() {}))),
    CROSS_CHAIN_EVENT(EthLogActionEnum.CROSS_CHAIN, 1, new Event("CrossChainEvent", List.of(new TypeReference<Uint16>() {}, new TypeReference<Uint64>() {})));


    private EthLogActionEnum actionEnum;

    private int id;

    private Event event;

    <T> EthLogEventEnum(EthLogActionEnum actionEnum, int id, Event event) {

        this.id = id;
        this.actionEnum = actionEnum;
        this.event = event;
    }

    public int getId() {
        return this.id;
    }

    public EthLogActionEnum getActionEnum() {
        return actionEnum;
    }

    public Event getEvent() {
        return this.event;
    }

    @Override
    public String toString() {
        return "BridgeEventEnum{" +
                "id='" + id +
                "actionEnum='" + actionEnum +
                "name='" + this.event.getName() +
                '}';
    }

    public static EthLogEventEnum queryEventByEventSignature(String eventSignature) {
        for (EthLogEventEnum param : values()) {
            if (EventEncoder.encode(param.getEvent()).equals(eventSignature)) {
                return param;
            }
        }

        String errInfo = String.format("unknown eventSignature:[%s]", eventSignature);
        throw new RuntimeException(errInfo);
    }

    private static final Map<Integer,Map<Integer, EthLogEventEnum>> actionMap = new HashMap<>();

    static {
        for (var event : EthLogEventEnum.values()) {
            actionMap.computeIfAbsent(event.getActionEnum().getCode(), k -> new HashMap<>())
                    .put(event.getId(), event);
        }
    }

    public static EthLogEventEnum fromCode(int actionCode, int eventCode) {
        var eventMap = actionMap.get(actionCode);
        ensure(eventMap != null, "EthLogActionEnum un know action code:{}", actionCode);
        var res = eventMap.get(eventCode);
        ensure(res != null, "EthLogActionEnum un know event code:{}", eventCode);
        return res;
    }
}
