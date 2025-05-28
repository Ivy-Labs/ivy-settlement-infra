package org.ivy.settlement.infrastructure.crypto.key.symmetric;


/**
 * 类DefaultCipherKey.java的实现描述：
 *
 * @author xuhao
 */

public class DefaultCipherKey extends CipherKey {

    @Override
    public byte[] encrypt(byte[] data) {
        return data;
    }

    @Override
    public byte[] decrypt(byte[] data) {
        return data;
    }

    @Override
    public byte[] getKeyBytes() {
        return null;
    }
}
