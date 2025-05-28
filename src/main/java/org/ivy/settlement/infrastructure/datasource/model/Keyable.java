package org.ivy.settlement.infrastructure.datasource.model;

import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;

/**
 * description:
 * @author carrot
 */
public interface Keyable extends Comparable<Keyable> {

    byte[] keyBytes();

    @Override
    default public int compareTo(Keyable o) {
        for (var i = 0; i < o.keyBytes().length; i++) {
            var temp = this.keyBytes()[i] - o.keyBytes()[i];
            if (temp == 0) continue;
            return temp;
        }
        return 0;
    }

    static DefaultKeyable ofDefault(byte[] keys) {
        return new DefaultKeyable(keys);
    }

    static class DefaultKeyable implements Keyable {

        final byte[] keyBytes;

        final int hashCode;

        public DefaultKeyable(byte[] keyBytes) {
            this.keyBytes = keyBytes;
            //this.hashCode = Arrays.hashCode(keyBytes);

            int temp = 17;
            for (int i = 0; i < keyBytes.length; i++) {
                temp = temp * 47 + keyBytes[i];
            }
            this.hashCode = temp;
        }

        @Override
        public byte[] keyBytes() {
            return keyBytes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            var keyable = (DefaultKeyable) o;
            return Arrays.equals(keyBytes, keyable.keyBytes);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public String toString() {
            return "DefaultKeyable{" +
                    "keyBytes=" + Hex.toHexString(this.keyBytes) +
                    '}';
        }
    }
}
