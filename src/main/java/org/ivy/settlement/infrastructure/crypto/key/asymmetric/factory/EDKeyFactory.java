package org.ivy.settlement.infrastructure.crypto.key.asymmetric.factory;

import org.ivy.settlement.infrastructure.crypto.key.asymmetric.SecureKey;
import org.ivy.settlement.infrastructure.crypto.key.asymmetric.ed.EDKey;

import java.security.SecureRandom;

/**
 * 类EDKeyFactory.java的实现描述：
 *
 * @author xuhao
 */

public class EDKeyFactory extends SecureKeyFactory {
    @Override
    public SecureKey fromRawPrivate(byte[] rawPrivKey, short shardingNumber) {
        return EDKey.fromRawPrivate(rawPrivKey, shardingNumber);
    }

    @Override
    public SecureKey getInstance(SecureRandom secureRandom, short shardingNumber) {
        return new EDKey(secureRandom, shardingNumber);
    }
}
