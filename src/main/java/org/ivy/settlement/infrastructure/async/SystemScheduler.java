package org.ivy.settlement.infrastructure.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * description:
 * @author carrot
 */
public class SystemScheduler {


    ExecutorService virtualExecutor;


    public SystemScheduler() {
        this.virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();
    }











}
