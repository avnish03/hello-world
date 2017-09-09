package com.ebizon.appify.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by manish on 24/12/16.
 */
public class GenericQueue {
    private static GenericQueue ourInstance = new GenericQueue();

    public static GenericQueue getInstance() {
        return ourInstance;
    }

    private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
    private GenericQueue() {
    }

    public void addToQueue(Callable<Void> callable){
        executor.submit(callable);
    }
}
