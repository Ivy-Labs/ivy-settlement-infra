package org.ivy.settlement.infrastructure.datasource.rocksdb;

import org.apache.commons.lang3.tuple.Pair;
import org.ivy.settlement.infrastructure.datasource.AbstractDbSource;
import org.ivy.settlement.infrastructure.datasource.DbSettings;
import org.ivy.settlement.infrastructure.datasource.model.Keyable;
import org.ivy.settlement.infrastructure.datasource.model.Persistable;
import org.rocksdb.*;
import org.rocksdb.util.SizeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ch.qos.logback.core.encoder.ByteArrayUtil.toHexString;


/**
 * description:
 * @author carrot
 */
public class RocksDbSource extends AbstractDbSource {

    private static final Logger logger = LoggerFactory.getLogger("db");

    public static final int DB_MAX_RETRY_TIME = 10;

    String name;

    // initialized for standalone test

    ReadOptions readOpts;

    DBOptions dbOptions;

    DbSettings settings;

    //CipherKey cipherKey;

    // The native RocksDB insert/update/delete are normally thread-safe
    // However close operation is not thread-safe.
    // This ReadWriteLock still permits concurrent execution of insert/delete/update operations
    // however blocks them on init/close/delete operations
    // private ReadWriteLock resetDbLock = new ReentrantReadWriteLock();
//    public RocksDbSource(String name, Map<String, Class<? extends Persistable>> columnFamilies, SystemConfig systemConfig) {
//        this(name, columnFamilies, systemConfig, DbSettings.DEFAULT);
//    }

    public RocksDbSource(String name, Map<String, Class<? extends Persistable>> columnFamilies, String dbPath, DbSettings settings) {
        this.name = name;
        this.settings = settings;
        this.writeBatchFactory = new WriteBatchFactory();
        //this.cipherKey = config.getCipherKey();
        logger.debug("New RocksDbSource: " + name);
        init(columnFamilies, dbPath);
    }

