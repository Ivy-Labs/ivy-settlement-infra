package org.ivy.settlement.ethereum.model.event;

import org.ivy.settlement.ethereum.model.constants.EthLogActionEnum;
import org.ivy.settlement.ethereum.model.constants.EthLogEventEnum;
import org.ivy.settlement.ethereum.model.event.interactive.InteractiveEvent;
import org.ivy.settlement.ethereum.model.event.interactive.challenge.EVMChallengeEvent;
import org.ivy.settlement.ethereum.model.event.interactive.war.SafetyWarEvent;
import org.ivy.settlement.infrastructure.datasource.model.CrossChainEvent;
import org.ivy.settlement.infrastructure.datasource.model.EthLogEvent;
import org.web3j.protocol.core.methods.response.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * @author carrot
 */
public class SettlementLogEventCollector {

    Long number;

    byte[] beaconRootId;

    byte[] blockHash;

    byte[] receiptRoot;

    List<UploadBlobEvent> uploadBlobEvents;
    List<EthCrossChainEvent> ethCrossChainEvents;

    List<EthLogEvent> allLogEvents;


    public SettlementLogEventCollector(Long number, byte[] beaconRootId) {
        this.number = number;
        this.beaconRootId = beaconRootId;
    }

    public void appendLog(Log log) {
        EthLogEventEnum eventEnum = EthLogEventEnum.queryEventByEventSignature(log.getTopics().get(0));
        switch (eventEnum) {
            case UPLOAD_SUCCESS_EVENT -> appendLogEvents(UploadSuccessEvent.fromLog(log, this.beaconRootId));
            case UPLOAD_RESIGN_EVENT -> appendLogEvents(UploadResignEvent.fromLog(log));
            case EPOCH_CHANGE_EVENT -> appendLogEvents(EpochChangeEvent.fromLog(log));
            case VOTER_UPDATE_EVENT -> appendLogEvents(VoterUpdateEvent.fromLog(log));
            case ADD_CHAIN_EVENT -> appendLogEvents(AddChainEvent.fromLog(log));
            case REMOVE_CHAIN_EVENT -> appendLogEvents(RemoveChainEvent.fromLog(log));
            case CROSS_CHAIN_EVENT -> appendLogEvents(EthCrossChainEvent.fromLog(log));
            case EVM_CHALLENGE_EVENT -> appendLogEvents(EVMChallengeEvent.fromLog(log));
            case SAFETY_WAR_EVENT -> appendLogEvents(SafetyWarEvent.fromLog(log));
        }
    }

    private void appendLogEvents(EthLogEvent event) {
        if (this.allLogEvents == null) {
            this.allLogEvents = new ArrayList<>(8);
        }
        this.allLogEvents.add(event);
    }


    public Long getNumber() {
        return number;
    }


    public List<UploadBlobEvent> getUploadBlobEvents() {
        if (uploadBlobEvents != null) return uploadBlobEvents;
        uploadBlobEvents = (List<UploadBlobEvent>) getEvents(EthLogActionEnum.UPLOAD_BLOB);
        return uploadBlobEvents;
    }


    public List<EthCrossChainEvent> getCrossChainEvents() {
        if (ethCrossChainEvents != null) return ethCrossChainEvents;
        ethCrossChainEvents = (List<EthCrossChainEvent>) getEvents(EthLogActionEnum.CROSS_CHAIN);
        return ethCrossChainEvents;
    }



    public List<? extends EthLogEvent> getEvents(EthLogActionEnum actionEnum) {
        return switch (actionEnum) {
            case ALL -> this.allLogEvents;
            case EPOCH_CHANGE -> this.allLogEvents.stream().filter(e -> e instanceof EpochChangeEvent).toList();
            case MANAGER_CHAIN -> this.allLogEvents.stream().filter(e -> e instanceof ManagerChainEvent).toList();
            case UPDATE_VOTER -> this.allLogEvents.stream().filter(e -> e instanceof VoterUpdateEvent).toList();
            case UPLOAD_BLOB -> this.allLogEvents.stream().filter(e -> e instanceof UploadBlobEvent).toList();
            case INTERACTIVE -> this.allLogEvents.stream().filter(e -> e instanceof InteractiveEvent).toList();
            case CROSS_CHAIN -> this.allLogEvents.stream().filter(e -> e instanceof CrossChainEvent).toList();
        };
    }

    public List<EthLogEvent> getAllLogEvents() {
        return this.allLogEvents;
    }

    public void settlement(byte[] blockHash, byte[] receiptRoot) {
        this.blockHash = blockHash;
        this.receiptRoot = receiptRoot;
    }

    public byte[] getBeaconRootId() {
        return beaconRootId;
    }

    public byte[] getBlockHash() {
        return blockHash;
    }

    public byte[] getReceiptRoot() {
        return receiptRoot;
    }

    public boolean isSettlement() {
        return this.blockHash != null && this.receiptRoot != null;
    }
}
