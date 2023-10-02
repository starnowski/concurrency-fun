package com.github.starnowski.concurrency.fun.java21;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class RateLimiterImplCleaningMultithreadedTCTest extends MultithreadedTestCase {

    private static final String userAgent = "15";
    private static final String ipAddress = "0.0.0.0";
    private RateLimiterImpl rateLimiter;
    private Boolean rateResult;
    @Override
    public void initialize() {
        rateLimiter = new RateLimiterImpl(Clock.systemUTC(), 1, Duration.ofSeconds(60));
        RateLimiterImpl.WorkUnit workUnit = new RateLimiterImpl.WorkUnit();
        Instant oldInstant = Instant.now().minus(Duration.ofMinutes(10));
        workUnit.getRequestInstants().add(oldInstant);
        rateLimiter.getMap().put(RateLimiterImpl.prepareKey(userAgent, ipAddress), workUnit);
    }
    public void thread1() {
//        rateResult = rateLimiter.canAccept(userAgent, ipAddress);
        rateLimiter.cleanOldWorkUnits();
    }
    public void thread2() {
//        rateLimiter.cleanOldWorkUnits();
        rateResult = rateLimiter.canAccept(userAgent, ipAddress);
    }
    @Override
    public void finish() {
        ConcurrentHashMap<RateLimiterImpl.Key, RateLimiterImpl.WorkUnit> map = rateLimiter.getMap();
        RateLimiterImpl.WorkUnit workUnit = map.get(RateLimiterImpl.prepareKey(userAgent, ipAddress));
        assertEquals(1, workUnit.getRequestInstants().size());
        assertTrue(rateResult);
    }

    @Test
    public void testRateLimit() throws Throwable {
        TestFramework.runManyTimes(new RateLimiterImplMultithreadedTCTest(), 100);
    }
}
