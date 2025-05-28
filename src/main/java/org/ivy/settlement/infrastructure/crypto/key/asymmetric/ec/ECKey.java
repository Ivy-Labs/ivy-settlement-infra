package org.ivy.settlement.infrastructure.crypto.key.asymmetric.ec;

import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.util.encoders.Hex;
import org.ivy.settlement.infrastructure.crypto.CastleProvider;
import org.ivy.settlement.infrastructure.crypto.jce.ECKeyPairGenerator;
import org.ivy.settlement.infrastructure.crypto.key.asymmetric.SecureKey;
import org.ivy.settlement.infrastructure.crypto.key.asymmetric.SecureKeyType;
import org.ivy.settlement.infrastructure.crypto.key.asymmetric.SecurePublicKey;
import org.ivy.settlement.infrastructure.bytes.ByteUtil;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import static org.ivy.settlement.infrastructure.bytes.ByteUtil.bigIntegerToBytes;

/**
 * <p>Represents an elliptic curve public and (optionally) private key, usable for digital signatures but not encryption.
 * Creating a new ECKey with the empty constructor will generate a new random keypair. Other static methods can be used
 * when you already have the public or private parts. If you create a key with only the public part, you can check
 * signatures but not create them.</p>
 *
 * <p>The ECDSA algorithm supports <i>key recovery</i> in which a signature plus a couple of discriminator bits can
 * be reversed to find the public key used to calculate it. This can be convenient when you have a message and a
 * signature and want to find out who signed it, rather than requiring the user to provide the expected identity.</p>
 * <p>
 * This code is borrowed from the bitcoinj project and altered to fit Ethereum.<br>
 * See <a href="https://github.com/bitcoinj/bitcoinj/blob/df9f5a479d28c84161de88165917a5cffcba08ca/core/src/main/java/org/bitcoinj/core/ECKey.java">
 * bitcoinj on GitHub</a>.
 */
public class ECKey extends SecureKey implements Serializable {

    /**
     * The parameters of the secp256k1 curve that Ethereum uses.
     */
    public static final ECDomainParameters CURVE;

    public static final ECParameterSpec CURVE_SPEC;

    public static final String ALGORITHM = "EC";

    public static final BigInteger HALF_CURVE_ORDER;

    private final ECPrivateKeyParameters privKeyParams;

    private static final SecureRandom secureRandom;


    static {
        // All clients must agree on the curve to use by agreement. Ethereum uses secp256k1.
        X9ECParameters params = SECNamedCurves.getByName("secp256k1");
        CURVE = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
        CURVE_SPEC = new ECParameterSpec(params.getCurve(), params.getG(), params.getN(), params.getH());
        HALF_CURVE_ORDER = params.getN().shiftRight(1);
        secureRandom = new SecureRandom();
    }

    private final PrivateKey privKey;

    /**
     * Generates an entirely new keypair.
     * <p>
     * BouncyCastle will be used as the Java Security Provider
     */
    public ECKey() {
        this((short) 1);
    }

    /**
     * Generates an entirely new keypair.
     * <p>
     * BouncyCastle will be used as the Java Security Provider
     */
    public ECKey(short shardingNumber) {
        this(secureRandom, shardingNumber);
    }

    /**
     * Generates an entirely new keypair with the given {@link SecureRandom} object.
     * <p>
     * BouncyCastle will be used as the Java Security Provider
     *
     * @param secureRandom -
     */
    public ECKey(SecureRandom secureRandom, short shardingNumber) {
        this(CastleProvider.getBouncyInstance(), secureRandom, shardingNumber);
    }

    /**
     * Generate a new keypair using the given Java Security Provider.
     * <p>
     * All private key operations will use the provider.
     */
    public ECKey(Provider provider, SecureRandom secureRandom, short shardingNumber) {
        final KeyPairGenerator keyPairGen = ECKeyPairGenerator.getInstance(provider, secureRandom);
        final KeyPair keyPair = keyPairGen.generateKeyPair();

        this.privKey = keyPair.getPrivate();
        this.privKeyParams = new ECPrivateKeyParameters(((BCECPrivateKey) privKey).getD(), CURVE);

        final PublicKey pubKey = keyPair.getPublic();
        if (pubKey instanceof BCECPublicKey) {
            byte[] pubBytes = ((BCECPublicKey) pubKey).getQ().getEncoded(false);
            this.securePublicKey = SecurePublicKey.generateFromRaw(pubBytes, SecureKeyType.ECDSA.getCode(), shardingNumber);
        } else {
            throw new AssertionError(
                    "Expected Provider " + provider.getName() +
                            " to produce a subtype of ECPublicKey, found " + pubKey.getClass());
        }
    }


