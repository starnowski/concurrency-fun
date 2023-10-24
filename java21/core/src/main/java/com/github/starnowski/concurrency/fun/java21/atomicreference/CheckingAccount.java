package com.github.starnowski.concurrency.fun.java21.atomicreference;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class CheckingAccount implements Account{

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
        this.current.set(this.current.get().add(amount));
        this.logs.add(new CheckingAccount.OperationLog(LocalDate.ofInstant(this.clock.instant(), ZoneId.systemDefault()), CheckingAccount.Operation.DEPOSIT, amount, this.current.get()));
        return currentBalance();
    }

    @Override
    public BigDecimal withdraw(BigDecimal amount) throws WithdrawException {
        if (this.current.get().subtract(amount).compareTo(BigDecimal.ZERO) == -1) {
            throw new WithdrawException();
        }
        this.current.set(this.current.get().subtract(amount));
        this.logs.add(new CheckingAccount.OperationLog(LocalDate.ofInstant(this.clock.instant(), ZoneId.systemDefault()), Operation.WITHDRAWAL, amount, this.current.get()));
        return currentBalance();
    }

    List<OperationLog> getLogs(){
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

    record OperationLog(LocalDate localDate, Operation operation, BigDecimal amount, BigDecimal balance){}
}