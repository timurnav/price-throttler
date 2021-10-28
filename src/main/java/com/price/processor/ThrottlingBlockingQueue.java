package com.price.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class ThrottlingBlockingQueue {

    private final Map<String, PriceUpdate> latestUpdates = new HashMap<>();
    private final LinkedBlockingQueue<String> ccyQueue = new LinkedBlockingQueue<>();

    private final Object lock = new Object();

    public PriceUpdate take() throws InterruptedException {
        String poll = ccyQueue.take();
        synchronized (lock) {
            return latestUpdates.remove(poll);
        }
    }

    public void offer(PriceUpdate priceUpdate) {
        synchronized (lock) {
            String ccyPair = priceUpdate.ccyPair;
            if (!latestUpdates.containsKey(ccyPair)) {
                ccyQueue.offer(ccyPair);
            }
            latestUpdates.put(ccyPair, priceUpdate);
        }
    }
}
