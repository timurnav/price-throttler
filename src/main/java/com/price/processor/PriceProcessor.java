package com.price.processor;

public interface PriceProcessor {

    void onPrice(String ccyPair, double rate);

    String identity();
}
