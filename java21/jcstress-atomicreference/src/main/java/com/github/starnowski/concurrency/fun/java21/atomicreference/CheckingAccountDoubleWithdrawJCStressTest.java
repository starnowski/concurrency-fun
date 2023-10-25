package com.github.starnowski.concurrency.fun.java21.atomicreference;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;

import java.math.BigDecimal;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.FORBIDDEN;

@JCStressTest
@Outcome(id = "1", expect = ACCEPTABLE, desc = "Current balance is correct")
@Outcome(id = "-1", expect = FORBIDDEN, desc = "Current balance is not correct")
@Outcome(id = "-2", expect = FORBIDDEN, desc = "Current balance was updated by first actor")
@Outcome(id = "-3", expect = FORBIDDEN, desc = "Current balance was updated by second actor")
@State
@Description("CheckingAccount is executed by two actors that are trying to withdraw some amount")
public class CheckingAccountDoubleWithdrawJCStressTest {

    private final CheckingAccount tested;

    public CheckingAccountDoubleWithdrawJCStressTest() {
        tested = new CheckingAccount(BigDecimal.valueOf(53.00));
    }

    @Actor
    public void actor1() {
        try {
            tested.withdraw(BigDecimal.valueOf(10));
        } catch (Account.WithdrawException e) {
            //throw new RuntimeException(e);
        }
    }

    @Actor
    public void actor2() {
        try {
            tested.withdraw(BigDecimal.valueOf(30));
        } catch (Account.WithdrawException e) {
            //throw new RuntimeException(e);
        }
    }

    @Arbiter
    public void arbiter(I_Result r) {
        BigDecimal currentBalance = tested.currentBalance();
        if (currentBalance.compareTo(BigDecimal.valueOf(13)) == 0) {
            r.r1 = 1;
        } else if (currentBalance.compareTo(BigDecimal.valueOf(43)) == 0) {
            r.r1 = -2;
        } else if (currentBalance.compareTo(BigDecimal.valueOf(23)) == 0) {
            r.r1 = -3;
        } else {
            r.r1 = -1;
        }
    }
}
