package org.ivy.settlement.ethereum.model.event;

import org.ivy.settlement.ethereum.model.constants.EthLogEventEnum;
import org.ivy.settlement.ethereum.model.event.interactive.InteractiveEvent;

/**
 * description:
 * @author carrot
 */
public abstract class UploadBlobEvent extends InteractiveEvent {

    long epoch;

    long height;

    public UploadBlobEvent(long epoch, long height) {
        this.epoch = epoch;
        this.height = height;
    }

    public long getEpoch() {
        return epoch;
    }

    public long getHeight() {
        return height;
    }

    public abstract int getStatus();

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
        return null;
    }
}
