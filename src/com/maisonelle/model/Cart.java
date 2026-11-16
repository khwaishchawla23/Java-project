package com.maisonelle.model;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the user's active shopping cart session.
 * Demonstrates Collections Framework (ArrayList) and BigDecimal precision.
 */
public class Cart implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ArrayList<CartItem> items;

    public Cart() {
        this.items = new ArrayList<>();
    }

    public synchronized boolean addItem(Product product, int quantity) {
        if (product == null || quantity <= 0) {
            return false;
        }

        for (CartItem item : items) {
            if (item.getProduct().getId() == product.getId()) {
                int newQty = item.getQuantity() + quantity;
                if (newQty > product.getStock()) {
                    return false; // Exceeds available stock
                }
                item.setQuantity(newQty);
                return true;
            }
        }

        if (quantity > product.getStock()) {
            return false;
        }

        items.add(new CartItem(product, quantity));
        return true;
    }

    public synchronized boolean updateQuantity(int productId, int quantity) {
        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);
            if (item.getProduct().getId() == productId) {
                if (quantity <= 0) {
                    items.remove(i);
                    return true;
                }
                if (quantity > item.getProduct().getStock()) {
                    return false;
                }
                item.setQuantity(quantity);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean removeItem(int productId) {
        return items.removeIf(item -> item.getProduct().getId() == productId);
    }

    public synchronized void clear() {
        items.clear();
    }

    public synchronized List<CartItem> getItems() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

    public synchronized boolean isEmpty() {
        return items.isEmpty();
    }

    public synchronized int getTotalItemCount() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public synchronized BigDecimal getSubtotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : items) {
            total = total.add(item.getSubtotal());
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }
}
