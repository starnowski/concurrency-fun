package com.github.starnowski.concurrency.fun.java21;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterImplMultithreadedTCTest extends MultithreadedTestCase {

    private static final String userAgent = "15";
    private static final String ipAddress = "0.0.0.0";
    private RateLimiter rateLimiter;
    private Boolean[] rateResults = new Boolean[2];
    @Override
    public void initialize() {
        rateLimiter = new RateLimiterImpl(Clock.systemUTC(), 1, Duration.ofSeconds(60));
    }
    public void thread1() {
        rateResults[0] = rateLimiter.canAccept(userAgent, ipAddress);
    }
    public void thread2() {
        rateResults[1] = rateLimiter.canAccept(userAgent, ipAddress);
    }
    @Override
    public void finish() {
        assertEquals(1, Stream.of(rateResults).filter(b -> Boolean.TRUE.equals(b)).count());
        assertEquals(1, Stream.of(rateResults).filter(b -> Boolean.FALSE.equals(b)).count());
    }

    @Test
    public void testRateLimit() throws Throwable {
        TestFramework.runManyTimes(new RateLimiterImplMultithreadedTCTest(), 100);
    }
}