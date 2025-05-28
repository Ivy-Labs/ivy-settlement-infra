package org.ivy.settlement.infrastructure.collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * description:
 * @author carrot
 */
public class BlockingPutHashMap<K, V>  {

    private static final Logger logger = LoggerFactory.getLogger("utils");

    final ReentrantLock lock;

    /** Condition for waiting puts */
    private final Condition notFull;

    final int blockSize;

    private HashMap<K, V> hashMap;

    public BlockingPutHashMap(int blockSize) {
        this.blockSize = blockSize;
        this.hashMap = new HashMap<>(blockSize);
        lock = new ReentrantLock(true);
        notFull =  lock.newCondition();
    }

    public void put(K key, V value){
        final ReentrantLock lock = this.lock;
        try {
            lock.lockInterruptibly();
            while (blockSize == hashMap.size()) {
                notFull.await();
            }
            this.hashMap.put(key, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            lock.unlock();
        }
    }

    public V remove(K key) {
        V result;
        final ReentrantLock lock = this.lock;
        try {
            lock.lockInterruptibly();
            result = this.hashMap.remove(key);

            if (result == null) return null;

            notFull.signal();
            return result;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        finally {
            lock.unlock();
        }
    }

    public V get(K key) {
        final ReentrantLock lock = this.lock;
        try {
            lock.lockInterruptibly();
            return this.hashMap.get(key);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }
}
