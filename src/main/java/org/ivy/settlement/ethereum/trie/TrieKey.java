package org.ivy.settlement.ethereum.trie;

import static org.ivy.settlement.infrastructure.bytes.ByteUtil.EMPTY_BYTE_ARRAY;
import static org.ivy.settlement.infrastructure.bytes.ByteUtil.toHexString;

/**
 * Created by Anton Nashatyrev on 13.02.2017.
 */
public final class TrieKey {

    public static final int ODD_OFFSET_FLAG = 0x1;

    public static final int TERMINATOR_FLAG = 0x2;

    private final byte[] key;

    private final int off;

    private final boolean terminal;

    public static TrieKey fromNormal(byte[] key) {
        return new TrieKey(key);
    }

    public static TrieKey fromPacked(byte[] key) {
        return new TrieKey(key, ((key[0] >> 4) & ODD_OFFSET_FLAG) != 0 ? 1 : 2, ((key[0] >> 4) & TERMINATOR_FLAG) != 0);
    }

    public static TrieKey empty(boolean terminal) {
        return new TrieKey(EMPTY_BYTE_ARRAY, 0, terminal);
    }

    public static TrieKey singleHex(int hex) {
        TrieKey ret = new TrieKey(new byte[1], 1, false);
        ret.setHex(0, hex);
        return ret;
    }

    public TrieKey(byte[] key, int off, boolean terminal) {
        this.terminal = terminal;
        this.off = off;
        this.key = key;
    }

    private TrieKey(byte[] key) {
        this(key, 0, true);
    }

    public byte[] toPacked() {
        var flags = ((off & 1) != 0 ? ODD_OFFSET_FLAG : 0) | (terminal ? TERMINATOR_FLAG : 0);
        var ret = new byte[getLength() / 2 + 1];
        var toCopy = (flags & ODD_OFFSET_FLAG) != 0 ? ret.length : ret.length - 1;
        System.arraycopy(key, key.length - toCopy, ret, ret.length - toCopy, toCopy);
        ret[0] &= 0x0F;
        ret[0] |= flags << 4;
        return ret;
    }

    public boolean isTerminal() {
        return terminal;
    }

    public boolean isEmpty() {
        return getLength() == 0;
    }

    public TrieKey shift(int hexCnt) {
        return new TrieKey(this.key, off + hexCnt, terminal);
    }

    public TrieKey getCommonPrefix(TrieKey k) {
        // TODO can be optimized
        var prefixLen = 0;
        var thisLength = getLength();
        var kLength = k.getLength();
        while (prefixLen < thisLength && prefixLen < kLength && getHex(prefixLen) == k.getHex(prefixLen))
            prefixLen++;
        var prefixKey = new byte[(prefixLen + 1) >> 1];
        var ret = new TrieKey(prefixKey, (prefixLen & 1) == 0 ? 0 : 1,
                prefixLen == getLength() && prefixLen == k.getLength() && terminal && k.isTerminal());
        for (var i = 0; i < prefixLen; i++) {
            ret.setHex(i, k.getHex(i));
        }
        return ret;
    }

    public TrieKey matchAndShift(TrieKey k) {
        var len = getLength();
        var kLen = k.getLength();
        if (len < kLen) return null;

        if ((off & 1) == (k.off & 1)) {
            // optimization to compare whole keys bytes
            if ((off & 1) == 1) {
                if (getHex(0) != k.getHex(0)) return null;
            }
            var idx1 = (off + 1) >> 1;
            var idx2 = (k.off + 1) >> 1;
            var l = kLen >> 1;
            for (int i = 0; i < l; i++, idx1++, idx2++) {
                if (key[idx1] != k.key[idx2]) return null;
            }
        } else {
            for (var i = 0; i < kLen; i++) {
                if (getHex(i) != k.getHex(i)) return null;
            }
        }
        return shift(kLen);
    }

    public int getLength() {
        return (key.length << 1) - off;
    }

    private void setHex(int idx, int hex) {
        var byteIdx = (off + idx) >> 1;
        if (((off + idx) & 1) == 0) {
            key[byteIdx] &= 0x0F;
            key[byteIdx] |= hex << 4;
        } else {
            key[byteIdx] &= 0xF0;
            key[byteIdx] |= hex;
        }
    }

    public int getHex(int idx) {
        byte b = key[(off + idx) >> 1];
        return (((off + idx) & 1) == 0 ? (b >> 4) : b) & 0xF;
    }

    public TrieKey concat(TrieKey k) {
        if (isTerminal()) throw new RuntimeException("Can' append to terminal key: " + this + " + " + k);
        var len = getLength();
        var kLen = k.getLength();
        var newLen = len + kLen;
        var newKeyBytes = new byte[(newLen + 1) >> 1];
        var ret = new TrieKey(newKeyBytes, newLen & 1, k.isTerminal());
        for (var i = 0; i < len; i++) {
            ret.setHex(i, getHex(i));
        }
        for (var i = 0; i < kLen; i++) {
            ret.setHex(len + i, k.getHex(i));
        }
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        var k = (TrieKey) obj;
        var len = getLength();

        if (len != k.getLength()) return false;
        // TODO can be optimized
        for (var i = 0; i < len; i++) {
            if (getHex(i) != k.getHex(i)) return false;
        }
        return isTerminal() == k.isTerminal();
    }

    @Override
    public String toString() {
        return toHexString(key).substring(off) + (isTerminal() ? "T" : "");
    }
}
