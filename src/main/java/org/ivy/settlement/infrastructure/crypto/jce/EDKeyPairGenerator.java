package org.ivy.settlement.infrastructure.crypto.jce;

import org.bouncycastle.jcajce.spec.EdDSAParameterSpec;

import java.security.*;

public final class EDKeyPairGenerator {

  public static final String ALGORITHM = "EdDSA";
  public static final String CURVE_NAME = EdDSAParameterSpec.Ed25519;

  private static final String algorithmAssertionMsg =
      "Assumed JRE supports ED25519 key pair generation";

  private static final String keySpecAssertionMsg =
      "Assumed correct key spec statically";

  private static final EdDSAParameterSpec ED25519_CURVE
      = new EdDSAParameterSpec(CURVE_NAME);

  private EDKeyPairGenerator() { }

  private static class Holder {
    private static final KeyPairGenerator INSTANCE;

    static {
      try {
        INSTANCE = KeyPairGenerator.getInstance(ALGORITHM);
        INSTANCE.initialize(ED25519_CURVE);
      } catch (NoSuchAlgorithmException ex) {
        throw new AssertionError(algorithmAssertionMsg, ex);
      } catch (InvalidAlgorithmParameterException ex) {
        throw new AssertionError(keySpecAssertionMsg, ex);
      }
    }
  }

  public static KeyPair generateKeyPair() {
    return Holder.INSTANCE.generateKeyPair();
  }


  public static KeyPairGenerator getInstance(final Provider provider, final SecureRandom random) {
    try {
      final KeyPairGenerator gen = KeyPairGenerator.getInstance(ALGORITHM, provider);
      gen.initialize(ED25519_CURVE, random);
      return gen;
    } catch (NoSuchAlgorithmException ex) {
      throw new AssertionError(algorithmAssertionMsg, ex);
    } catch (InvalidAlgorithmParameterException ex) {
      throw new AssertionError(keySpecAssertionMsg, ex);
    }
  }
}
