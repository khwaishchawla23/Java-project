package com.maisonelle.service;

import com.maisonelle.model.Cart;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculates billing totals including subtotal, coupon deductions,
 * statutory 18% GST, and delivery logistics fees.
 */
public class BillingService {

    public static final BigDecimal GST_RATE = new BigDecimal("0.18");
    public static final BigDecimal FREE_DELIVERY_THRESHOLD = new BigDecimal("1000.00");
    public static final BigDecimal STANDARD_DELIVERY_FEE = new BigDecimal("99.00");

    public static class BillingSummary {
        private final BigDecimal subtotal;
        private final String couponCode;
        private final BigDecimal discountAmount;
        private final BigDecimal taxableAmount;
        private final BigDecimal gstAmount;
        private final BigDecimal deliveryFee;
        private final BigDecimal finalPayableAmount;
        private final boolean freeDeliveryApplied;

        public BillingSummary(BigDecimal subtotal, String couponCode, BigDecimal discountAmount,
                              BigDecimal taxableAmount, BigDecimal gstAmount, BigDecimal deliveryFee,
                              BigDecimal finalPayableAmount, boolean freeDeliveryApplied) {
            this.subtotal = subtotal;
            this.couponCode = couponCode;
            this.discountAmount = discountAmount;
            this.taxableAmount = taxableAmount;
            this.gstAmount = gstAmount;
            this.deliveryFee = deliveryFee;
            this.finalPayableAmount = finalPayableAmount;
            this.freeDeliveryApplied = freeDeliveryApplied;
        }

        public BigDecimal getSubtotal() { return subtotal; }
        public String getCouponCode() { return couponCode; }
        public BigDecimal getDiscountAmount() { return discountAmount; }
        public BigDecimal getTaxableAmount() { return taxableAmount; }
        public BigDecimal getGstAmount() { return gstAmount; }
        public BigDecimal getDeliveryFee() { return deliveryFee; }
        public BigDecimal getFinalPayableAmount() { return finalPayableAmount; }
        public boolean isFreeDeliveryApplied() { return freeDeliveryApplied; }
    }

    private final DiscountService discountService;

    public BillingService(DiscountService discountService) {
        this.discountService = discountService;
    }

    public BillingSummary calculateBilling(Cart cart, String couponCode) {
        BigDecimal subtotal = cart != null ? cart.getSubtotal() : BigDecimal.ZERO;

        BigDecimal discount = BigDecimal.ZERO;
        String appliedCode = null;

        if (couponCode != null && !couponCode.trim().isEmpty() && discountService != null) {
            DiscountService.CouponResult res = discountService.applyCoupon(couponCode, subtotal);
            if (res.isSuccess()) {
                discount = res.getDiscountAmount();
                appliedCode = res.getCode();
            }
        }

        BigDecimal taxable = subtotal.subtract(discount);
        if (taxable.compareTo(BigDecimal.ZERO) < 0) {
            taxable = BigDecimal.ZERO;
        }

        BigDecimal gst = taxable.multiply(GST_RATE).setScale(2, RoundingMode.HALF_UP);

        boolean freeDelivery = taxable.compareTo(FREE_DELIVERY_THRESHOLD) >= 0;
        BigDecimal delivery = subtotal.compareTo(BigDecimal.ZERO) == 0 || freeDelivery
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : STANDARD_DELIVERY_FEE.setScale(2, RoundingMode.HALF_UP);

        BigDecimal finalTotal = taxable.add(gst).add(delivery).setScale(2, RoundingMode.HALF_UP);

        return new BillingSummary(subtotal, appliedCode, discount, taxable, gst, delivery, finalTotal, freeDelivery);
    }
}
