package com.price.processor;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ThrottlingPriceDistributorTest {

    public static final int MAX_SUBSCRIBERS = 3;

    private ThrottlingPriceDistributor priceDistributor;

    @BeforeEach
    void init() {
        priceDistributor = new ThrottlingPriceDistributor(MAX_SUBSCRIBERS);
    }

    @AfterEach
    void tearDown() {
        if (priceDistributor != null) {
            priceDistributor.onShutdown();
        }
    }

    @Test
    void priceDistributionTest() throws Exception {
        WaitingPriceProcessor a = new WaitingPriceProcessor("A");
        WaitingPriceProcessor b = new WaitingPriceProcessor("B");
        priceDistributor.subscribe(a);
        priceDistributor.subscribe(b);

        priceDistributor.onPrice("AB", 1.0);
        priceDistributor.onPrice("AC", 2.0);
        priceDistributor.onPrice("AD", 3.0);
        priceDistributor.onPrice("AE", 4.0);

        for (int i = 0; i < 4; i++) {
            a.barrier.await();
            b.barrier.await();
        }
        a.releaseBarrier();
        b.releaseBarrier();

        Assertions.assertThat(a.consumed)
                .containsExactly(
                        new TestData("AB", 1.0),
                        new TestData("AC", 2.0),
                        new TestData("AD", 3.0),
                        new TestData("AE", 4.0)
                );
        Assertions.assertThat(b.consumed)
                .containsExactly(
                        new TestData("AB", 1.0),
                        new TestData("AC", 2.0),
                        new TestData("AD", 3.0),
                        new TestData("AE", 4.0)
                );
    }

    @Test
    void slowConsumerPriceThrottlingTest() throws Exception {
        WaitingPriceProcessor a = new WaitingPriceProcessor("A");
        WaitingPriceProcessor b = new WaitingPriceProcessor("B");
        priceDistributor.subscribe(a);
        priceDistributor.subscribe(b);

        priceDistributor.onPrice("AB", 1.0);
        a.barrier.await();

        priceDistributor.onPrice("AB", 1.1);
        a.barrier.await();

        priceDistributor.onPrice("AB", 1.2);
        a.barrier.await();

        b.releaseBarrier();
        b.releaseBarrier();
        b.releaseBarrier();

        Assertions.assertThat(a.consumed)
                .containsExactly(
                        new TestData("AB", 1.0),
                        new TestData("AB", 1.1),
                        new TestData("AB", 1.2)
                );
        Assertions.assertThat(b.consumed)
                .containsExactly(
                        new TestData("AB", 1.0),
                        new TestData("AB", 1.2));
    }
}
