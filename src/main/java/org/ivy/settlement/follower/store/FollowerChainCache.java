package org.ivy.settlement.follower.store;

import org.ivy.settlement.ethereum.model.SettlementChainBlockRetrievalResult;
import org.ivy.settlement.ethereum.model.settlement.LatestFollowerChainBlockBatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * description:
 * @author carrot
 */
public class FollowerChainCache {

    int cacheRange;

    Map<Integer, CacheEntity> cache;

    ReentrantReadWriteLock lock;

    public FollowerChainCache(int cacheRange, List<LatestFollowerChainBlockBatch> initBatches) {
        this.cacheRange = cacheRange;
        this.cache = new HashMap<>();
        this.lock = new ReentrantReadWriteLock();
        put(initBatches);
    }

    public void put(List<LatestFollowerChainBlockBatch> logBatches) {
        logBatches.forEach(b -> {
            var entity = computeIfAbsent(b.getChain(), b.getEndNumber());
            entity.put(b);
        });
    }


    public SettlementChainBlockRetrievalResult retrieval(int chain, long start , long end) {
        var cacheEntity = this.getEntity(chain);
        if (cacheEntity == null) return SettlementChainBlockRetrievalResult.ofNotExist();
        return cacheEntity.retrievalFollowerChainBlock(start, end);
    }


    private CacheEntity computeIfAbsent(int chain, long latestNumber) {
        CacheEntity entity;
        try {
            this.lock.readLock().lock();
            entity = this.cache.get(chain);
        } finally {
            this.lock.readLock().unlock();
        }


        if (entity != null) return entity;
        entity = new CacheEntity(cacheRange, latestNumber);
        try {
            this.lock.writeLock().lock();
            this.cache.put(chain, entity);
        } finally {
            this.lock.writeLock().unlock();
        }
        return entity;
    }

    public CacheEntity getEntity(int chain) {
        try {
            this.lock.readLock().lock();
            return this.cache.get(chain);
        } finally {
            this.lock.readLock().unlock();
        }
    }

}
