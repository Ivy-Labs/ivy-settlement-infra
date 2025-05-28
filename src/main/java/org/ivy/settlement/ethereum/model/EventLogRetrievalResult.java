package org.ivy.settlement.ethereum.model;

import org.ivy.settlement.infrastructure.datasource.model.EthLogEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * @author carrot
 */
public class EventLogRetrievalResult {

    public static final int EQUALS = 0;

    public static final int BIGGER = 1;

    public static final int SMALLER = 2;


    private int status;

    private long start;

    private long end;

    final List<List<? extends EthLogEvent>> result;


    private EventLogRetrievalResult(int status, long start, long end, List<List<? extends EthLogEvent>> result) {
        this.status = status;
        this.start = start;
        this.end = end;
        this.result = result;
    }

    public static EventLogRetrievalResult ofEquals(List<List<? extends EthLogEvent>> result) {
        return new EventLogRetrievalResult(EQUALS, -1, -1, result);
    }

    public static  EventLogRetrievalResult ofSmaller(long start, long end, ArrayList<List<? extends EthLogEvent>> result) {
        return new EventLogRetrievalResult(SMALLER, start, end, result);
    }

    public static  EventLogRetrievalResult ofBigger() {
        return new EventLogRetrievalResult(BIGGER, -1, -1, null);
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

    public int getStatus() {
        return status;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public List<List<? extends EthLogEvent>> getResult() {
        return result;
    }


    //    public List<List<EthLogEvent>> getResult() {
//        return result;
//    }
}
