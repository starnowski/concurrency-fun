package com.github.starnowski.concurrency.fun.java21;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.singletonList;
import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.FORBIDDEN;

@JCStressTest
@Outcome(id = "1", expect = ACCEPTABLE, desc = "Only one rate accepted.")
@Outcome(id = "0", expect = FORBIDDEN, desc = "No rate accepted")
@Outcome(id = "-1", expect = FORBIDDEN, desc = "Work unit is null but rate was accepted")
@Outcome(id = "-2", expect = FORBIDDEN, desc = "Work unit is null and rate was not even accepted")
@Outcome(id = "-3", expect = FORBIDDEN, desc = "Work unit is null and second actor was not invoked")
@State
@Description("RateLimiterConcurrentHashMapImpl is executed by one actor that is trying to accept its request and another actor tries to clean old work units for which one of them contains the key that represents the request that the other actor is trying to accept")
public class RateLimiterConcurrentHashMapImplCleaningJCStressTest {

    private static final String userAgent = "15";
    private static final String ipAddress = "0.0.0.0";
    private RateLimiterConcurrentHashMapImpl rateLimiter;
    private Boolean rateResult;

    public RateLimiterConcurrentHashMapImplCleaningJCStressTest()
    {
        rateLimiter = new RateLimiterConcurrentHashMapImpl(Clock.systemUTC(), 1, Duration.ofSeconds(60));
        Instant oldInstant = Instant.now().minus(Duration.ofMinutes(10));
        RateLimiterConcurrentHashMapImpl.WorkUnit workUnit = new RateLimiterConcurrentHashMapImpl.WorkUnit(singletonList(new RateLimiterConcurrentHashMapImpl.RequestInstantWithUUID(oldInstant, UUID.randomUUID())));
        rateLimiter.getMap().put(RateLimiterConcurrentHashMapImpl.prepareKey(userAgent, ipAddress), workUnit);
    }

    @Actor
    public void actor1() {
        rateLimiter.cleanOldWorkUnits();
    }

    @Actor
    public void actor2() {
        rateResult = rateLimiter.canAccept(userAgent, ipAddress);
    }

    @Arbiter
    public void arbiter(I_Result r) {
        ConcurrentHashMap<RateLimiterConcurrentHashMapImpl.Key, RateLimiterConcurrentHashMapImpl.WorkUnit> map = rateLimiter.getMap();
        RateLimiterConcurrentHashMapImpl.WorkUnit workUnit = map.get(RateLimiterConcurrentHashMapImpl.prepareKey(userAgent, ipAddress));
        if (workUnit != null && workUnit.getRequestInstants().size() == 1) {
            r.r1 = 1;
        } else if (workUnit == null) {
            if (rateResult == null) {
                r.r1 = -3;
            } else if (rateResult) {
                r.r1 = -1;
            } else {
                r.r1 = -2;
            }
        } else {
            r.r1 = 0;
        }
    }
}