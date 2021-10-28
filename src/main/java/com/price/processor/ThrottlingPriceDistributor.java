package com.price.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThrottlingPriceDistributor implements PriceDistributor {

    private static final Logger logger = LoggerFactory.getLogger(ThrottlingPriceDistributor.class);

    private final Map<PriceProcessor, AsyncPriceProcessor> distributionSet = new ConcurrentHashMap<>();

    private final ExecutorService executorService;

    public ThrottlingPriceDistributor() {
        this.executorService = Executors.newFixedThreadPool(100); // todo improve me
    }

    @Override
    public void subscribe(PriceProcessor priceProcessor) {
        logger.info("Subscribing {}", priceProcessor.identity());
        distributionSet.put(priceProcessor, new AsyncPriceProcessor(priceProcessor, executorService));
        logger.info("Subscribed {}", priceProcessor.identity());
    }

    @Override
    public void unsubscribe(PriceProcessor priceProcessor) {
        logger.info("Unsubscribing {}", priceProcessor.identity());
        AsyncPriceProcessor removed = distributionSet.remove(priceProcessor);
        if (removed != null) {
            removed.stop();
        }
        logger.info("Unsubscribed {}", priceProcessor.identity());
    }

    @Override
    public void onPrice(String ccyPair, double rate) {
        PriceUpdate priceUpdate = new PriceUpdate(ccyPair, rate);
        logger.trace("Price update broadcasting {}", priceUpdate);
        distributionSet.values().forEach(processor -> processor.onPrice(priceUpdate));
        logger.trace("Price update broadcast finished sent {}", priceUpdate);
    }
}
