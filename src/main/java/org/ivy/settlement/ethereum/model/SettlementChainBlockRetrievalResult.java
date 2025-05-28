package org.ivy.settlement.ethereum.model;


import org.ivy.settlement.ethereum.model.settlement.SettlementBlockInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * @author carrot
 */
public class SettlementChainBlockRetrievalResult {

    public static final int EQUALS = 0;

    public static final int BIGGER = 1;

    public static final int SMALLER = 2;
    public static final int NOT_EXIST = 3;


    private int status;

    private long start;

    private long end;

    final List<SettlementBlockInfo> result;


    private SettlementChainBlockRetrievalResult(int status, long start, long end, List<SettlementBlockInfo> result) {
        this.status = status;
        this.start = start;
        this.end = end;
        this.result = result;
    }

    public static SettlementChainBlockRetrievalResult ofEquals(List<SettlementBlockInfo> result) {
        return new SettlementChainBlockRetrievalResult(EQUALS, -1, -1, result);
    }

    public static SettlementChainBlockRetrievalResult ofSmaller(long start, long end, ArrayList<SettlementBlockInfo> result) {
        return new SettlementChainBlockRetrievalResult(SMALLER, start, end, result);
    }

    public static SettlementChainBlockRetrievalResult ofBigger() {
        return new SettlementChainBlockRetrievalResult(BIGGER, -1, -1, null);
    }

    public static SettlementChainBlockRetrievalResult ofNotExist() {
        return new SettlementChainBlockRetrievalResult(NOT_EXIST, -1, -1, null);
    }

    public boolean isEquals() {
        return this.status == EQUALS;
    }

    public boolean isBigger() {
        return this.status == BIGGER;
    }

    public boolean isSmaller() {
        return this.status == SMALLER;
    }

    public boolean isNotExist() {
        return this.status == NOT_EXIST;
    }

    public int getStatus() {
        return status;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public List<SettlementBlockInfo> getResult() {
        return result;
    }


    //    public List<List<EthLogEvent>> getResult() {
//        return result;
//    }
}
