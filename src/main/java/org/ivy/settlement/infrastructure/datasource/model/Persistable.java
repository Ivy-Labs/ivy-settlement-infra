package org.ivy.settlement.infrastructure.datasource.model;


import org.ivy.settlement.infrastructure.rlp.RLPModel;

/**
 * description:
 * @author carrot
 */
public abstract class Persistable extends RLPModel implements Valueable {

    public Persistable(byte[] rlpEncoded) {
        super(rlpEncoded);
    }

    @Override
    public byte[] valueBytes() {
        return this.rlpEncoded;
    }

    protected abstract byte[] rlpEncoded();

    protected abstract void rlpDecoded();
}
