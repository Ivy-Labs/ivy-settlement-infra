package org.ivy.settlement.infrastructure.crypto;


import org.ivy.settlement.infrastructure.rlp.RLP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.Digest;
import org.spongycastle.crypto.digests.RIPEMD160Digest;
import org.spongycastle.crypto.digests.SHA3Digest;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.Random;

import static java.util.Arrays.copyOfRange;
import static org.ivy.settlement.infrastructure.bytes.ByteUtil.EMPTY_BYTE_ARRAY;

/**
 * description:
 * @author carrot
 */
public class HashUtil {

    private static final Logger LOG = LoggerFactory.getLogger(HashUtil.class);

    public static final byte[] EMPTY_DATA_HASH;
    public static final byte[] EMPTY_LIST_HASH;
    public static final byte[] EMPTY_TRIE_HASH;

    public static final Provider CRYPTO_PROVIDER;

    public static final String HASH_256_ALGORITHM_NAME;
    public static final String HASH_256_LIGHT_ALGORITHM_NAME;
    public static final String HASH_512_ALGORITHM_NAME;

    static {
        CastleProvider.getSpongyInstance();
        //Security.addProvider(CastleProvider.getSpongyInstance());
        CRYPTO_PROVIDER = Security.getProvider("SC");
        HASH_256_ALGORITHM_NAME = "ETH-KECCAK-256";
        HASH_256_LIGHT_ALGORITHM_NAME = "ETH-KECCAK-256-LIGHT";
        HASH_512_ALGORITHM_NAME = "ETH-KECCAK-512";
        EMPTY_DATA_HASH = sha3(EMPTY_BYTE_ARRAY);
        EMPTY_LIST_HASH = sha3(RLP.encodeList());
        EMPTY_TRIE_HASH = sha3(RLP.encodeElement(EMPTY_BYTE_ARRAY));
    }

    /**
     * @param input
     *            - data for hashing
     * @return - sha256 getHash of the data
     */
    public static byte[] sha256(byte[] input) {
        try {
            MessageDigest sha256digest = MessageDigest.getInstance("SHA-256");
            return sha256digest.digest(input);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Can't find such algorithm", e);
            throw new RuntimeException(e);
        }
    }

