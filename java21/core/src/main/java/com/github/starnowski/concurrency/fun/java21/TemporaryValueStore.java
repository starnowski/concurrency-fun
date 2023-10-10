package com.github.starnowski.concurrency.fun.java21;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class TemporaryValueStore {


    private final Supplier<TemporaryValue> temporaryValueSupplier;
    private final Clock clock;
    private TemporaryValueWrapper temporaryValueWrapper;
    private Lock supplierLock = new ReentrantLock();

    public TemporaryValueStore(Supplier<TemporaryValue> temporaryValueSupplier, Clock clock) {
        this.temporaryValueSupplier = temporaryValueSupplier;
        this.clock = clock;
    }

    public TemporaryValue get()
    {
        boolean shouldRetrieve = false;
        if (temporaryValueWrapper == null) {
            shouldRetrieve = true;
        }
        else {
            Instant validationTimeout = temporaryValueWrapper.instant.plus(this.temporaryValueWrapper.temporaryValue.getTtl());
            Instant instant = this.clock.instant();
            if (instant.isAfter(validationTimeout)) {
                shouldRetrieve = true;
            }
        }
        if (shouldRetrieve) {

        }
        //TODO
        return temporaryValueWrapper.temporaryValue;
    }

    public static final class TemporaryValue {
        private final String value;
        private final Duration ttl;

        public TemporaryValue(String value, Duration ttl) {
            this.value = value;
            this.ttl = ttl;
        }

        public String getValue() {
            return value;
        }

        public Duration getTtl() {
            return ttl;
        }
    }

    private final class TemporaryValueWrapper {

        private final Instant instant;
        private final TemporaryValue temporaryValue;

        public TemporaryValueWrapper(Instant instant, TemporaryValue temporaryValue) {
            this.instant = instant;
            this.temporaryValue = temporaryValue;
        }

        public Instant getInstant() {
            return instant;
        }

        public TemporaryValue getTemporaryValue() {
            return temporaryValue;
        }
    }
}