    private void init(Map<String, Class<? extends Persistable>> columnFamilies, String dbPath) {
        try {

            dbOptions = new DBOptions();
            // general options
            dbOptions.setCreateIfMissing(true)
                    // For now we set the max total WAL size to be 512M. This config can be useful when column
                    // families are updated at non-uniform frequencies.
                    .setMaxTotalWalSize(0)
                    //.setWalRecoveryMode(AbsoluteConsistency)
                    .setCreateMissingColumnFamilies(true)
                    .setMaxOpenFiles(settings.getMaxOpenFiles())
                    .setIncreaseParallelism(settings.getMaxThreads())
                    .setMaxBackgroundCompactions(settings.getMaxThreads())
                    .setMaxBackgroundFlushes(settings.getMaxThreads())
                    .setAllowConcurrentMemtableWrite(true)
                    .setEnableWriteThreadAdaptiveYield(true)
                    .setInfoLogLevel(InfoLogLevel.ERROR_LEVEL)
                    .setMaxSubcompactions(settings.getMaxThreads());


            // read options
            readOpts = new ReadOptions();
            readOpts = readOpts.setPrefixSameAsStart(true)
                    .setVerifyChecksums(false);

            // key prefix for state node lookups
            //options.useFixedLengthPrefixExtractor(NodeKeyCompositor.PREFIX_BYTES);

            //BlockBasedTable 是 SSTable 的默认表格式。
            var blockBasedTableConfig = new BlockBasedTableConfig();
            blockBasedTableConfig
                    .setCacheNumShardBits(2)
                    .setBlockSizeDeviation(10)
                    .setBlockRestartInterval(64)
                    .setBlockCacheSize(-1);
                    //.setBlockCacheSize(100_000 * SizeUnit.KB)
            if (settings.isBloomFilterFlag()) {
                blockBasedTableConfig.setFilter(new BloomFilter(10, false));
            }
            // .setBlockCacheCompressedSize(32 * SizeUnit.KB);

            var columnFamilyOptions = new ColumnFamilyOptions()
                    .setTableFormatConfig(blockBasedTableConfig)
                    .setMaxWriteBufferNumber(4)
                    .setMinWriteBufferNumberToMerge(4)
                    .setWriteBufferSize(settings.getWriteBufferSize() * SizeUnit.MB);

            var columnFamilyDescriptors = new ArrayList<ColumnFamilyDescriptor>();
            columnFamilyDescriptors.add(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, columnFamilyOptions));


            for (var name : columnFamilies.keySet()) {
                columnFamilyDescriptors.add(new ColumnFamilyDescriptor(name.getBytes(), columnFamilyOptions));
            }


            var path = Paths.get(dbPath, name).toString();
            db = RocksDB.open(dbOptions, path, columnFamilyDescriptors, columnFamilyHandles);

            initProcessTable(columnFamilies, columnFamilyDescriptors);
        } catch (Exception e) {
            close(db, dbOptions);
            throw new RuntimeException(e);
        }

    }

    private void initProcessTable(Map<String, Class<? extends Persistable>> columnFamilies, List<ColumnFamilyDescriptor> columnFamilyDescriptors) throws NoSuchMethodException {
        //skip default column
        var i = 1;
        for (; i < columnFamilyDescriptors.size(); i++) {
            var name = new String(columnFamilyDescriptors.get(i).getName());
            clazz2HandleTable.put(columnFamilies.get(name), this.columnFamilyHandles.get(i));
            clazz2ConstructorTable.put(columnFamilies.get(name), columnFamilies.get(name).getDeclaredConstructor(byte[].class));
        }
    }


    public byte[] getRaw(Class<?> model, Keyable keyable) {
        try {
            var handle = clazz2HandleTable.get(model);
            //var encryptedKey = cipherKey.encrypt(keyable.keyBytes());
            return db.get(handle, readOpts, keyable.keyBytes());
            //return cipherKey.decrypt(ret);
        } catch (Exception e) {
            logger.error("Failed to get from db '{}'", name, e);
            hintOnTooManyOpenFiles(e);
            throw new RuntimeException(e);
        }
    }

    public Persistable get(Class<?> model, Keyable keyable) {
        try {
            var handle = clazz2HandleTable.get(model);
            //var encryptedKey = cipherKey.encrypt(keyable.keyBytes());
            var ret = db.get(handle, readOpts, keyable.keyBytes());
            if (ret == null) {
                return null;
            }
            return (Persistable) clazz2ConstructorTable.get(model).newInstance(ret);
        } catch (Exception e) {
            logger.error("Failed to get from db '{}'", name, e);
            hintOnTooManyOpenFiles(e);
            throw new RuntimeException(e);
        }
    }


    public List<Persistable> getAll(Class<?> model) {
        var handle = clazz2HandleTable.get(model);
        try (var iterator = db.newIterator(handle, readOpts)) {
            var result = new ArrayList<Persistable>();
            var constructor = clazz2ConstructorTable.get(model);
            for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
                result.add((Persistable) constructor.newInstance(iterator.value()));
            }

            return result;
        } catch (Exception e) {
            logger.error("Failed to get from db '{}'", name, e);
            hintOnTooManyOpenFiles(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void put(Keyable keyable, Persistable persistable) {
        //WriteOptions writeOptions = null;
        try (var writeOptions = new WriteOptions()) {


            //writeOptions = new WriteOptions();
            writeOptions.setSync(true);
            var handle = clazz2HandleTable.get(persistable.getClass());
            //var encryptedKey = cipherKey.encrypt(keyable.keyBytes());
            if (persistable.valueBytes() != null) {
                //var encryptedValue = cipherKey.encrypt(persistable.valueBytes());
                db.put(handle, writeOptions, keyable.keyBytes(), persistable.valueBytes());
            } else {
                db.delete(handle, writeOptions, keyable.keyBytes());
            }
            if (logger.isTraceEnabled())
                logger.trace("<~ RocksDbSource.put(): " + name + ", key: " + toHexString(keyable.keyBytes()) + ", " + (persistable.valueBytes() == null ? "null" : persistable.valueBytes().length));
        } catch (RocksDBException e) {
            logger.error("Failed to put into db '{}'", name, e);
            hintOnTooManyOpenFiles(e);
            throw new RuntimeException(e);
        }
    }


    public void updateBatch(List<Pair<Keyable, Persistable>> saveBatch) {

        //if (logger.isTraceEnabled()) logger.trace("~> RocksDbSource.updateBatch(): " + name + ", " + saveBatch.size());
        try (var batch = writeBatchFactory.getInstance();
             var writeOptions = new WriteOptions()) {


            //For now we always use synchronous writes. This makes sure that once the operation returns
            //success, the data is persisted even if the machine crashes.
            writeOptions.setSync(true);
            for (var pair : saveBatch) {
                var handle = clazz2HandleTable.get(pair.getRight().getClass());
                if (pair.getRight().valueBytes() == null) {
                    batch.delete(handle, pair.getLeft().keyBytes());
                } else {
                    batch.put(handle, pair.getLeft().keyBytes(), pair.getRight().valueBytes());
                }
            }
            //long start = System.currentTimeMillis();
            db.write(writeOptions, batch);
            //long end = System.currentTimeMillis();
            //logger.debug("write db cost:{}ms", (end - start));


            //if (logger.isTraceEnabled()) logger.trace("<~ RocksDbSource.updateBatch(): " + name + ", " + saveBatch.size());
        } catch (RocksDBException e) {
            logger.error("Error in batch update on db '{}'", name, e);
            //hintOnTooManyOpenFiles(e);
            throw new RuntimeException(e);
        }
    }


    public List<byte[]> batchGetRaw(Class<?> model, List<byte[]> keys) {
        var handle = clazz2HandleTable.get(model);
        try {

            var handles = new ArrayList<ColumnFamilyHandle>(keys.size());
            //var keys = new ArrayList<byte[]>(keys.size());
            keys.forEach(key -> handles.add(handle));
            //keys.forEach(key -> keys.add(key));

            return db.multiGetAsList(readOpts, handles, keys);
        } catch (RocksDBException e) {
            logger.error("Failed to multiGet db [{}], error! {} ", name, e);
            hintOnTooManyOpenFiles(e);
            throw new RuntimeException(e);
        }
    }

    public void delRange(Class<?> model, byte[] start, byte[] end) {
        try (var writeOptions = new WriteOptions()) {
            //For now we always use synchronous writes. This makes sure that once the operation returns
            //success, the data is persisted even if the machine crashes.
            writeOptions.setSync(true);
            var handle = clazz2HandleTable.get(model);

            db.deleteRange(handle, writeOptions, start, end);
        } catch (RocksDBException e) {
            logger.error("Error in batch update on db '{}'", name, e);
            //hintOnTooManyOpenFiles(e);
            throw new RuntimeException(e);
        }
    }

    public RocksIterator newIterator(Class<?> model) {
        return db.newIterator(this.clazz2HandleTable.get(model));
    }


    private void hintOnTooManyOpenFiles(Exception e) {
        if (e.getMessage() != null && e.getMessage().toLowerCase().contains("too many open files")) {
            logger.info("");
            logger.info("       Mitigating 'Too many open files':");
            logger.info("       either decrease value of database.maxOpenFiles parameter in thanos-chain.conf");
            logger.info("       or set higher limit by using 'ulimit -n' command in command line");
            logger.info("");
        }
    }

    public void shutdown() {
        for (final var columnFamilyHandle : columnFamilyHandles) {
            close(columnFamilyHandle);
        }
        close(db, readOpts, dbOptions);
    }

    public static void close(AutoCloseable... autoCloseables) {
        for (var it : autoCloseables) {
            try {
                if (it != null) {
                    it.close();
                }
            } catch (Exception ignored) {
                logger.debug("Silent exception occured", ignored);
            }
        }
    }
}
