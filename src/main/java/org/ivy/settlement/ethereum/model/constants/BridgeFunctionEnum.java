package org.ivy.settlement.ethereum.model.constants;

/**
 * description:
 * @author carrot
 */
public enum BridgeFunctionEnum {
    UploadBridgeBlk(
            1,
            "upload bridge blk",
            "0xe7d7bdf7"
    );
    private long id;
    private String name;
    private String funcSignature;

    <T> BridgeFunctionEnum(long id, String name, String funcSignature) {
        this.id = id;
        this.name = name;
        this.funcSignature = funcSignature;
    }

    public String getName() {
        return this.name;
    }

    public long getId() {
        return this.id;
    }
    public String getFuncSignature() {
        return this.funcSignature;
    }

    @Override
    public String toString() {
        return "BridgeEventEnum{" +
                "id='" + id +
                "name='" + name +
                "funcSignature='" + funcSignature +
                '}';
    }

    public static BridgeFunctionEnum queryByFuncSignature(String funcSignature) {
        for (BridgeFunctionEnum param : values()) {
            if (param.getFuncSignature().equals(funcSignature)) {
                return param;
            }
        }

        String errInfo = String.format("unknown eventSignature:[%s]", funcSignature);
        throw new RuntimeException(errInfo);
    }
}
