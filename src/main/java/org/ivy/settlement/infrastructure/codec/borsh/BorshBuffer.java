package org.ivy.settlement.infrastructure.codec.borsh;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import static java.util.Objects.requireNonNull;

/**
 * description:
 * @author taining
 */
public class BorshBuffer implements BorshInput, BorshOutput<BorshBuffer> {
    protected final ByteBuffer buffer;

    protected BorshBuffer(final ByteBuffer buffer) {
        this.buffer = requireNonNull(buffer);
        this.buffer.order(ByteOrder.LITTLE_ENDIAN);
        this.buffer.mark();
    }

    protected byte[] array() {
        assert(this.buffer.hasArray());
        return this.buffer.array();
    }

    public static BorshBuffer allocate(final int capacity) {
        return new BorshBuffer(ByteBuffer.allocate(capacity));
    }

    public static BorshBuffer allocateDirect(final int capacity) {
        return new BorshBuffer(ByteBuffer.allocateDirect(capacity));
    }

    public static BorshBuffer wrap(final byte[] array) {
        return new BorshBuffer(ByteBuffer.wrap(array));
    }

    public byte[] toByteArray() {
        assert(this.buffer.hasArray());
        final int arrayOffset = this.buffer.arrayOffset();
        return Arrays.copyOfRange(this.buffer.array(),
                arrayOffset, arrayOffset + this.buffer.position());
    }

    public int capacity() {
        return this.buffer.capacity();
    }

    public BorshBuffer reset() {
        this.buffer.reset();
        return this;
    }

    @Override
    public short readU16() {
        return this.buffer.getShort();
    }

    @Override
    public int readU32() {
        return this.buffer.getInt();
    }

    @Override
    public long readU64() {
        return this.buffer.getLong();
    }

    @Override
    public float readF32() {
        return this.buffer.getFloat();
    }

    @Override
    public double readF64() {
        return this.buffer.getDouble();
    }

    @Override
    public byte read() {
        return this.buffer.get();
    }

    @Override
    public void read(final byte[] result, final int offset, final int length) {
        this.buffer.get(result, offset, length);
    }

    @Override
    public BorshBuffer writeU16(final short value) {
        this.buffer.putShort(value);
        return this;
    }

    @Override
    public BorshBuffer writeU32(final int value) {
        this.buffer.putInt(value);
        return this;
    }

    @Override
    public BorshBuffer writeU64(final long value) {
        this.buffer.putLong(value);
        return this;
    }

    @Override
    public BorshBuffer writeF32(final float value) {
        this.buffer.putFloat(value);
        return this;
    }

    @Override
    public BorshBuffer writeF64(final double value) {
        this.buffer.putDouble(value);
        return this;
    }

    @Override
    public BorshBuffer write(final byte[] bytes) {
        this.buffer.put(bytes);
        return this;
    }

    @Override
    public BorshBuffer write(final byte b) {
        this.buffer.put(b);
        return this;
    }
}
