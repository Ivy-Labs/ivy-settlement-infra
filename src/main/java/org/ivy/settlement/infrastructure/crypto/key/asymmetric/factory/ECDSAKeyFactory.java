package org.ivy.settlement.infrastructure.crypto.key.asymmetric.factory;


import org.ivy.settlement.infrastructure.crypto.key.asymmetric.SecureKey;
import org.ivy.settlement.infrastructure.crypto.key.asymmetric.ec.ECKey;

import java.security.SecureRandom;

/**
 * 类ECKeyFactory.java的实现描述：
 *
 * @author xuhao
 */

public class ECDSAKeyFactory extends SecureKeyFactory {

    @Override
    public SecureKey fromRawPrivate(byte[] rawPrivKey, short shardingNumber) {
        return ECKey.fromRawPrivKeyBytes(rawPrivKey, shardingNumber);
    }

    @Override
    public SecureKey getInstance(SecureRandom secureRandom, short shardingNumber) {
        return new ECKey(secureRandom, shardingNumber);
    }
}
