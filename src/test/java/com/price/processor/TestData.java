package com.price.processor;

import java.util.Objects;

public class TestData {
    public final String ccyPair;
    public final double rate;

    public TestData(String ccyPair, double rate) {
        this.ccyPair = ccyPair;
        this.rate = rate;
    }

    @Override
    public String toString() {
        return ccyPair + ':' + rate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestData testData = (TestData) o;
        return Double.compare(testData.rate, rate) == 0
                && Objects.equals(ccyPair, testData.ccyPair);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ccyPair, rate);
    }
}
