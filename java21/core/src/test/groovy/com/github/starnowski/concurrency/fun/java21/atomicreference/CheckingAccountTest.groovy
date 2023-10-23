package com.github.starnowski.concurrency.fun.java21.atomicreference

import spock.lang.Specification
import spock.lang.Unroll
import java.time.ZoneId;

import java.time.Clock
import java.time.LocalDate

class CheckingAccountTest extends Specification {

    @Unroll
    def "should display current balance based on initial value #value"() {
        given:
        def tested = new CheckingAccount(value)

        when:
        def result = tested.currentBalance()

        then:
        result == value

        where:
        value << [BigDecimal.valueOf(13.0), BigDecimal.valueOf(0.0)]
    }

    @Unroll
    def "should add passed amount #value to current balance based #current and return updated value #expected"() {
        given:
        def tested = new CheckingAccount(current)

        when:
        def result = tested.deposit(value)

        then:
        result == expected

        and: "currentBalance return same result"
        expected == tested.currentBalance()

        where:
        current | value || expected
        BigDecimal.valueOf(0.0) | BigDecimal.valueOf(13.1) || BigDecimal.valueOf(13.1)
        BigDecimal.valueOf(14.00) | BigDecimal.valueOf(57.1) || BigDecimal.valueOf(71.10)
    }

    @Unroll
    def "should withdraw passed amount #value to current balance based #current and return updated value #expected"() {
        given:
        def tested = new CheckingAccount(current)

        when:
        def result = tested.withdraw(value)

        then:
        result == expected

        and: "currentBalance return same result"
        expected == tested.currentBalance()

        where:
        current | value || expected
        BigDecimal.valueOf(0.0) | BigDecimal.valueOf(1) || BigDecimal.valueOf(-1.0)
        BigDecimal.valueOf(14.00) | BigDecimal.valueOf(5.00) || BigDecimal.valueOf(9.00)
    }
//    Given a client makes a deposit of 2000 on 22-08-2022
//
//    And a withdrawal of 500 on 24-08-2022
//    Date               || Operation           || Amount    || Balance
//
//    22/08/2022    || DEPOSIT             || 2000         || 2000
//
//    24/08/2022    || WITHDRAWAL    || 500          || 1500

    @Unroll
    def "should return correct logs #expected when user deposit and then withdraw specific amount"(){
        given:
        Clock clock = Mock(Clock)
        def tested = new CheckingAccount(BigDecimal.valueOf(0), clock)
        clock.instant() >>> [LocalDate.of(2022, 8, 22).atStartOfDay(ZoneId.systemDefault()).toInstant(), LocalDate.of(2022, 8, 24).atStartOfDay(ZoneId.systemDefault()).toInstant() ]
        tested.deposit(BigDecimal.valueOf(2000))
        tested.withdraw(BigDecimal.valueOf(500))

        when:
        def results = tested.getLogs()

        then:
        results == expected

        where:
        expected << [[new CheckingAccount.OperationLog(LocalDate.of(2022, 8, 22), CheckingAccount.Operation.DEPOSIT, BigDecimal.valueOf(2000), BigDecimal.valueOf(2000)),
                      new CheckingAccount.OperationLog(LocalDate.of(2022, 8, 24), CheckingAccount.Operation.WITHDRAWAL, BigDecimal.valueOf(500), BigDecimal.valueOf(1500))]]
    }
}