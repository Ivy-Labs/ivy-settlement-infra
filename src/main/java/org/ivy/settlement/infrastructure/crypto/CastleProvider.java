package org.ivy.settlement.infrastructure.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Provider;
import java.security.Security;

public final class CastleProvider {

    private static class Holder {

        private static final Provider SPONGY_INSTANCE;

        private static final Provider BOUNCY_INSTANCE;


        static {

            SPONGY_INSTANCE = new org.spongycastle.jce.provider.BouncyCastleProvider();

            BOUNCY_INSTANCE = new BouncyCastleProvider();

            SPONGY_INSTANCE.put("MessageDigest.ETH-KECCAK-256", "cryptohash.crypto.infrastructure.org.ivy.settlement.Keccak256");

            SPONGY_INSTANCE.put("MessageDigest.ETH-KECCAK-256-LIGHT", "cryptohash.crypto.infrastructure.org.ivy.settlement.Keccak256Light");

            SPONGY_INSTANCE.put("MessageDigest.ETH-KECCAK-512", "cryptohash.crypto.infrastructure.org.ivy.settlement.Keccak512");

            //jdk1.8 此处替换是为了确保java.security中的provider列表正确（即用BC库替换SunEC）。
            // 如果不删除SunEC，只在表尾加BC库，那么SunEC顺序在BC库之前，结果是在国密tls中，优先使用SunEC识别国密曲线，导致识别失败。，
            // 如果在表头添加BC库，虽然能解决上述问题，但会导致SM4生成失败（具体原因待确认）。故只能采取替换策略。
            //Security.removeProvider("SunEC");
            //Security.insertProviderAt(BOUNCY_INSTANCE, 3);

            Security.addProvider(BOUNCY_INSTANCE);
            Security.addProvider(SPONGY_INSTANCE);
        }
    }

    public static Provider getSpongyInstance() {
        return Holder.SPONGY_INSTANCE;
    }

    public static Provider getBouncyInstance() {
        return Holder.BOUNCY_INSTANCE;
    }

}
