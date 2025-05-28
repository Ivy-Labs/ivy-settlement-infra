package org.ivy.settlement.infrastructure.async;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * description:
 * @author carrot
 */
public class ThanosThreadFactory implements ThreadFactory {

    private AtomicInteger threadIndex = new AtomicInteger(0);

    public final String baseName;

    public ThanosThreadFactory(String baseName) {
        this.baseName = baseName;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, String.format("%s_thread%d", baseName, this.threadIndex.incrementAndGet()));
    }
}
