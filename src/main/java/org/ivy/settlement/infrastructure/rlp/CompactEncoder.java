package org.ivy.settlement.infrastructure.rlp;

import java.util.HashMap;
import java.util.Map;

import static org.spongycastle.util.encoders.Hex.encode;

/**
 * Compact encoding of hex sequence with optional terminator
 *
 * The traditional compact way of encoding a hex string is to convert it into binary
 * - that is, a string like 0f1248 would become three bytes 15, 18, 72. However,
 * this approach has one slight problem: what if the length of the hex string is odd?
 * In that case, there is no way to distinguish between, say, 0f1248 and f1248.
 *
 * Additionally, our application in the Merkle Patricia tree requires the additional feature
 * that a hex string can also have a special "terminator symbol" at the end (denoted by the 'T').
 * A terminator symbol can occur only once, and only at the end.
 *
 * An alternative way of thinking about this to not think of there being a terminator symbol,
 * but instead treat bit specifying the existence of the terminator symbol as a bit specifying
 * that the given node encodes a final node, where the value is an actual value, rather than
 * the getHash of yet another node.
 *
 * To solve both of these issues, we force the first nibble of the final byte-stream to encode
 * two flags, specifying oddness of length (ignoring the 'T' symbol) and terminator status;
 * these are placed, respectively, into the two lowest significant bits of the first nibble.
 * In the case of an even-length hex string, we must introduce a second nibble (of value zero)
 * to ensure the hex-string is even in length and thus is representable by a whole number of bytes.
 *
 * Examples:
 * &gt; [ 1, 2, 3, 4, 5 ]
 * '\x11\x23\x45'
 * &gt; [ 0, 1, 2, 3, 4, 5 ]
 * '\x00\x01\x23\x45'
 * &gt; [ 0, 15, 1, 12, 11, 8, T ]
 * '\x20\x0f\x1c\xb8'
 * &gt; [ 15, 1, 12, 11, 8, T ]
 * '\x3f\x1c\xb8'
 */
public class CompactEncoder {

    private final static byte TERMINATOR = 16;
    private final static Map<Character, Byte> hexMap = new HashMap<>();

    static {
        hexMap.put('0', (byte) 0x0);
        hexMap.put('1', (byte) 0x1);
        hexMap.put('2', (byte) 0x2);
        hexMap.put('3', (byte) 0x3);
        hexMap.put('4', (byte) 0x4);
        hexMap.put('5', (byte) 0x5);
        hexMap.put('6', (byte) 0x6);
        hexMap.put('7', (byte) 0x7);
        hexMap.put('8', (byte) 0x8);
        hexMap.put('9', (byte) 0x9);
        hexMap.put('a', (byte) 0xa);
        hexMap.put('b', (byte) 0xb);
        hexMap.put('c', (byte) 0xc);
        hexMap.put('d', (byte) 0xd);
        hexMap.put('e', (byte) 0xe);
        hexMap.put('f', (byte) 0xf);
    }

    public static byte[] binToNibblesNoTerminator(byte[] str) {

        byte[] hexEncoded = encode(str);

        for (int i = 0; i < hexEncoded.length; ++i){
            byte b = hexEncoded[i];
            hexEncoded[i] = hexMap.get((char) b);
        }

        return hexEncoded;
    }
}
