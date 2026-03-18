package org.vaadin.tutorial.backend.financial;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Represents a monetary amount with fixed precision suitable for financial calculations.
 * <p>
 * All amounts are normalized to {@value #SCALE} decimal places using
 * {@link RoundingMode#HALF_EVEN} (banker's rounding) to ensure consistent
 * representation and arithmetic behavior.
 *
 * @param amount the monetary amount, normalized to {@value #SCALE} decimal places
 */
public record Money(BigDecimal amount) {

    /**
     * The number of decimal places used for all monetary amounts.
     */
    public static final int SCALE = 2;

    /**
     * The math context used for monetary calculations, providing 16 digits of precision
     * with {@link RoundingMode#HALF_EVEN} (banker's rounding).
     */
    public static final MathContext MATH_CONTEXT = new MathContext(16, RoundingMode.HALF_EVEN);

    /**
     * Creates a new Money instance, normalizing the amount to {@value #SCALE} decimal places.
     *
     * @param amount the monetary amount
     */
    public Money {
        amount = amount.setScale(SCALE, MATH_CONTEXT.getRoundingMode());
    }

    /**
     * Returns the string representation of the amount.
     *
     * @return the amount as a string with {@value #SCALE} decimal places
     */
    @Override
    public String toString() {
        return amount.toString();
    }
}
