package com.price.processor;

public interface PriceDistributor {

    void onPrice(String ccyPair, double rate);

    void subscribe(PriceProcessor priceProcessor);

    void unsubscribe(PriceProcessor priceProcessor);
}
