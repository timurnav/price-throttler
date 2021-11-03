package com.price.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CyclicBarrier;

class WaitingPriceProcessor implements PriceProcessor {

    private static final Logger logger = LoggerFactory.getLogger(WaitingPriceProcessor.class);

    private final String identity;
    final CyclicBarrier barrier = new CyclicBarrier(2);
    final List<TestData> consumed = new CopyOnWriteArrayList<>();

    WaitingPriceProcessor(String identity) {
        this.identity = identity;
    }

    public void releaseBarrier() throws BrokenBarrierException, InterruptedException {
        for (int i = 0; i < barrier.getNumberWaiting(); i++) {
            barrier.await();
        }
    }

    @Override
    public void onPrice(String ccyPair, double rate) {
        consumed.add(new TestData(ccyPair, rate));
        logger.trace("{} is waiting for barrier release", identity);
        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException ignore) {
            logger.warn("{} interrupted", identity);
        }
        logger.trace("{}'s barrier released", identity);
    }

    @Override
    public String identity() {
        return identity;
    }

    @Override
    public String toString() {
        return "PriceProcessor[" + identity + ']';
    }
}
