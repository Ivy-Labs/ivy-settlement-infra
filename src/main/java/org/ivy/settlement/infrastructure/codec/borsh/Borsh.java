package org.ivy.settlement.infrastructure.codec.borsh;


import java.util.Arrays;

import static java.util.Objects.requireNonNull;

/**
 * description:
 * @author taining
 */
public interface Borsh {

    public static byte[] serialize(final Object object) {
        return BorshBuffer.allocate(4096).write(requireNonNull(object)).toByteArray();
    }

    public static <T> T deserialize(final byte[] bytes, final Class klass) {
        return deserialize(BorshBuffer.wrap(requireNonNull(bytes)), klass);
    }

    public static <T> T deserialize(final BorshBuffer buffer, final Class klass) {
        return buffer.read(requireNonNull(klass));
    }

    public static boolean isSerializable(final Class klass) {
        if (klass == null) return false;
        return Arrays.stream(klass.getInterfaces()).anyMatch(iface -> iface == Borsh.class) ||
                isSerializable(klass.getSuperclass());
    }
}
