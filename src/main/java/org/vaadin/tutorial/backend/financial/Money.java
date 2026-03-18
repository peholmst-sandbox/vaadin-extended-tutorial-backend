package org.vaadin.tutorial.backend.financial;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public record Money(BigDecimal amount) {

    public static final int SCALE = 2;
    public static final MathContext MATH_CONTEXT = new MathContext(16, RoundingMode.HALF_EVEN);

    public Money {
        amount = amount.setScale(SCALE, MATH_CONTEXT.getRoundingMode());
    }

    @Override
    public String toString() {
        return amount.toString();
    }
}
