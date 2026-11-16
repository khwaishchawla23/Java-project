package com.maisonelle.service;

import com.maisonelle.model.Coupon;
import com.maisonelle.util.DataManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates promotional codes and calculates discount amounts.
 * Demonstrates separation of business logic from graphical presentation.
 */
public class DiscountService {

    public static class CouponResult {
        private final boolean success;
        private final BigDecimal discountAmount;
        private final String message;
        private final String code;

        public CouponResult(boolean success, BigDecimal discountAmount, String message, String code) {
            this.success = success;
            this.discountAmount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
            this.message = message;
            this.code = code;
        }

        public boolean isSuccess() { return success; }
        public BigDecimal getDiscountAmount() { return discountAmount; }
        public String getMessage() { return message; }
        public String getCode() { return code; }
    }

    private final ArrayList<Coupon> coupons;
    private final DataManager dataManager;

    public DiscountService(DataManager dataManager) {
        this.dataManager = dataManager;
        this.coupons = dataManager.loadCoupons();
    }

    public synchronized CouponResult applyCoupon(String rawCode, BigDecimal subtotal) {
        if (rawCode == null || rawCode.trim().isEmpty()) {
            return new CouponResult(false, BigDecimal.ZERO, "No coupon code entered.", null);
        }

        String code = rawCode.trim().toUpperCase();
        Coupon matched = null;
        for (Coupon c : coupons) {
            if (c.getCode().equalsIgnoreCase(code)) {
                matched = c;
                break;
            }
        }

        if (matched == null) {
            return new CouponResult(false, BigDecimal.ZERO, "Invalid coupon code: " + code, code);
        }

        if (!matched.isActive()) {
            return new CouponResult(false, BigDecimal.ZERO, "Coupon " + code + " has expired.", code);
        }

        if (subtotal == null || subtotal.compareTo(matched.getMinPurchase()) < 0) {
            return new CouponResult(false, BigDecimal.ZERO,
                    "Coupon " + code + " requires a minimum cart subtotal of ₹" + matched.getMinPurchase(), code);
        }

        BigDecimal discount = matched.calculateDiscount(subtotal);
        return new CouponResult(true, discount,
                "Success! " + matched.getDescription() + " (-₹" + discount + ")", code);
    }

    public synchronized List<Coupon> getAllCoupons() {
        return new ArrayList<>(coupons);
    }
}