    /**
     * Pair a private key with a public EC point.
     * <p>
     * All private key operations will use the provider.
     */
    public ECKey(PrivateKey privKey, short shardingNumber) {
        if (privKey instanceof BCECPrivateKey) {
            this.privKey = privKey;
            BigInteger d = ((BCECPrivateKey) privKey).getD();
            this.privKeyParams = new ECPrivateKeyParameters(d, CURVE);

            byte[] pubBytes = CURVE.getG().multiply(d).getEncoded(false);
            this.securePublicKey = SecurePublicKey.generateFromRaw(pubBytes, SecureKeyType.ECDSA.getCode(), shardingNumber);
        } else {
            throw new IllegalArgumentException(
                    "Expected EC private key, given a private key object with class " +
                            privKey.getClass().toString() +
                            " and algorithm " + privKey.getAlgorithm());
        }
    }

    /**
     * Creates an ECKey given the private key only.
     *
     * @param rawPrivKeyBytes -
     * @return -
     */
    public static ECKey fromRawPrivKeyBytes(byte[] rawPrivKeyBytes, short shardingNumber) {
        return new ECKey(fromRawPrivKeyBytes(rawPrivKeyBytes), shardingNumber);
    }

    public byte[] getRawPrivKeyBytes() {
        if (privKey == null) {
            return null;
        } else if (privKey instanceof BCECPrivateKey) {
            return ByteUtil.bigIntegerToBytes(((BCECPrivateKey) privKey).getD(), 32);
        } else {
            return null;
        }
    }

    public byte[] sign(byte[] messageHash) {
        // No decryption of private key required.
        if (privKey == null)
            throw new MissingPrivateKeyException();
        try {
            ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
            signer.init(true, privKeyParams);
            BigInteger[] components = signer.generateSignature(messageHash);
            return StdDSAEncoder.encode(components[0], toCanonicalised(components[1]));
        } catch (Exception e) {
            logger.error("ECKey sign  failed, msgHash:{}.", Hex.toHexString(messageHash), e);
            throw new AssertionError("ECKey sign failed.", e);
        }
    }

    /* Convert a byte slice into a PrivateKey object
     */
    static PrivateKey fromRawPrivKeyBytes(byte[] rawPrivKey) {
        if (rawPrivKey == null) {
            return null;
        } else {
            try {
                BigInteger d = new BigInteger(1, rawPrivKey);
                return KeyFactory.getInstance(ALGORITHM, CastleProvider.getBouncyInstance())
                        .generatePrivate(new ECPrivateKeySpec(d, CURVE_SPEC));
            } catch (InvalidKeySpecException ex) {
                throw new AssertionError("Assumed correct ECDSA key spec statically", ex);
            } catch (NoSuchAlgorithmException ex) {
                throw new AssertionError("Assumed correct algorithm of ECDSA privKey", ex);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("pub:").append(Hex.toHexString(this.securePublicKey.getPubKey()));
        return b.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ECKey)) return false;

        ECKey ecKey = (ECKey) o;
        return privKey.equals(ecKey.privKey);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.securePublicKey.getPubKey());
    }

    // The order of the curve is the number of valid points that exist on that curve. If S is in the upper
    // half of the number of valid points, then bring it back to the lower half. Otherwise, imagine that
    //    N = 10
    //    s = 8, so (-8 % 10 == 2) thus both (r, 8) and (r, 2) are valid solutions.
    //    10 - 8 == 2, giving us always the latter solution, which is canonical.
    private BigInteger toCanonicalised(BigInteger s) {
        if (s.compareTo(HALF_CURVE_ORDER) > 0) {
            return CURVE.getN().subtract(s);
        } else {
            return s;
        }
    }

}
