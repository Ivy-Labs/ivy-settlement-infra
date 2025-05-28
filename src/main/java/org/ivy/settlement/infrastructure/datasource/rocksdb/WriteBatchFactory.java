package org.ivy.settlement.infrastructure.datasource.rocksdb;

import org.rocksdb.WriteBatch;

/**
 * description:
 * @author carrot
 */
public class WriteBatchFactory {


    public WriteBatchFactory() {
    }

    public WriteBatch getInstance() {
        return new WriteBatch();
//        if (!systemConfig.dataNeedEncrypt()) {
//            return new WriteBatch();
//        } else {
//            return new EncryptWriteBatch(systemConfig.getCipherKey());
//        }
    }
}
