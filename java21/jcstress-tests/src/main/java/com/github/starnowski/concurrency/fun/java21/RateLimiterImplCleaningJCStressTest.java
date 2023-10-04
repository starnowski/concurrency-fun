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
@Outcome(id = "-1", expect = FORBIDDEN, desc = "Work unit is null but rate was accepted")
@Outcome(id = "-2", expect = FORBIDDEN, desc = "Work unit is null and rate was not even accepted")
@Outcome(id = "-3", expect = FORBIDDEN, desc = "Work unit is null and second actor was not invoked")
@State
@Description("RateLimiter is executed by one actor that is trying to accept its request and another actor tries to clean old work units for which one of them contains the key that represents the request that the other actor is trying to accept")
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
//        r.r1 = (int) (workUnit == null ? 0 : workUnit.getRequestInstants().size());
    }
}