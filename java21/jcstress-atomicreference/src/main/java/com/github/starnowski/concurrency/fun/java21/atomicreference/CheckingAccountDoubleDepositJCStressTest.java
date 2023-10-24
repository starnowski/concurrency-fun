package com.github.starnowski.concurrency.fun.java21.atomicreference;

import com.github.starnowski.concurrency.fun.java21.TemporaryValueStore;
import org.mockito.Mockito;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.time.Instant.now;
import static org.mockito.Mockito.when;
import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.FORBIDDEN;

@JCStressTest
@Outcome(id = "1", expect = ACCEPTABLE, desc = "Current balance is correct")
@Outcome(id = "-1", expect = FORBIDDEN, desc = "Current balance is not correct")
@Outcome(id = "-2", expect = FORBIDDEN, desc = "Current balance was updated by first actor")
@Outcome(id = "-3", expect = FORBIDDEN, desc = "Current balance was updated by second actor")
@State
@Description("CheckingAccount is executed by two actors that are trying to deposit some amount")
public class CheckingAccountDoubleDepositJCStressTest {

    private CheckingAccount tested;

    public CheckingAccountDoubleDepositJCStressTest()
    {
        tested = new CheckingAccount(BigDecimal.valueOf(13.00));
    }

    @Actor
    public void actor1() {
        tested.deposit(BigDecimal.valueOf(10));
    }

    @Actor
    public void actor2() {
        tested.deposit(BigDecimal.valueOf(30));
    }

    @Arbiter
    public void arbiter(I_Result r) {
        BigDecimal currentBalance = tested.currentBalance();
        if (currentBalance.compareTo(BigDecimal.valueOf(53)) == 0) {
            r.r1 = 1;
        } else if (currentBalance.compareTo(BigDecimal.valueOf(23)) == 0) {
            r.r1 = -2;
        } else if (currentBalance.compareTo(BigDecimal.valueOf(43)) == 0) {
            r.r1 = -3;
        } else {
            r.r1 = -1;
        }
    }
}
