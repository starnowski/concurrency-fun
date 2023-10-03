package com.github.starnowski.concurrency.fun.java21;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.FORBIDDEN;

@JCStressTest
@Outcome(id = "1", expect = ACCEPTABLE, desc = "Only one rate accepted.")
@Outcome(id = "0", expect = FORBIDDEN, desc = "No rate accepted")
@State
public class RateLimiterImplCleaningJCStressTest {

    private static final String userAgent = "15";
    private static final String ipAddress = "0.0.0.0";
    private RateLimiterImpl rateLimiter;
    private Boolean rateResult;

    public RateLimiterImplCleaningJCStressTest()
    {
        rateLimiter = new RateLimiterImpl(Clock.systemUTC(), 1, Duration.ofSeconds(60));
        RateLimiterImpl.WorkUnit workUnit = new RateLimiterImpl.WorkUnit();
        Instant oldInstant = Instant.now().minus(Duration.ofMinutes(10));
        workUnit.getRequestInstants().add(oldInstant);
        rateLimiter.getMap().put(RateLimiterImpl.prepareKey(userAgent, ipAddress), workUnit);
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
        ConcurrentHashMap<RateLimiterImpl.Key, RateLimiterImpl.WorkUnit> map = rateLimiter.getMap();
        RateLimiterImpl.WorkUnit workUnit = map.get(RateLimiterImpl.prepareKey(userAgent, ipAddress));
        r.r1 = (int) (workUnit == null ? 0 : workUnit.getRequestInstants().size());
    }
}