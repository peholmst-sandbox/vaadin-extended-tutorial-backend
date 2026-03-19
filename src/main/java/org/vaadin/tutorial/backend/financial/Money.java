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
     * A Money instance representing zero.
     */
    public static final Money ZERO = new Money(BigDecimal.ZERO);

    /**
     * Creates a new Money instance, normalizing the amount to {@value #SCALE} decimal places.
     *
     * @param amount the monetary amount
     */
    public Money {
        amount = amount.setScale(SCALE, MATH_CONTEXT.getRoundingMode());
    }

    /**
     * Adds the given money to this amount.
     *
     * @param other the amount to add
     * @return a new Money instance with the summed amount
     */
    public Money add(Money other) {
        return new Money(amount.add(other.amount, MATH_CONTEXT));
    }

    /**
     * Subtracts the given money from this amount.
     *
     * @param other the amount to subtract
     * @return a new Money instance with the difference
     */
    public Money subtract(Money other) {
        return new Money(amount.subtract(other.amount, MATH_CONTEXT));
    }

    /**
     * Multiplies this amount by the given quantity.
     *
     * @param quantity the quantity to multiply by
     * @return a new Money instance with the multiplied amount
     */
    public Money multiply(int quantity) {
        return new Money(amount.multiply(BigDecimal.valueOf(quantity), MATH_CONTEXT));
    }

    /**
     * Returns {@code true} if this amount is strictly greater than zero.
     *
     * @return {@code true} if the amount is positive
     */
    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
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
