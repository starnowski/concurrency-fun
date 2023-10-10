package com.github.starnowski.concurrency.fun.java21;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
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
        Instant instant = null;
        if (temporaryValueWrapper == null) {
            shouldRetrieve = true;
            instant = this.clock.instant();
        }
        else {
            Instant validationTimeout = temporaryValueWrapper.instant.plus(this.temporaryValueWrapper.temporaryValue.getTtl());
            instant = this.clock.instant();
            if (instant.isAfter(validationTimeout)) {
                shouldRetrieve = true;
            }
        }
        if (shouldRetrieve) {
            boolean lockAcquired = false;
            try {
                lockAcquired = supplierLock.tryLock(1000, TimeUnit.MILLISECONDS);
                if (lockAcquired) {
                    boolean tryRetrieve = false;
                    if (temporaryValueWrapper == null) {
                        tryRetrieve = true;
                    } else {
                        Instant validationTimeout = temporaryValueWrapper.instant.plus(this.temporaryValueWrapper.temporaryValue.getTtl());
                        if (instant.isAfter(validationTimeout)) {
                            tryRetrieve = true;
                        }
                    }
                    if (tryRetrieve) {
                        temporaryValueWrapper = new TemporaryValueWrapper(instant, temporaryValueSupplier.get());
                    }
                }
            } catch (InterruptedException e) {
                // do nothing
            } finally {
                if (lockAcquired) {
                    supplierLock.unlock();
                }
            }
        }
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
