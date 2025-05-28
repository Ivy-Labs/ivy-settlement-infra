package org.ivy.settlement.infrastructure.datasource.model;

/**
 * description:
 * @author carrot
 */
public class DefaultValueable extends Persistable {

    public DefaultValueable(byte[] rlpEncoded) {
        super(rlpEncoded);
    }

    @Override
    protected byte[] rlpEncoded() {
        return this.rlpEncoded;
    }

    @Override
    protected void rlpDecoded() {
        // do noting
    }
}
