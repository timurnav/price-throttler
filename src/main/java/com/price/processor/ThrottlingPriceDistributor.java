package com.price.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.SECONDS;

public class ThrottlingPriceDistributor implements PriceDistributor, ShutdownHook {

    private static final Logger logger = LoggerFactory.getLogger(ThrottlingPriceDistributor.class);

    private final Map<PriceProcessor, AsyncPriceProcessor> distributionMap;
    private final int maxSubscribers;

    private final ExecutorService executorService;

    public ThrottlingPriceDistributor(int maxSubscribers) {
        this.maxSubscribers = maxSubscribers;
        this.distributionMap = new ConcurrentHashMap<>(maxSubscribers, 1);
        this.executorService = Executors.newFixedThreadPool(maxSubscribers);
    }

    @Override
    public void subscribe(PriceProcessor priceProcessor) {
        logger.info("Subscribing {}", priceProcessor.identity());
        AsyncPriceProcessor processor = new AsyncPriceProcessor(priceProcessor);
        tryPut(priceProcessor, processor);
        executorService.submit(processor::startProcessing);
        logger.info("Subscribed {}", priceProcessor.identity());
    }

    private synchronized void tryPut(PriceProcessor priceProcessor, AsyncPriceProcessor processor) {
        if (distributionMap.size() < maxSubscribers) {
            distributionMap.put(priceProcessor, processor);
        } else {
            throw new SubscribersCapacityExceededException(maxSubscribers);
        }
    }

    @Override
    public void unsubscribe(PriceProcessor priceProcessor) {
        logger.info("Unsubscribing {}", priceProcessor.identity());
        AsyncPriceProcessor removed = distributionMap.remove(priceProcessor);
        if (removed != null) {
            removed.stop();
        }
        logger.info("Unsubscribed {}", priceProcessor.identity());
    }

    @Override
    public void onPrice(String ccyPair, double rate) {
        PriceUpdate priceUpdate = new PriceUpdate(ccyPair, rate);
        logger.trace("Price update broadcasting {}", priceUpdate);
        distributionMap.values().forEach(processor -> processor.onPrice(priceUpdate));
        logger.trace("Price update broadcast sent {}", priceUpdate);
    }

    @Override
    public void onShutdown() {
        distributionMap.values().forEach(AsyncPriceProcessor::stop);
        executorService.shutdown();
    }
}
