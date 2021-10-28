package com.price.processor;

public class PriceUpdate {

    public final String ccyPair;
    public final double rate;

    public PriceUpdate(String ccyPair, double rate) {
        this.ccyPair = ccyPair;
        this.rate = rate;
    }

    @Override
    public String toString() {
        return ccyPair + ':' + rate;
    }
}
