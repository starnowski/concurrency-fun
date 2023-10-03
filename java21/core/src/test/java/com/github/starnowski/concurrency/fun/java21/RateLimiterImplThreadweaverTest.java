package com.github.starnowski.concurrency.fun.java21;

import com.google.testing.threadtester.*;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterImplThreadweaverTest {

    private static final String userAgent = "15";
    private static final String ipAddress = "0.0.0.0";
    private RateLimiter rateLimiter;
    private Boolean[] rateResults = new Boolean[2];
    @ThreadedBefore
    public void before() {
        rateLimiter = new RateLimiterImpl(Clock.systemUTC(), 1, Duration.ofSeconds(60));
    }
    @ThreadedMain
    public void mainThread() {
        rateResults[0] = rateLimiter.canAccept(userAgent, ipAddress);
    }
    @ThreadedSecondary
    public void secondThread() {
        rateResults[1] = rateLimiter.canAccept(userAgent, ipAddress);
    }
    @ThreadedAfter
    public void after() {
        assertEquals(1, Stream.of(rateResults).filter(b -> Boolean.TRUE.equals(b)).count());
        assertEquals(1, Stream.of(rateResults).filter(b -> Boolean.FALSE.equals(b)).count());
    }

//    @Test
    public void testCounter() {
        new AnnotatedTestRunner().runTests(this.getClass(), RateLimiterImpl.class);
    }
}