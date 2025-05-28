package org.ivy.settlement.ethereum.model.event.interactive;

import org.ivy.settlement.ethereum.model.constants.EthLogEventEnum;
import org.ivy.settlement.infrastructure.datasource.model.EthLogEvent;

/**
 * description:
 * @author carrot
 */
public abstract class InteractiveEvent implements EthLogEvent {

    public abstract String getId();

    public abstract boolean createInstant();

    public abstract byte[] buildInitContent();

    public abstract EthLogEventEnum getEthLogEvent();

    public boolean willTimeout() {
        return false;
    }
}
