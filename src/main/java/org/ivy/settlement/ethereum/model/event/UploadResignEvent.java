package org.ivy.settlement.ethereum.model.event;

import org.ivy.settlement.ethereum.model.constants.EthLogEventEnum;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.tx.Contract;

import static org.ivy.settlement.ethereum.model.LatestUploadBlobState.UPLOAD_RESIGN;

/**
 * description:
 * @author emo
 */
public class UploadResignEvent extends UploadBlobEvent {


    public UploadResignEvent(Uint256 epoch, Uint256 height) {
        super(epoch.getValue().longValue(), height.getValue().longValue());
    }

    @Override
    public int getStatus() {
        return UPLOAD_RESIGN;
    }

    public static UploadResignEvent fromLog(Log log) {
        var eventValues = Contract.staticExtractEventParameters(EthLogEventEnum.UPLOAD_RESIGN_EVENT.getEvent(), log);
        var nonIndexedValues = eventValues.getNonIndexedValues();
        return new UploadResignEvent(
                (Uint256) nonIndexedValues.get(0),
                (Uint256) nonIndexedValues.get(1)
        );
    }
}
