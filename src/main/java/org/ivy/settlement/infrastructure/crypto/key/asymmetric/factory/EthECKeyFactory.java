package org.ivy.settlement.infrastructure.crypto.key.asymmetric.factory;

import org.ivy.settlement.infrastructure.crypto.key.asymmetric.SecureKey;
import org.ivy.settlement.infrastructure.crypto.key.asymmetric.ec.EthECKey;

import java.security.SecureRandom;

/**
 * EthECKeyFactory.java description：
 *
 * @Author laiyiyu
 */
public class EthECKeyFactory extends SecureKeyFactory {

    @Override
    public SecureKey fromRawPrivate(byte[] rawPrivKeyBytes, short shardingNumber) {
        return EthECKey.fromRawPrivKeyBytes(rawPrivKeyBytes, shardingNumber);
    }


    @Override
    public SecureKey getInstance(SecureRandom secureRandom, short shardingNumber) {
        return new EthECKey(secureRandom, shardingNumber);
    }
}
