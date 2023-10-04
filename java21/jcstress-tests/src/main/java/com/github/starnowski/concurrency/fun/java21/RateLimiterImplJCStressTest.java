package com.github.starnowski.concurrency.fun.java21;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;

import java.time.Clock;
import java.time.Duration;
import java.util.stream.Stream;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.FORBIDDEN;

@JCStressTest
@Outcome(id = "1", expect = ACCEPTABLE, desc = "Only one rate accepted.")
@Outcome(id = "2", expect = FORBIDDEN, desc = "Two rates were accepted no matter the limit of the one rate.")
@State
@Description("RateLimiter is executed by two actors that are trying to accept its rate with the same input")
public class RateLimiterImplJCStressTest {

    private static final String userAgent = "15";
    private static final String ipAddress = "0.0.0.0";
    private RateLimiter rateLimiter = new RateLimiterImpl(Clock.systemUTC(), 1, Duration.ofSeconds(60));
    private final Boolean[] rateResults = new Boolean[2];

    @Actor
    public void actor1() {
        rateResults[0] = rateLimiter.canAccept(userAgent, ipAddress);
    }

    @Actor
    public void actor2() {
        rateResults[1] = rateLimiter.canAccept(userAgent, ipAddress);
    }

    @Arbiter
    public void arbiter(I_Result r) {
        r.r1 = (int) Stream.of(rateResults).filter(b -> Boolean.TRUE.equals(b)).count();
    }
}