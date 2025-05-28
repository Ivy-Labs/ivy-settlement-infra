package org.ivy.settlement.infrastructure.datasource.inmem;

import org.apache.commons.lang3.tuple.Pair;
import org.ivy.settlement.infrastructure.datasource.AbstractDbSource;
import org.ivy.settlement.infrastructure.datasource.model.Keyable;
import org.ivy.settlement.infrastructure.datasource.model.Persistable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * description:
 * @author carrot
 */
public class CacheDbSource extends AbstractDbSource {

    private ConcurrentMap<Keyable, Persistable> cache = new ConcurrentHashMap<>();

    public CacheDbSource() {
    }

    @Override
    public Persistable get(Class<?> model, Keyable keyable) {
        return this.cache.get(keyable);
    }

    @Override
    public byte[] getRaw(Class<?> model, Keyable keyable) {
        Persistable persistable = this.cache.get(keyable);
        if (persistable != null) {
            return persistable.valueBytes();
        }
        return null;
    }

    @Override
    public List<byte[]> batchGetRaw(Class<?> model, List<byte[]> keys) {
        var result = new ArrayList<byte[]>();
        for (byte[] key : keys) {
            Keyable keyable = Keyable.ofDefault(key);
            Persistable persistable = cache.get(keyable);
            if (persistable == null) {
                result.add(null);
            } else {
                result.add(persistable.valueBytes());
            }

        }
        return result;
    }

    @Override
    public void put(Keyable keyable, Persistable persistable) {
        this.cache.put(keyable, persistable);
    }

    @Override
    public void updateBatch(List<Pair<Keyable, Persistable>> saveBatch) {
        for (Pair<Keyable, Persistable> pair : saveBatch) {
            this.cache.put(pair.getLeft(), pair.getRight());
        }
    }

    @Override
    public List<Persistable> getAll(Class<?> model) {
        List<Persistable> result = new ArrayList<>();
        for (Map.Entry<Keyable, Persistable> entry : cache.entrySet()) {
            if (entry.getValue().getClass().isAssignableFrom(model)) {
                result.add(entry.getValue());
            }
        }
        return result;
    }
}
