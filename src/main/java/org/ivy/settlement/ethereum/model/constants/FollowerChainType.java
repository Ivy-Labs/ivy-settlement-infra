package org.ivy.settlement.ethereum.model.constants;

import org.ivy.settlement.infrastructure.anyhow.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * description:
 * @author carrot
 */
public enum FollowerChainType {

    APP_CHAIN(0),

    CROSS_CHAIN(1);


    private int code;

    FollowerChainType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    private static final Map<Integer, FollowerChainType> byteToTypeMap = new HashMap<>();

    static {
        for (var action : FollowerChainType.values()) {
            byteToTypeMap.put(action.code, action);
        }
    }

    public static FollowerChainType fromCode(int code) {
        var res = byteToTypeMap.get(code);
        Assert.ensure(res != null, "FollowerChainType un know code:{}", code);
        return res;
    }

}
