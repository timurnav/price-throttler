package com.price.processor;

public class SubscribersCapacityExceededException extends RuntimeException {

    public SubscribersCapacityExceededException(int maxSubscribers) {
        super(String.format("Unable to add another subscriber: capacity %d exceeded", maxSubscribers));
    }
}
