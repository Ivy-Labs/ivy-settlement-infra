package org.ivy.settlement.ethereum.model.event;

import org.ivy.settlement.ethereum.model.LatestUploadBlobState;
import org.ivy.settlement.ethereum.model.constants.EthLogEventEnum;
import org.web3j.abi.datatypes.BytesType;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint64;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.tx.Contract;

import java.util.List;

/**
 * description:
 * @author emo
 */
public class UploadSuccessEvent extends UploadBlobEvent {

    byte[] beaconRootId;

    long txNums;

    byte[] root;

    long signerSet;

    List<byte[]> blobHashes;

    public UploadSuccessEvent(byte[] beaconRootId, Uint256 epoch, Uint256 height, Uint64 txNums, Bytes32 root, Uint256 signerSet, DynamicArray<DynamicBytes> blobHashes) {
        super(epoch.getValue().longValue(), height.getValue().longValue());
        this.beaconRootId = beaconRootId;
        this.txNums = txNums.getValue().longValue();
        this.root = root.getValue();
        this.signerSet = signerSet.getValue().longValue();
        this.blobHashes = blobHashes.getValue().stream().map(BytesType::getValue).toList();
    }

    public byte[] getBeaconRootId() {
        return beaconRootId;
    }

    public long getTxNums() {
        return txNums;
    }

    public byte[] getRoot() {
        return root;
    }


    public long getSignerSet() {
        return signerSet;
    }

    public List<byte[]> getBlobHashes() {
        return blobHashes;
    }

    public static UploadSuccessEvent fromLog(Log log, byte[] beaconRootId) {
        var eventValues = Contract.staticExtractEventParameters(EthLogEventEnum.UPLOAD_SUCCESS_EVENT.getEvent(), log);
        var nonIndexedValues = eventValues.getNonIndexedValues();
        return new UploadSuccessEvent(
                beaconRootId,
                (Uint256) nonIndexedValues.get(1),
                (Uint256) nonIndexedValues.get(2),
                (Uint64) nonIndexedValues.get(3),
                (Bytes32) nonIndexedValues.get(4),
                (Uint256) nonIndexedValues.get(5),
                (DynamicArray<DynamicBytes>) nonIndexedValues.get(6)
                );
    }

    @Override
    public int getStatus() {
        return LatestUploadBlobState.UPLOAD_SUCCESS;
    }

    @Override
    public boolean createInstant() {
        return true;
    }

    @Override
    public byte[] buildInitContent() {
        return new byte[0];
    }


}
