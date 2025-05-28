package org.ivy.settlement.ethereum.trie;


import org.ivy.settlement.infrastructure.bytes.ByteArrayWrapper;

import java.util.HashMap;
import java.util.Map;


/**
 * description:
 * @author carrot
 */
public class HashMapDBSimple<V> {

    protected final Map<ByteArrayWrapper, V> storage;

    public HashMapDBSimple() {
        this(new HashMap<>());
    }

    public HashMapDBSimple(HashMap<ByteArrayWrapper, V> storage) {
        this.storage = storage;
    }

    public void put(byte[] key, V val) {
        if (val == null) {
            delete(key);
        } else {
            storage.put(new ByteArrayWrapper(key), val);
        }
    }

    public V get(byte[] key) {
        return storage.get(new ByteArrayWrapper(key));
    }

    public void delete(byte[] key) {
        storage.remove(new ByteArrayWrapper(key));
    }

    public void close() {}

    public void reset() {
        storage.clear();
    }
}
