package org.ivy.settlement.ethereum.model.constants;

/**
 * description:
 * @author carrot
 */
public enum ChainType {
    Beacon("beacon", 1),
    BSC("bsc", 2),
    ZKSYNC("zksync", 3),
    POLYGON("polygon", 4),
    ARBITRUM("arbitrum", 5),
    OPTIMISTIC("optimistic", 6),
    BTC("btc", 7);
    private String name;
    private long id;
    ChainType(String name, long id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public long getId() {
        return this.id;
    }

    @Override
    public String toString() {
        return "ChainType{" +
                "name='" + name +
                "id='" + id +
                '}';
    }
}
