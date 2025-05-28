package org.ivy.settlement.infrastructure.crypto.key.asymmetric.factory;


import org.ivy.settlement.infrastructure.crypto.key.asymmetric.SecureKey;

import java.security.SecureRandom;

/**
 * 类SecureKeyFactory.java的实现描述：
 *
 * @author xuhao
 */


public abstract class SecureKeyFactory {

    public abstract SecureKey fromRawPrivate(byte[] rawPrivKey, short shardingNumber);

    public abstract SecureKey getInstance(SecureRandom secureRandom, short shardingNumber);
}
