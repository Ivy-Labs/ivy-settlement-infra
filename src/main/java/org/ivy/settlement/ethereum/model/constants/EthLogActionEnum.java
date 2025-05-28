package org.ivy.settlement.ethereum.model.constants;

import java.util.HashMap;
import java.util.Map;

import static org.ivy.settlement.infrastructure.anyhow.Assert.ensure;

/**
 * description:
 * @author carrot
 */
public enum EthLogActionEnum {

    ALL(0),

    EPOCH_CHANGE(1),

    UPDATE_VOTER(2),

    MANAGER_CHAIN(3),

    UPLOAD_BLOB(4),

    CROSS_CHAIN(5),

    INTERACTIVE(6);



    private int code;

    EthLogActionEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    private static final Map<Integer, EthLogActionEnum> byteToActionMap = new HashMap<>();

    static {
        for (var action : EthLogActionEnum.values()) {
            byteToActionMap.put(action.code, action);
        }
    }

    public static EthLogActionEnum fromCode(int code) {
        var res = byteToActionMap.get(code);
        ensure(res != null, "EthLogActionEnum un know code:{}", code);
        return res;
    }
}
