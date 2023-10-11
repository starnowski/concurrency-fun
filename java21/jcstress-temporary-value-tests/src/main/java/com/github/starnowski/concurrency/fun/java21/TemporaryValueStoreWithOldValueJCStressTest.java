package com.github.starnowski.concurrency.fun.java21;

import org.mockito.Mockito;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.time.Instant.now;
import static org.mockito.Mockito.when;
import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.FORBIDDEN;

@JCStressTest
@Outcome(id = "1", expect = ACCEPTABLE, desc = "Supplier was invoked twice but values for both actors are matches")
@Outcome(id = "-1", expect = FORBIDDEN, desc = "Supplier was not invoked twice but values for both actors matches")
@Outcome(id = "-2", expect = FORBIDDEN, desc = "Supplier was invoked twice but values for both actors are not matches")
@Outcome(id = "-3", expect = FORBIDDEN, desc = "Supplier was not invoked twice but values for both actors are not matches")
@State
@Description("TemporaryValueStore is executed by two actors that are trying to get temporary value that was not initialized yet")
public class TemporaryValueStoreWithOldValueJCStressTest {

    private TemporaryValueStore tested;
    private TemporaryValueStore.TemporaryValue value;
    private Supplier<TemporaryValueStore.TemporaryValue> temporaryValueSupplier;
    private final TemporaryValueStore.TemporaryValue[] temporaryValues = new TemporaryValueStore.TemporaryValue[2];

    public TemporaryValueStoreWithOldValueJCStressTest()
    {
        temporaryValueSupplier = Mockito.mock(Supplier.class);
        value = new TemporaryValueStore.TemporaryValue("Value", Duration.ofSeconds(100));
        Clock clock = Mockito.mock(Clock.class);
        when(clock.instant()).thenReturn(now().minus(Duration.ofSeconds(200)), now());
        tested = new TemporaryValueStore(temporaryValueSupplier, clock);
        when(temporaryValueSupplier.get()).thenReturn(new TemporaryValueStore.TemporaryValue("Initial Value", Duration.ofSeconds(100)), value);
        tested.get();
    }

    @Actor
    public void actor1() {
        temporaryValues[0] = tested.get();
    }

    @Actor
    public void actor2() {
        temporaryValues[1] = tested.get();
    }

    @Arbiter
    public void arbiter(I_Result r) {
        int count = (int) Stream.of(temporaryValues).filter(v -> value.equals(v)).count();
        boolean supplierExecutedOnce = doesInvocationNumberCorrect(2);
        if (count == 2 && supplierExecutedOnce) {
            r.r1 = 1;
        } else if (count == 2) {
            r.r1 = -1;
        } else if (!supplierExecutedOnce) {
            r.r1 = -2;
        } else {
            r.r1 = -3;
        }
    }

    private boolean doesInvocationNumberCorrect(int expectedCount)
    {
        try {
            Mockito.verify(temporaryValueSupplier, Mockito.times(expectedCount)).get();
            return true;
        } catch (RuntimeException runtimeException) {
            return false;
        }
    }
}
