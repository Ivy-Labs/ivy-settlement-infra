package org.ivy.settlement.infrastructure.crypto.key.asymmetric.ec;

import org.ivy.settlement.infrastructure.bytes.ByteUtil;
import org.ivy.settlement.infrastructure.crypto.CastleProvider;
import org.ivy.settlement.infrastructure.crypto.HashUtil;
import org.ivy.settlement.infrastructure.crypto.jce.ECKeyFactory;
import org.ivy.settlement.infrastructure.crypto.jce.ECKeyPairGenerator;
import org.ivy.settlement.infrastructure.crypto.key.asymmetric.SecureKey;
import org.ivy.settlement.infrastructure.crypto.key.asymmetric.SecureKeyType;
import org.ivy.settlement.infrastructure.crypto.key.asymmetric.SecurePublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.asn1.sec.SECNamedCurves;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.asn1.x9.X9IntegerConverter;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.engines.AESEngine;
import org.spongycastle.crypto.modes.SICBlockCipher;
import org.spongycastle.crypto.params.*;
import org.spongycastle.crypto.signers.ECDSASigner;
import org.spongycastle.crypto.signers.HMacDSAKCalculator;
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.spongycastle.jce.spec.ECParameterSpec;
import org.spongycastle.jce.spec.ECPrivateKeySpec;
import org.spongycastle.math.ec.ECAlgorithms;
import org.spongycastle.math.ec.ECCurve;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

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
public class EthECKey extends SecureKey implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(EthECKey.class);

    private static final BigInteger SECP256K1N = new BigInteger("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141", 16);


    /**
     * The parameters of the secp256k1 curve that Ethereum uses.
     */
    public static final ECDomainParameters CURVE;

    public static final ECParameterSpec CURVE_SPEC;

    /**
     * Equal to CURVE.getN().shiftRight(1), used for canonicalising the S value of a signature.
     * ECDSA signatures are mutable in the sense that for a given (R, S) pair,
     * then both (R, S) and (R, N - S mod N) are valid signatures.
     * Canonical signatures are those where 1 <= S <= N/2
     * <p>
     * See https://github.com/bitcoin/bips/blob/master/bip-0062.mediawiki#Low_S_values_in_signatures
     */
    public static final BigInteger HALF_CURVE_ORDER;

    public static final EthECKey DUMMY;

    private static final SecureRandom secureRandom;

    private static final long serialVersionUID = -728224901792295832L;

    static {
        // All clients must agree on the curve to use by agreement. Ethereum uses secp256k1.
        X9ECParameters params = SECNamedCurves.getByName("secp256k1");
        CURVE = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
        CURVE_SPEC = new ECParameterSpec(params.getCurve(), params.getG(), params.getN(), params.getH());
        HALF_CURVE_ORDER = params.getN().shiftRight(1);
        secureRandom = new SecureRandom();
        DUMMY = fromPrivate(BigInteger.ONE, (short) 1);
    }

    // The two parts of the key. If "priv" is set, "pub" can always be calculated. If "pub" is set but not "priv", we
    // can only verify signatures not make them.
    // TODO: Redesign this class to use consistent internals and more efficient serialization.
    private final PrivateKey privKey;
    protected final ECPoint pub;


    // the Java Cryptographic Architecture provider to use for Signature
    // this is set along with the PrivateKey privKey and must be compatible
    // this provider will be used when selecting a Signature instance
    // https://docs.oracle.com/javase/8/docs/technotes/guides/security/SunProviders.html
    private final Provider provider;

    // Transient because it's calculated on demand.
    transient private byte[] pubKeyHash;
    /**
     * Generate a new keypair using the given Java Security Provider.
     * <p>
     * All private key operations will use the provider.
     */
    public EthECKey(Provider provider, SecureRandom secureRandom, short shardingNum) {
        this.provider = provider;
        final KeyPairGenerator keyPairGen = ECKeyPairGenerator.getInstance(provider, secureRandom);
        final KeyPair keyPair = keyPairGen.generateKeyPair();

        this.privKey = keyPair.getPrivate();

        final PublicKey pubKey = keyPair.getPublic();
        if (pubKey instanceof BCECPublicKey) {
            pub = ((BCECPublicKey) pubKey).getQ();
        } else {
            throw new AssertionError(
                    "Expected Provider " + provider.getName() +
                            " to produce a subtype of ECPublicKey, found " + pubKey.getClass());
        }
        this.securePublicKey = SecurePublicKey.generateFromRaw(pub.getEncoded(/* compressed */ false), SecureKeyType.ETH256K1.getCode(), shardingNum);

    }

    /**
     * Generates an entirely new keypair with the given {@link SecureRandom} object.
     * <p>
     * BouncyCastle will be used as the Java Security Provider
     *
     * @param secureRandom -
     */
    public EthECKey(SecureRandom secureRandom, short shardingNum) {
        this(CastleProvider.getSpongyInstance(), secureRandom, shardingNum);
    }

    /* Test if a generic private key is an EC private key
     *
     * it is not sufficient to check that privKey is a subtype of ECPrivateKey
     * as the SunPKCS11 Provider will return a generic PrivateKey instance
     * a fallback that covers this case is to check the key algorithm
     */
    private static boolean isECPrivateKey(PrivateKey privKey) {
        return privKey instanceof ECPrivateKey || privKey.getAlgorithm().equals("EC");
    }

    /**
     * Pair a private key with a public EC point.
     * <p>
     * All private key operations will use the provider.
     */
    public EthECKey(Provider provider, PrivateKey privKey, ECPoint pub, short shardingNum) {
        this.provider = provider;

        if (privKey == null || isECPrivateKey(privKey)) {
            this.privKey = privKey;
        } else {
            throw new IllegalArgumentException(
                    "Expected EC private key, given a private key object with class " +
                            privKey.getClass().toString() +
                            " and algorithm " + privKey.getAlgorithm());
        }

        if (pub == null) {
            throw new IllegalArgumentException("Public key may not be null");
        }

        if (pub.isInfinity()) {
            throw new IllegalArgumentException("Public key must not be a point at infinity, probably your private key is incorrect");
        }

        this.pub = pub;
        this.securePublicKey = SecurePublicKey.generateFromRaw(pub.getEncoded(/* compressed */ false), SecureKeyType.ETH256K1.getCode(), shardingNum);
    }

    /* Convert a BigInteger into a PrivateKey object
     */
    private static PrivateKey privateKeyFromBigInteger(BigInteger priv) {
        if (priv == null) {
            return null;
        } else {
            try {
                return ECKeyFactory
                        .getInstance(CastleProvider.getSpongyInstance())
                        .generatePrivate(new ECPrivateKeySpec(priv, CURVE_SPEC));
            } catch (InvalidKeySpecException ex) {
                throw new AssertionError("Assumed correct key spec statically");
            }
        }
    }

    /**
     * Pair a private key integer with a public EC point
     * <p>
     * BouncyCastle will be used as the Java Security Provider
     */
    public EthECKey(BigInteger priv, ECPoint pub, short shardingNum) {
        this(
                CastleProvider.getSpongyInstance(),
                privateKeyFromBigInteger(priv),
                pub,
                shardingNum
        );
    }

    /**
     * Utility for compressing an elliptic curve point. Returns the same point if it's already compressed.
     * See the ECKey class docs for a discussion of point compression.
     *
     * @param uncompressed -
     * @return -
     * @deprecated per-point compression property will be removed in Bouncy Castle
     */
    public static ECPoint compressPoint(ECPoint uncompressed) {
        return CURVE.getCurve().decodePoint(uncompressed.getEncoded(true));
    }

    /**
     * Utility for decompressing an elliptic curve point. Returns the same point if it's already compressed.
     * See the ECKey class docs for a discussion of point compression.
     *
     * @param compressed -
     * @return -
     * @deprecated per-point compression property will be removed in Bouncy Castle
     */
    public static ECPoint decompressPoint(ECPoint compressed) {
        return CURVE.getCurve().decodePoint(compressed.getEncoded(false));
    }

    /**
     * Creates an ECKey given the private key only.
     *
     * @param privKey -
     * @return -
     */
    public static EthECKey fromPrivate(BigInteger privKey, short shardingNum) {
        return new EthECKey(privKey, CURVE.getG().multiply(privKey), shardingNum);
    }

    /**
     * Creates an ECKey given the private key only.
     *
     * @param rawPrivKeyBytes -
     * @return -
     */
    public static EthECKey fromRawPrivKeyBytes(byte[] rawPrivKeyBytes, short shardingNum) {
        return fromPrivate(new BigInteger(1, rawPrivKeyBytes), shardingNum);
    }


    /**
     * Creates an ECKey that cannot be used for signing, only verifying signatures, from the given encoded point.
     * The compression state of pub will be preserved.
     *
     * @param pub -
     * @return -
     */
    public static EthECKey fromPublicOnly(byte[] pub, short shardingNum) {
        return new EthECKey(null, CURVE.getCurve().decodePoint(pub), shardingNum);
    }

    /**
     * Returns a copy of this key, but with the public point represented in uncompressed form. Normally you would
     * never need this: it's for specialised scenarios or when backwards compatibility in encoded form is necessary.
     *
     * @return -
     * @deprecated per-point compression property will be removed in Bouncy Castle
     */
    public EthECKey decompress() {
        if (!pub.isCompressed())
            return this;
        else
            return new EthECKey(this.provider, this.privKey, decompressPoint(pub), (short) this.securePublicKey.getShardingNumber());
    }

    /**
     * @deprecated per-point compression property will be removed in Bouncy Castle
     */
    public EthECKey compress() {
        if (pub.isCompressed())
            return this;
        else
            return new EthECKey(this.provider, this.privKey, compressPoint(pub), (short) this.securePublicKey.getShardingNumber());
    }

    /**
     * Returns true if this key doesn't have access to private key bytes. This may be because it was never
     * given any private key bytes to begin with (a watching key).
     *
     * @return -
     */
    public boolean isPubKeyOnly() {
        return privKey == null;
    }

    /**
     * Returns true if this key has access to private key bytes. Does the opposite of
     * {@link #isPubKeyOnly()}.
     *
     * @return -
     */
    public boolean hasPrivKey() {
        return privKey != null;
    }

    /**
     * Returns public key bytes from the given private key. To convert a byte array into a BigInteger, use <tt>
     * new BigInteger(1, bytes);</tt>
     *
     * @param privKey    -
     * @param compressed -
     * @return -
     */
    public static byte[] publicKeyFromPrivate(BigInteger privKey, boolean compressed) {
        ECPoint point = CURVE.getG().multiply(privKey);
        return point.getEncoded(compressed);
    }

    /**
     * Compute an address from an encoded public key.
     *
     * @param pubBytes an encoded (uncompressed) public key
     * @return 20-byte address
     */
    public static byte[] computeAddress(byte[] pubBytes) {
        return HashUtil.sha3omit12(
                Arrays.copyOfRange(pubBytes, 1, pubBytes.length));
    }

    /**
     * Compute an address from a public point.
     *
     * @param pubPoint a public point
     * @return 20-byte address
     */
    public static byte[] computeAddress(ECPoint pubPoint) {
        return computeAddress(pubPoint.getEncoded(/* uncompressed */ false));
    }

    /**
     * Gets the address form of the public key.
     *
     * @return 20-byte address
     */
    public byte[] getAddress() {
        if (pubKeyHash == null) {
            pubKeyHash = computeAddress(this.pub);
        }
        return pubKeyHash;
    }

    /**
     * Compute the encoded X, Y coordinates of a public point.
     * <p>
     * This is the encoded public key without the leading byte.
     *
     * @param pubPoint a public point
     * @return 64-byte X,Y point pair
     */
    public static byte[] pubBytesWithoutFormat(ECPoint pubPoint) {
        final byte[] pubBytes = pubPoint.getEncoded(/* uncompressed */ false);
        return Arrays.copyOfRange(pubBytes, 1, pubBytes.length);
    }

    /**
     * Recover the public key from an encoded node id.
     *
     * @param nodeId a 64-byte X,Y point pair
     */
    public static EthECKey fromNodeId(byte[] nodeId, short shardingNum) {
        check(nodeId.length == 64, "Expected a 64 byte node id");
        byte[] pubBytes = new byte[65];
        System.arraycopy(nodeId, 0, pubBytes, 1, nodeId.length);
        pubBytes[0] = 0x04; // uncompressed
        return EthECKey.fromPublicOnly(pubBytes, shardingNum);
    }

    /**
     * Gets the encoded public key value.
     *
     * @return 65-byte encoded public key
     */
    public byte[] doGetPubKey() {
        return this.securePublicKey.getRawPubKey();
    }


    /**
     * Returns whether this key is using the compressed form or not. Compressed pubkeys are only 33 bytes, not 64.
     *
     * @return -
     */
    public boolean isCompressed() {
        return pub.isCompressed();
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("pub:").append(ByteUtil.toHexString(pub.getEncoded(false)));
        return b.toString();
    }

    @Override
    public byte[] sign(byte[] messageHash) {
        if (privKey == null) {
            throw new MissingPrivateKeyException();
        }
        var ecdsaSig = signECDSA(messageHash);
        return ecdsaSig.toBase64().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Takes the keccak getHash (32 bytes) of data and returns the ECDSA signature
     *
     * @param messageHash -
     * @return -
     * @throws IllegalStateException if this ECKey does not have the private part.
     */
    public ECDSASignature signECDSA(byte[] messageHash) {
        ECDSASignature sig = doSign(messageHash);
        // Now we have to work backwards to figure out the recId needed to recover the signature.
        int recId = -1;
        byte[] thisKey = this.pub.getEncoded(/* compressed */ false);
        for (int i = 0; i < 4; i++) {
            byte[] k = EthECKey.recoverPubBytesFromSignature(i, sig, messageHash);
            if (k != null && Arrays.equals(k, thisKey)) {
                recId = i;
                break;
            }
        }
        if (recId == -1)
            throw new RuntimeException("Could not construct a recoverable key. This should never happen.");
        sig.v = (byte) (recId + 27);
        return sig;
    }


    /**
     * Signs the given getHash and returns the R and S components as BigIntegers
     * and put them in ECDSASignature
     *
     * @param input to signECDSA
     * @return ECDSASignature signature that contains the R and S components
     */
    private ECDSASignature doSign(byte[] input) {
        if (input.length != 32) {
            throw new IllegalArgumentException("Expected 32 byte input to ECDSA signature, not " + input.length);
        }
        // No decryption of private key required.
        if (privKey == null)
            throw new MissingPrivateKeyException();

        var signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
        var privKeyParams = new ECPrivateKeyParameters(((BCECPrivateKey) privKey).getD(), CURVE);
        signer.init(true, privKeyParams);
        var components = signer.generateSignature(input);
        return new ECDSASignature(components[0], components[1]).toCanonicalised();

    }

    /**
     * Given a piece of text and a message signature encoded in base64, returns an ECKey
     * containing the public key that was used to signECDSA it. This can then be compared to the expected public key to
     * determine if the signature was correct.
     *
     * @param messageHash     a piece of human readable text that was signed
     * @param signatureBase64 The Ethereum-format message signature in base64
     * @return -
     * @throws SignatureException If the public key could not be recovered or if there was a signature format error.
     */
    public static byte[] signatureToKeyBytes(byte[] messageHash, String signatureBase64) throws SignatureException {
        byte[] signatureEncoded;
        try {
            signatureEncoded = Base64.decode(signatureBase64);
        } catch (RuntimeException e) {
            // This is what you get back from Bouncy Castle if base64 doesn't decode :(
            throw new SignatureException("Could not decode base64", e);
        }
        // Parse the signature bytes into r/s and the selector value.
        if (signatureEncoded.length < 65)
            throw new SignatureException("Signature truncated, expected 65 bytes and got " + signatureEncoded.length);

        return signatureToKeyBytes(
                messageHash,
                ECDSASignature.fromComponents(
                        Arrays.copyOfRange(signatureEncoded, 1, 33),
                        Arrays.copyOfRange(signatureEncoded, 33, 65),
                        (byte) (signatureEncoded[0] & 0xFF)));
    }

    public static byte[] signatureToKeyBytes(byte[] messageHash, ECDSASignature sig) throws SignatureException {
        check(messageHash.length == 32, "messageHash argument has length " + messageHash.length);
        int header = sig.v;
        // The header byte: 0x1B = first key with even y, 0x1C = first key with odd y,
        //                  0x1D = second key with even y, 0x1E = second key with odd y
        if (header < 27 || header > 34)
            throw new SignatureException("Header byte out of range: " + header);
        if (header >= 31) {
            header -= 4;
        }
        int recId = header - 27;
        byte[] key = EthECKey.recoverPubBytesFromSignature(recId, sig, messageHash);
        if (key == null)
            throw new SignatureException("Could not recover public key from signature");
        return key;
    }

    /**
     * Compute the address of the key that signed the given signature.
     *
     * @param messageHash     32-byte getHash of message
     * @param signatureBase64 Base-64 encoded signature
     * @return 20-byte address
     */
    public static byte[] signatureToAddress(byte[] messageHash, String signatureBase64) throws SignatureException {
        return computeAddress(signatureToKeyBytes(messageHash, signatureBase64));
    }

    /**
     * Compute the address of the key that signed the given signature.
     *
     * @param messageHash 32-byte getHash of message
     * @param sig         -
     * @return 20-byte address
     */
    public static byte[] signatureToAddress(byte[] messageHash, ECDSASignature sig) throws SignatureException {
        return computeAddress(signatureToKeyBytes(messageHash, sig));
    }

    /**
     * Compute the key that signed the given signature.
     *
     * @param messageHash     32-byte getHash of message
     * @param signatureBase64 Base-64 encoded signature
     * @return ECKey
     */
    public static EthECKey signatureToKey(byte[] messageHash, String signatureBase64, short shardingNum) throws SignatureException {
        final byte[] keyBytes = signatureToKeyBytes(messageHash, signatureBase64);
        return EthECKey.fromPublicOnly(keyBytes, shardingNum);
    }

    /**
     * Compute the key that signed the given signature.
     *
     * @param messageHash 32-byte getHash of message
     * @param sig         -
     * @return ECKey
     */
    public static EthECKey signatureToKey(byte[] messageHash, ECDSASignature sig, short shardingNum) throws SignatureException {
        final byte[] keyBytes = signatureToKeyBytes(messageHash, sig);
        return EthECKey.fromPublicOnly(keyBytes, shardingNum);
    }


    /**
     * Decrypt cipher by AES in SIC(also know as CTR) mode
     *
     * @param cipher -proper cipher
     * @return decrypted cipher, equal length to the cipher.
     * @deprecated should not use EC private scalar value as an AES key
     */
    public byte[] decryptAES(byte[] cipher) {

        if (privKey == null) {
            throw new MissingPrivateKeyException();
        }
        if (!(privKey instanceof BCECPrivateKey)) {
            throw new UnsupportedOperationException("Cannot use the private key as an AES key");
        }


        AESEngine engine = new AESEngine();
        SICBlockCipher ctrEngine = new SICBlockCipher(engine);

        KeyParameter key = new KeyParameter(BigIntegers.asUnsignedByteArray(((BCECPrivateKey) privKey).getD()));
        ParametersWithIV params = new ParametersWithIV(key, new byte[16]);

        ctrEngine.init(false, params);

        int i = 0;
        byte[] out = new byte[cipher.length];
        while (i < cipher.length) {
            ctrEngine.processBlock(cipher, i, out, i);
            i += engine.getBlockSize();
            if (cipher.length - i < engine.getBlockSize())
                break;
        }

        // process left bytes
        if (cipher.length - i > 0) {
            byte[] tmpBlock = new byte[16];
            System.arraycopy(cipher, i, tmpBlock, 0, cipher.length - i);
            ctrEngine.processBlock(tmpBlock, 0, tmpBlock, 0);
            System.arraycopy(tmpBlock, 0, out, i, cipher.length - i);
        }

        return out;
    }


    /**
     * <p>Verifies the given ECDSA signature against the message bytes using the public key bytes.</p>
     *
     * <p>When using native ECDSA verification, data must be 32 bytes, and no element may be
     * larger than 520 bytes.</p>
     *
     * @param data      Hash of the data to verify.
     * @param signature signature.
     * @param pub       The public key bytes to use.
     * @return -
     */
    public static boolean verify(byte[] data, ECDSASignature signature, byte[] pub) {
        try {
            ECDSASigner signer = new ECDSASigner();
            ECPublicKeyParameters params = new ECPublicKeyParameters(CURVE.getCurve().decodePoint(pub), CURVE);
            signer.init(false, params);
            return signer.verifySignature(data, signature.r, signature.s);
        } catch (Exception npe) {
            // Bouncy Castle contains a bug that can cause NPEs given specially crafted signatures.
            // Those signatures are inherently invalid/attack sigs so we just fail them here rather than crash the thread.
            logger.error("Caught NPE inside bouncy castle", npe);
            return false;
        }
    }

    /**
     * Verifies the given ASN.1 encoded ECDSA signature against a getHash using the public key.
     *
     * @param data      Hash of the data to verify.
     * @param signature signature.
     * @param pub       The public key bytes to use.
     * @return -
     */
    public static boolean verify(byte[] data, byte[] signature, byte[] pub) {
        return verify(data, ECDSASignature.decodeFromBase64Bytes(signature), pub);
    }

    /**
     * Verifies the given ASN.1 encoded ECDSA signature against a getHash using the public key.
     *
     * @param data      Hash of the data to verify.
     * @param signature signature.
     * @return -
     */
    public boolean verify(byte[] data, byte[] signature) {
        return EthECKey.verify(data, signature, doGetPubKey());
    }

    /**
     * Returns true if this pubkey is canonical, i.e. the correct length taking into account compression.
     *
     * @return -
     */
    public boolean isPubKeyCanonical() {
        return isPubKeyCanonical(pub.getEncoded(/* uncompressed */ false));
    }


    /**
     * Returns true if the given pubkey is canonical, i.e. the correct length taking into account compression.
     *
     * @param pubkey -
     * @return -
     */
    public static boolean isPubKeyCanonical(byte[] pubkey) {
        if (pubkey[0] == 0x04) {
            // Uncompressed pubkey
            if (pubkey.length != 65)
                return false;
        } else if (pubkey[0] == 0x02 || pubkey[0] == 0x03) {
            // Compressed pubkey
            if (pubkey.length != 33)
                return false;
        } else
            return false;
        return true;
    }

    /**
     * <p>Given the components of a signature and a selector value, recover and return the public key
     * that generated the signature according to the algorithm in SEC1v2 section 4.1.6.</p>
     *
     * <p>The recId is an index from 0 to 3 which indicates which of the 4 possible keys is the correct one. Because
     * the key recovery operation yields multiple potential keys, the correct key must either be stored alongside the
     * signature, or you must be willing to try each recId in turn until you find one that outputs the key you are
     * expecting.</p>
     *
     * <p>If this method returns null it means recovery was not possible and recId should be iterated.</p>
     *
     * <p>Given the above two points, a correct usage of this method is inside a for loop from 0 to 3, and if the
     * output is null OR a key that is not the one you expect, you try again with the next recId.</p>
     *
     * @param recId       Which possible key to recover.
     * @param sig         the R and S components of the signature, wrapped.
     * @param messageHash Hash of the data that was signed.
     * @return 65-byte encoded public key
     */
    public static byte[] recoverPubBytesFromSignature(int recId, ECDSASignature sig, byte[] messageHash) {
        check(recId >= 0, "recId must be positive");
        check(sig.r.signum() >= 0, "r must be positive");
        check(sig.s.signum() >= 0, "s must be positive");
        check(messageHash != null, "messageHash must not be null");
        // 1.0 For j from 0 to h   (h == recId here and the loop is outside this function)
        //   1.1 Let x = r + jn
        BigInteger n = CURVE.getN();  // Curve order.
        BigInteger i = BigInteger.valueOf((long) recId / 2);
        BigInteger x = sig.r.add(i.multiply(n));
        //   1.2. Convert the integer x to an octet string X of length mlen using the conversion routine
        //        specified in Section 2.3.7, where mlen = ⌈(log2 p)/8⌉ or mlen = ⌈m/8⌉.
        //   1.3. Convert the octet string (16 set binary digits)||X to an elliptic curve point R using the
        //        conversion routine specified in Section 2.3.4. If this conversion routine outputs “invalid”, then
        //        do another iteration of Step 1.
        //
        // More concisely, what these points mean is to use X as a compressed public key.
        ECCurve.Fp curve = (ECCurve.Fp) CURVE.getCurve();
        BigInteger prime = curve.getQ();  // Bouncy Castle is not consistent about the letter it uses for the prime.
        if (x.compareTo(prime) >= 0) {
            // Cannot have point co-ordinates larger than this as everything takes place modulo Q.
            return null;
        }
        // Compressed keys require you to know an extra bit of data about the y-coord as there are two possibilities.
        // So it's encoded in the recId.
        ECPoint R = decompressKey(x, (recId & 1) == 1);
        //   1.4. If nR != point at infinity, then do another iteration of Step 1 (callers responsibility).
        if (!R.multiply(n).isInfinity())
            return null;
        //   1.5. Compute e from M using Steps 2 and 3 of ECDSA signature verification.
        BigInteger e = new BigInteger(1, messageHash);
        //   1.6. For k from 1 to 2 do the following.   (loop is outside this function via iterating recId)
        //   1.6.1. Compute a candidate public key as:
        //               Q = mi(r) * (sR - eG)
        //
        // Where mi(x) is the modular multiplicative inverse. We transform this into the following:
        //               Q = (mi(r) * s ** R) + (mi(r) * -e ** G)
        // Where -e is the modular additive inverse of e, that is z such that z + e = 0 (mod n). In the above equation
        // ** is point multiplication and + is point addition (the EC group operator).
        //
        // We can find the additive inverse by subtracting e from zero then taking the mod. For example the additive
        // inverse of 3 modulo 11 is 8 because 3 + 8 mod 11 = 0, and -3 mod 11 = 8.
        BigInteger eInv = BigInteger.ZERO.subtract(e).mod(n);
        BigInteger rInv = sig.r.modInverse(n);
        BigInteger srInv = rInv.multiply(sig.s).mod(n);
        BigInteger eInvrInv = rInv.multiply(eInv).mod(n);
        ECPoint.Fp q = (ECPoint.Fp) ECAlgorithms.sumOfTwoMultiplies(CURVE.getG(), eInvrInv, R, srInv);
        // result sanity check: point must not be at infinity
        if (q.isInfinity())
            return null;
        return q.getEncoded(/* compressed */ false);
    }

    /**
     * @param recId       Which possible key to recover.
     * @param sig         the R and S components of the signature, wrapped.
     * @param messageHash Hash of the data that was signed.
     * @return 20-byte address
     */
    public static byte[] recoverAddressFromSignature(int recId, ECDSASignature sig, byte[] messageHash) {
        final byte[] pubBytes = recoverPubBytesFromSignature(recId, sig, messageHash);
        if (pubBytes == null) {
            return null;
        } else {
            return computeAddress(pubBytes);
        }
    }

    /**
     * @param recId       Which possible key to recover.
     * @param sig         the R and S components of the signature, wrapped.
     * @param messageHash Hash of the data that was signed.
     * @return ECKey
     */
    public static EthECKey recoverFromSignature(int recId, ECDSASignature sig, byte[] messageHash, short shardingNum) {
        final byte[] pubBytes = recoverPubBytesFromSignature(recId, sig, messageHash);
        if (pubBytes == null) {
            return null;
        } else {
            return EthECKey.fromPublicOnly(pubBytes, shardingNum);
        }
    }


    /**
     * Decompress a compressed public key (x co-ord and low-bit of y-coord).
     *
     * @param xBN  -
     * @param yBit -
     * @return -
     */
    private static ECPoint decompressKey(BigInteger xBN, boolean yBit) {
        X9IntegerConverter x9 = new X9IntegerConverter();
        byte[] compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(CURVE.getCurve()));
        compEnc[0] = (byte) (yBit ? 0x03 : 0x02);
        return CURVE.getCurve().decodePoint(compEnc);
    }

    /**
     * Returns a 32 byte array containing the private key, or null if the key is encrypted or public only
     *
     * @return -
     */
    public byte[] getRawPrivKeyBytes() {
        if (privKey == null) {
            return null;
        } else if (privKey instanceof BCECPrivateKey) {
            return ByteUtil.bigIntegerToBytes(((BCECPrivateKey) privKey).getD(), 32);
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof EthECKey)) return false;

        EthECKey ecKey = (EthECKey) o;

        if (privKey != null && !privKey.equals(ecKey.privKey)) return false;
        if (pub != null && !pub.equals(ecKey.pub)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(doGetPubKey());
    }


    private static void check(boolean test, String message) {
        if (!test) throw new IllegalArgumentException(message);
    }

    /**
     * Groups the two components that make up a signature, and provides a way to encode to Base64 form, which is
     * how ECDSA signatures are represented when embedded in other data structures in the Ethereum protocol. The raw
     * components can be useful for doing further EC maths on them.
     */
    public static class ECDSASignature {
        /**
         * The two components of the signature.
         */
        public final BigInteger r, s;
        public byte v;

        /**
         * Constructs a signature with the given components. Does NOT automatically canonicalise the signature.
         *
         * @param r -
         * @param s -
         */
        public ECDSASignature(BigInteger r, BigInteger s) {
            this.r = r;
            this.s = s;
        }

        /**
         * t
         *
         * @param r
         * @param s
         * @return -
         */
        private static ECDSASignature fromComponents(byte[] r, byte[] s) {
            return new ECDSASignature(new BigInteger(1, r), new BigInteger(1, s));
        }

        /**
         * @param r -
         * @param s -
         * @param v -
         * @return -
         */
        public static ECDSASignature fromComponents(byte[] r, byte[] s, byte v) {
            ECDSASignature signature = fromComponents(r, s);
            signature.v = v;
            return signature;
        }

        public boolean validateComponents() {
            return validateComponents(r, s, v);
        }

        public static boolean validateComponents(BigInteger r, BigInteger s, byte v) {

            if (v != 27 && v != 28) return false;

            if (isLessThan(r, BigInteger.ONE)) return false;
            if (isLessThan(s, BigInteger.ONE)) return false;

            if (!isLessThan(r, SECP256K1N)) return false;
            if (!isLessThan(s, SECP256K1N)) return false;

            return true;
        }


        public static ECDSASignature decodeFromBase64Bytes(byte[] base64Signature) {
            byte[] signatureEncoded;
            try {
                signatureEncoded = Base64.decode(base64Signature);
            } catch (RuntimeException e) {
                // This is what you get back from Bouncy Castle if base64 doesn't decode :(
                throw new RuntimeException("Could not decode base64", e);
            }

            return ECDSASignature.fromComponents(
                    Arrays.copyOfRange(signatureEncoded, 1, 33),
                    Arrays.copyOfRange(signatureEncoded, 33, 65),
                    (byte) (signatureEncoded[0] & 0xFF));
        }


        /**
         * Will automatically adjust the S component to be less than or equal to half the curve order, if necessary.
         * This is required because for every signature (r,s) the signature (r, -s (mod N)) is a valid signature of
         * the same message. However, we dislike the ability to modify the bits of a Ethereum transaction after it's
         * been signed, as that violates various assumed invariants. Thus in future only one of those forms will be
         * considered legal and the other will be banned.
         *
         * @return -
         */
        public ECDSASignature toCanonicalised() {
            if (s.compareTo(HALF_CURVE_ORDER) > 0) {
                // The order of the curve is the number of valid points that exist on that curve. If S is in the upper
                // half of the number of valid points, then bring it back to the lower half. Otherwise, imagine that
                //    N = 10
                //    s = 8, so (-8 % 10 == 2) thus both (r, 8) and (r, 2) are valid solutions.
                //    10 - 8 == 2, giving us always the latter solution, which is canonical.
                return new ECDSASignature(r, CURVE.getN().subtract(s));
            } else {
                return this;
            }
        }

        /**
         * @return -
         */
        public String toBase64() {
            byte[] sigData = new byte[65];  // 1 header + 32 bytes for R + 32 bytes for S
            sigData[0] = v;
            System.arraycopy(ByteUtil.bigIntegerToBytes(this.r, 32), 0, sigData, 1, 32);
            System.arraycopy(ByteUtil.bigIntegerToBytes(this.s, 32), 0, sigData, 33, 32);
            return new String(Base64.encode(sigData), Charset.forName("UTF-8"));
        }

        public byte[] toByteArray() {
            final byte fixedV = this.v >= 27
                    ? (byte) (this.v - 27)
                    : this.v;

            return ByteUtil.merge(
                    ByteUtil.bigIntegerToBytes(this.r, 32),
                    ByteUtil.bigIntegerToBytes(this.s, 32),
                    new byte[]{fixedV});
        }

        public String toHex() {
            return Hex.toHexString(toByteArray());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ECDSASignature signature = (ECDSASignature) o;

            if (!r.equals(signature.r)) return false;
            if (!s.equals(signature.s)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = r.hashCode();
            result = 31 * result + s.hashCode();
            return result;
        }
    }

    public static boolean isLessThan(BigInteger valueA, BigInteger valueB){
        return valueA.compareTo(valueB) < 0;
    }


    public static void main(String[] args) {
        EthECKey ecKey = EthECKey.fromRawPrivKeyBytes(Hex.decode("77ee7a385f1e653966908d580eba32a090ddadf862633c329d5c73dfdfb54f6f"), (short) 1);
        String msg = "hello";
        byte[] msgHash = HashUtil.sha3(msg.getBytes());
        byte[] sign = ecKey.sign(msgHash);
        System.out.println("sign:" + Hex.toHexString(sign));
        System.out.println(sign.length);
        boolean verifyRes = ecKey.verify(msgHash, sign);
        System.out.println("verify result:" + verifyRes);
    }
}
