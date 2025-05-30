package org.ivy.settlement.infrastructure.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * description:
 * @author carrot
 */
public abstract class IrisSettlementWorker {

    private static final Logger logger = LoggerFactory.getLogger("iris-settlement-worker");

    Runnable loopTask;

    Thread thread;

    CountDownLatch terminateCondition;

    volatile boolean stop;

    public IrisSettlementWorker(String name) {
        initTask();
        this.thread = new Thread(loopTask, name);
        this.terminateCondition = new CountDownLatch(1);
    }

    private void initTask() {
        loopTask = () -> {
            beforeLoop();

            while (true) {
                if (stop) {
                    logger.warn("{} was stopped!", Thread.currentThread().getName());
                    break;
                }

                try {
                    doWork();
                } catch (Throwable e) {
                    doException(e);
                } finally {
                    doRelease();
                }
            }
            this.terminateCondition.countDown();
        };
    }

    public void start() {
        this.thread.start();
    }

    public void terminateAndFullAwait() {
        try {
            this.stop = true;
            this.terminateCondition.await();
        } catch (InterruptedException e) {
        }
    }

    public void stop() {this.stop = true;}

    public void fullAwait() {
        try {
            this.terminateCondition.await();
        } catch (InterruptedException e) {
        }
    }

    protected void beforeLoop() {}

    protected abstract void doWork() throws Exception;

    protected void doException(Throwable e) {
        //e.printStackTrace();
        logger.warn(String.format("[%s] do work warn!", Thread.currentThread().getName()), e);
    }

    protected void doRelease() {}
}
