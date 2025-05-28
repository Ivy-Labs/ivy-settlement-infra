package org.ivy.settlement.infrastructure.codec.borsh;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import static java.util.Objects.requireNonNull;

/**
 * description:
 * @author taining
 */
public class BorshReader implements BorshInput, Closeable {
    private final InputStream stream;

    public BorshReader(final InputStream stream) {
        this.stream = requireNonNull(stream);
    }

    @Override
    public void close() throws IOException {
        this.stream.close();
    }

    @Override
    public byte read() {
        try {
            final int result = this.stream.read();
            if (result == -1) {
                throw new EOFException();
            }
            return (byte)result;
        }
        catch (final IOException error) {
            throw new RuntimeException(error);
        }
    }

    @Override
    public void read(final byte[] result, final int offset, final int length) {
        if (offset < 0 || length < 0 || length > result.length - offset) {
            throw new IndexOutOfBoundsException();
        }
        try {
            int n = 0;
            while (n < length) {
                final int count = this.stream.read(result, offset + n, length - n);
                if (count == -1) {
                    throw new EOFException();
                }
                n += count;
            }
        }
        catch (final IOException error) {
            throw new RuntimeException(error);
        }
    }
}
