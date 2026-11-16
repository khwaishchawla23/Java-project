package com.maisonelle.model;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Promotional discount voucher with percentage or flat deduction rules.
 */
public class Coupon implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public enum Type {
        PERCENTAGE,
        FLAT
    }

    private final String code;
    private final Type type;
    private final BigDecimal discountValue;
    private final BigDecimal minPurchase;
    private final BigDecimal maxDiscount;
    private final String description;
    private boolean active;

    public Coupon(String code, Type type, BigDecimal discountValue,
                  BigDecimal minPurchase, BigDecimal maxDiscount,
                  String description, boolean active) {
        this.code = code != null ? code.trim().toUpperCase() : "";
        this.type = type != null ? type : Type.PERCENTAGE;
        this.discountValue = discountValue != null ? discountValue : BigDecimal.ZERO;
        this.minPurchase = minPurchase != null ? minPurchase : BigDecimal.ZERO;
        this.maxDiscount = maxDiscount != null ? maxDiscount : BigDecimal.ZERO;
        this.description = description;
        this.active = active;
    }

    public BigDecimal calculateDiscount(BigDecimal cartSubtotal) {
        if (!active || cartSubtotal == null || cartSubtotal.compareTo(minPurchase) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount;
        if (type == Type.PERCENTAGE) {
            discount = cartSubtotal.multiply(discountValue)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (maxDiscount.compareTo(BigDecimal.ZERO) > 0 && discount.compareTo(maxDiscount) > 0) {
                discount = maxDiscount;
            }
        } else {
            discount = discountValue;
        }

        if (discount.compareTo(cartSubtotal) > 0) {
            discount = cartSubtotal;
        }

        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    public String getCode() { return code; }
    public Type getType() { return type; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public BigDecimal getMinPurchase() { return minPurchase; }
    public BigDecimal getMaxDiscount() { return maxDiscount; }
    public String getDescription() { return description; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return code + " - " + description;
    }
}
