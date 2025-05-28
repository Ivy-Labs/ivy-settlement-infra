package org.ivy.settlement.follower.store;

import org.ivy.settlement.ethereum.model.SettlementChainBlockRetrievalResult;
import org.ivy.settlement.ethereum.model.settlement.LatestFollowerChainBlockBatch;
import org.ivy.settlement.ethereum.model.settlement.SettlementBlockInfo;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * description:
 * @author carrot
 */
class CacheEntity {

    long cacheRange;

    volatile long latestNumber;

    TreeMap<Long, SettlementBlockInfo> settlementBlockInfoCache;

    ReentrantReadWriteLock lock;

    public CacheEntity(long cacheRange, long latestNumber) {
        this.cacheRange = cacheRange;
        this.latestNumber = latestNumber;
        this.settlementBlockInfoCache = new TreeMap<>();
        this.lock = new ReentrantReadWriteLock();
    }

    public long getLatestNumber() {
        return latestNumber;
    }

    void put(LatestFollowerChainBlockBatch batch) {
        try {
            this.lock.writeLock().lock();
            for (var b : batch.getSettlementBlockInfos()) {
                this.settlementBlockInfoCache.put(b.getHeight(), b);
            }
            this.latestNumber = batch.getEndNumber();

            var currentMin = latestNumber - cacheRange;
            var firstEntry = this.settlementBlockInfoCache.firstEntry();
            while (firstEntry != null) {
                if (currentMin > firstEntry.getKey()) {
                    //remove the smallest
                    this.settlementBlockInfoCache.pollFirstEntry();
                    firstEntry = this.settlementBlockInfoCache.firstEntry();
                } else {
                    break;
                }
            }
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    SettlementChainBlockRetrievalResult retrievalFollowerChainBlock(long start, long end) {
        try {
            this.lock.readLock().lock();
            if (end > this.latestNumber) {
                return SettlementChainBlockRetrievalResult.ofBigger();
            }

            var min = this.latestNumber - cacheRange;
            var actions = new ArrayList<SettlementBlockInfo>(8);

            if (start < min) {
                this.settlementBlockInfoCache
                        .subMap(min, true, end, true)
                        .forEach((k, v) -> actions.add(v));
                return SettlementChainBlockRetrievalResult.ofSmaller(start, min - 1, actions);
            } else {
                this.settlementBlockInfoCache
                        .subMap(start, true, end, true)
                        .forEach((k, v) -> actions.add(v));
                return SettlementChainBlockRetrievalResult.ofEquals(actions);
            }
        } finally {
            this.lock.readLock().unlock();
        }
    }

}
