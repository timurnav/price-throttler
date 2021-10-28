package com.price.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

public class AsyncPriceProcessor {

    private static final Logger logger = LoggerFactory.getLogger(AsyncPriceProcessor.class);

    private final ThrottlingBlockingQueue queue = new ThrottlingBlockingQueue();
    private final PriceProcessor priceProcessor;
    private volatile boolean stopped;

    public AsyncPriceProcessor(PriceProcessor priceProcessor, ExecutorService executorService) {
        this.priceProcessor = priceProcessor;
        executorService.submit(this::process);
    }

    private void process() {
        while (true) {
            try {
                if (stopped) {
                    logger.info("Processing of {} is stopped", priceProcessor.identity());
                    return;
                }
                PriceUpdate priceUpdate = queue.take();
                logger.trace("Sending price update {} to {}", priceUpdate, priceProcessor);
                priceProcessor.onPrice(priceUpdate.ccyPair, priceUpdate.rate);
                logger.trace("Price update {} to {}", priceUpdate, priceProcessor);
            } catch (InterruptedException e) {
                logger.info("{} processing is interrupted", priceProcessor.identity());
                return;
            } catch (Exception e) {
                logger.error("Unable to route price update to {}", priceProcessor.identity(), e);
            }
        }
    }

    public void onPrice(PriceUpdate priceUpdate) {
        queue.offer(priceUpdate);
    }

    public void stop() {
        stopped = true;
    }
}
