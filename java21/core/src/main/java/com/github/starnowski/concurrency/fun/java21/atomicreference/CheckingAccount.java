package com.github.starnowski.concurrency.fun.java21.atomicreference;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class CheckingAccount implements Account {

    private final AtomicReference<BigDecimal> current;
    private final Clock clock;
    private final List<OperationLog> logs = new CopyOnWriteArrayList<>();

    public CheckingAccount(BigDecimal bigDecimal) {
        this(bigDecimal, Clock.systemUTC());
    }

    public CheckingAccount(BigDecimal bigDecimal, Clock clock) {
        this.current = new AtomicReference<>(bigDecimal);
        this.clock = clock;
    }

    @Override
    public BigDecimal currentBalance() {
        return this.current.get();
    }

    @Override
    public BigDecimal deposit(BigDecimal amount) {
        boolean valueChanged;
        BigDecimal currentValue;
        do {
            currentValue = this.current.get();
            valueChanged = this.current.compareAndSet(currentValue, currentValue.add(amount));
        } while (!valueChanged);
        this.logs.add(new CheckingAccount.OperationLog(LocalDate.ofInstant(this.clock.instant(), ZoneId.systemDefault()), CheckingAccount.Operation.DEPOSIT, amount, currentValue.add(amount)));
        return currentBalance();
    }

    @Override
    public BigDecimal withdraw(BigDecimal amount) throws WithdrawException {
        boolean valueChanged;
        BigDecimal currentValue;
        {
            currentValue = this.current.get();
            if (currentValue.subtract(amount).compareTo(BigDecimal.ZERO) == -1) {
                throw new WithdrawException();
            }
            valueChanged = this.current.compareAndSet(currentValue, currentValue.subtract(amount));
        }
        while (!valueChanged) ;
        this.logs.add(new CheckingAccount.OperationLog(LocalDate.ofInstant(this.clock.instant(), ZoneId.systemDefault()), Operation.WITHDRAWAL, amount, currentValue.subtract(amount)));
        return currentBalance();
    }

    List<OperationLog> getLogs() {
        return this.logs;
    }

    @Override
    public String printStatement() {
        //TODO
        return null;
    }

    enum Operation {
        DEPOSIT,
        WITHDRAWAL
    }

    record OperationLog(LocalDate localDate, Operation operation, BigDecimal amount, BigDecimal balance) {
    }
}