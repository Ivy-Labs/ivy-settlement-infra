package org.ivy.settlement.infrastructure.merkle;


import org.ivy.settlement.infrastructure.codec.borsh.Borsh;

import java.util.ArrayList;

/**
 * description:
 * @author taining
 */
public class MerkleProof implements Borsh {

    private long index;

    private ArrayList<byte[]> items;

    public MerkleProof(long index, ArrayList<byte[]> items) {
        this.index = index;
        this.items = items;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public ArrayList<byte[]> getItems() {
        return items;
    }

    public void setItems(ArrayList<byte[]> items) {
        this.items = items;
    }
}
