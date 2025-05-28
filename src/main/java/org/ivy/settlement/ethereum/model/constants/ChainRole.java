package org.ivy.settlement.ethereum.model.constants;

/**
 * description:
 * @author carrot
 */
public enum ChainRole {
    MAIN("main"),
    FOLLOW("follow");
    private String des;
    ChainRole(String des) {
        this.des = des;
    }

    public String getDes() {
        return des;
    }

    @Override
    public String toString() {
        return "ChainRole{" +
                "des='" + des +
                '}';
    }
}
