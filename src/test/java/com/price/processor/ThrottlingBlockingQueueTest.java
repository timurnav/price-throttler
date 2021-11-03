package com.price.processor;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

public class ThrottlingBlockingQueueTest {

    private final ThrottlingBlockingQueue queue = new ThrottlingBlockingQueue();

    @AfterEach
    void cleanUp() {
        queue.clear();
    }

    @Test
    void orderedDeduplicationTest() {
        queue.offer(new PriceUpdate("AB", 1.2));
        queue.offer(new PriceUpdate("AC", 1.1));
        queue.offer(new PriceUpdate("AC", 1.0));
        queue.offer(new PriceUpdate("AC", 1.1));
        queue.offer(new PriceUpdate("AB", 1.4));

        Assertions.assertThat(queue.poll())
                .usingRecursiveComparison()
                .isEqualTo(new PriceUpdate("AB", 1.4));
        Assertions.assertThat(queue.poll())
                .usingRecursiveComparison()
                .isEqualTo(new PriceUpdate("AC", 1.1));
        Assertions.assertThat(queue.poll())
                .isNull();
    }

    @Test
    void waitingForUpdateTest() {
        CompletableFuture<PriceUpdate> futurePriceUpdate = CompletableFuture.supplyAsync(() -> {
            try {
                return queue.take();
            } catch (InterruptedException ignore) {
                return null;
            }
        });
        queue.offer(new PriceUpdate("AB", 1.2));

        Assertions.assertThat(futurePriceUpdate.join())
                .usingRecursiveComparison()
                .isEqualTo(new PriceUpdate("AB", 1.2));
        Assertions.assertThat(queue.poll())
                .isNull();
    }
}
