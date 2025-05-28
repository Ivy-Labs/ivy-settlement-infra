package org.ivy.settlement.infrastructure.collections;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * description:
 * @author carrot
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {

    ReentrantLock cacheLock;

    private final int maxSize;

    public LRUCache(int maxSize) {
        super(maxSize, 0.75f, true);
        this.maxSize = maxSize;
    }

    @Override
    public V get(Object key) {
        return super.get(key);
    }

    @Override
    public V put(K key, V value) {
        cacheLock.lock();
        try {
            return super.put(key, value);
        } finally {
            cacheLock.unlock();
        }
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return this.size() > maxSize;
    }


}
