package org.vaadin.tutorial.backend.financial;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    void constructor_setsScaleToTwo() {
        var money = new Money(new BigDecimal("10"));

        assertEquals(2, money.amount().scale());
        assertEquals(new BigDecimal("10.00"), money.amount());
    }

    @Test
    void constructor_roundsUsingHalfEven() {
        assertEquals(new BigDecimal("10.12"), new Money(new BigDecimal("10.125")).amount());
        assertEquals(new BigDecimal("10.14"), new Money(new BigDecimal("10.135")).amount());
        assertEquals(new BigDecimal("10.13"), new Money(new BigDecimal("10.1250001")).amount());
    }

    @Test
    void constructor_preservesExactTwoDecimalValues() {
        var money = new Money(new BigDecimal("99.99"));

        assertEquals(new BigDecimal("99.99"), money.amount());
    }

    @Test
    void constructor_truncatesExtraDecimals() {
        var money = new Money(new BigDecimal("10.999"));

        assertEquals(new BigDecimal("11.00"), money.amount());
    }

    @Test
    void mathContext_hasCorrectPrecision() {
        assertEquals(16, Money.MATH_CONTEXT.getPrecision());
    }

    @Test
    void mathContext_usesHalfEvenRounding() {
        assertEquals(RoundingMode.HALF_EVEN, Money.MATH_CONTEXT.getRoundingMode());
    }

    @Test
    void scale_isTwo() {
        assertEquals(2, Money.SCALE);
    }

    @Test
    void toString_returnsAmountString() {
        var money = new Money(new BigDecimal("123.45"));

        assertEquals("123.45", money.toString());
    }

    @Test
    void toString_includesTrailingZeros() {
        var money = new Money(new BigDecimal("100"));

        assertEquals("100.00", money.toString());
    }

    @Test
    void equals_comparesAmounts() {
        var money1 = new Money(new BigDecimal("50.00"));
        var money2 = new Money(new BigDecimal("50"));

        assertEquals(money1, money2);
    }
}
