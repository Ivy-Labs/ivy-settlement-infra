package org.ivy.settlement.infrastructure.datasource;


import org.apache.commons.lang3.tuple.Pair;
import org.ivy.settlement.infrastructure.datasource.model.Keyable;
import org.ivy.settlement.infrastructure.datasource.model.Persistable;
import org.ivy.settlement.infrastructure.datasource.rocksdb.WriteBatchFactory;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * description:
 * @author carrot
 */
public abstract class AbstractDbSource {

    static {
        RocksDB.loadLibrary();
    }

    public RocksDB db;

    public WriteBatchFactory writeBatchFactory;

    public final List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>();

    public final Map<Class<? extends Persistable>, ColumnFamilyHandle> clazz2HandleTable = new HashMap<>();

    public final Map<Class<? extends Persistable>, Constructor> clazz2ConstructorTable = new HashMap<>();

    public abstract Persistable get(Class<?> model, Keyable keyable);

    public abstract byte[] getRaw(Class<?> model, Keyable keyable);

    public abstract List<byte[]> batchGetRaw(Class<?> model, List<byte[]> keys);

    public abstract void put(Keyable keyable, Persistable persistable);

    public abstract void updateBatch(List<Pair<Keyable, Persistable>> saveBatch);

    public abstract List<Persistable> getAll(Class<?> model);
}
