package com.github.starnowski.concurrency.fun.java21.atomicreference;

import java.math.BigDecimal;

public interface Account {

    BigDecimal currentBalance();

    BigDecimal deposit(BigDecimal amount);

    BigDecimal withdraw(BigDecimal amount) throws WithdrawException;

    String  printStatement();

    class WithdrawException extends Exception{}
}