    public static byte[] sha3(byte[] input) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(HASH_256_ALGORITHM_NAME, CRYPTO_PROVIDER);
            digest.update(input);
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Can't find such algorithm", e);
            throw new RuntimeException(e);
        }

    }

    public static byte[] sha3Light(byte[] input) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(HASH_256_LIGHT_ALGORITHM_NAME, CRYPTO_PROVIDER);
            digest.update(input);
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Can't find such algorithm", e);
            throw new RuntimeException(e);
        }

    }



    public static byte[] sha3Dynamic(byte[]...inputs) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(HASH_256_ALGORITHM_NAME, CRYPTO_PROVIDER);

            for (byte[] input: inputs) {
                digest.update(input);
            }
            return digest.digest();
        } catch (Exception e) {
            LOG.error("sha3Dynamic error!", e);
            throw new RuntimeException(e);
        }

    }

    public static byte[] sha3(byte[] input1, byte[] input2) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(HASH_256_ALGORITHM_NAME, CRYPTO_PROVIDER);
            digest.update(input1, 0, input1.length);
            digest.update(input2, 0, input2.length);
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Can't find such algorithm", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * hashing chunk of the data
     *
     * @param input
     *            - data for getHash
     * @param start
     *            - start of hashing chunk
     * @param length
     *            - length of hashing chunk
     * @return - keccak getHash of the chunk
     */
    public static byte[] sha3(byte[] input, int start, int length) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(HASH_256_ALGORITHM_NAME, CRYPTO_PROVIDER);
            digest.update(input, start, length);
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Can't find such algorithm", e);
            throw new RuntimeException(e);
        }
    }

    public static byte[] sha512(byte[] input) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(HASH_512_ALGORITHM_NAME, CRYPTO_PROVIDER);
            digest.update(input);
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Can't find such algorithm", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * @param data
     *            - message to getHash
     * @return - reipmd160 getHash of the message
     */
    public static byte[] ripemd160(byte[] data) {
        Digest digest = new RIPEMD160Digest();
        if (data != null) {
            byte[] resBuf = new byte[digest.getDigestSize()];
            digest.update(data, 0, data.length);
            digest.doFinal(resBuf, 0);
            return resBuf;
        }
        throw new NullPointerException("Can't getHash a NULL value");
    }

    /**
     * Calculates RIGTMOST160(SHA3(input)). This is used in address
     * calculations. *
     *
     * @param input
     *            - data
     * @return - 20 right bytes of the getHash keccak of the data
     */
    public static byte[] sha3omit12(byte[] input) {
        byte[] hash = sha3(input);
        return copyOfRange(hash, 12, hash.length);
    }

    /**
     * The way to calculate new address inside ethereum
     *
     * @param addr
     *            - creating address
     * @param nonce
     *            - nonce of creating address
     * @return new address
     */
    public static byte[] calcNewAddr(byte[] addr, byte[] nonce) {

        byte[] encSender = RLP.encodeElement(addr);
        byte[] encNonce = RLP.encodeBigInteger(new BigInteger(1, nonce));

        return sha3omit12(RLP.encodeList(encSender, encNonce));
    }

    /**
     * The way to calculate new address inside ethereum for "org.ethereum.vm.OpCode#CREATE2"
     * sha3(0xff ++ msg.sender ++ salt ++ sha3(init_code)))[12:]
     *
     * @param senderAddr - creating address
     * @param initCode - contract init code
     * @param salt - salt to make different result addresses
     * @return new address
     */
    public static byte[] calcSaltAddr(byte[] senderAddr, byte[] initCode, byte[] salt) {
        // 1 - 0xff length, 32 bytes - keccak-256
        byte[] data = new byte[1 + senderAddr.length + salt.length + 32];
        data[0] = (byte) 0xff;
        int currentOffset = 1;
        System.arraycopy(senderAddr, 0, data, currentOffset, senderAddr.length);
        currentOffset += senderAddr.length;
        System.arraycopy(salt, 0, data, currentOffset, salt.length);
        currentOffset += salt.length;
        byte[] sha3InitCode = sha3(initCode);
        System.arraycopy(sha3InitCode, 0, data, currentOffset, sha3InitCode.length);

        return sha3omit12(data);
    }

    /**
     * @see #doubleDigest(byte[], int, int)
     *
     * @param input
     *            -
     * @return -
     */
    public static byte[] doubleDigest(byte[] input) {
        return doubleDigest(input, 0, input.length);
    }

    /**
     * Calculates the SHA-256 getHash of the given byte range, and then hashes the
     * resulting getHash again. This is standard procedure in Bitcoin. The
     * resulting getHash is in big endian form.
     *
     * @param input
     *            -
     * @param offset
     *            -
     * @param length
     *            -
     * @return -
     */
    public static byte[] doubleDigest(byte[] input, int offset, int length) {
        try {
            MessageDigest sha256digest = MessageDigest.getInstance("SHA-256");
            sha256digest.reset();
            sha256digest.update(input, offset, length);
            byte[] first = sha256digest.digest();
            return sha256digest.digest(first);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Can't find such algorithm", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * @return generates random peer id for the HelloMsg
     */
    public static byte[] randomPeerId() {
        Random random = new Random();
        byte[] peerIdBytes = new BigInteger(512, random).toByteArray();

        final String peerId;
        if (peerIdBytes.length > 64)
            peerId = Hex.toHexString(peerIdBytes, 1, 64);
        else
            peerId = Hex.toHexString(peerIdBytes);

        return Hex.decode(peerId);
    }

    /**
     * @return - generate random 32 byte getHash
     */
    public static byte[] randomHash() {

        byte[] randomHash = new byte[32];
        Random random = new Random();
        random.nextBytes(randomHash);
        return randomHash;
    }

    public static String shortHash(byte[] hash) {
        return Hex.toHexString(hash).substring(0, 6);
    }


    public static void main(String[] args) {

        System.out.println(Hex.toHexString(EMPTY_DATA_HASH));
        byte[] randomHash =  randomHash();


        for (int i = 0; i < 30; i++) {
            long start = System.currentTimeMillis();
            //sha3256();
            test2();
            long end = System.currentTimeMillis();
            System.out.println("test1 cost:"+ (end - start));
        }
    }

    public static void test1() {
        byte[] randomHash =  randomHash();
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(HASH_256_ALGORITHM_NAME, CRYPTO_PROVIDER);
            for (int i = 0;i < 200000;i++) {
                digest.update(randomHash);
            }
            //digest.digest();
            System.out.println(Hex.toHexString(digest.digest()));
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Can't find such algorithm", e);
            throw new RuntimeException(e);
        }
    }

    public static void test2() {
        byte[] randomHash =  randomHash();
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(HASH_256_LIGHT_ALGORITHM_NAME, CRYPTO_PROVIDER);

            for (int i = 0;i < 200000;i++) {
                digest.update(randomHash);
            }
            byte[] result = digest.digest();
            System.out.println(Hex.toHexString(result));

        } catch (NoSuchAlgorithmException e) {
            LOG.error("Can't find such algorithm", e);
            throw new RuntimeException(e);
        }
    }

    public static String sha3256() {
        Digest digest = new SHA3Digest(256);
        byte[] randomHash =  randomHash();
        for (int i = 0;i < 200000;i++) {
            digest.update(randomHash, 0, randomHash.length);
        }

        byte[] rsData = new byte[digest.getDigestSize()];
        //System.out.println(rsData.length);
        digest.doFinal(rsData, 0);
        return Hex.toHexString(rsData);
    }
}
