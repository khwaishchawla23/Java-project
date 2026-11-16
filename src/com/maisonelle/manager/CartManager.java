package com.maisonelle.manager;

import com.maisonelle.model.Cart;
import com.maisonelle.model.CartItem;
import com.maisonelle.model.Product;

import java.math.BigDecimal;
import java.util.List;

/**
 * Manages the active shopping cart operations and stock boundaries.
 */
public class CartManager {

    private final Cart cart;

    public CartManager() {
        this.cart = new Cart();
    }

    public synchronized Cart getCart() {
        return cart;
    }

    public synchronized boolean addToCart(Product product, int quantity) {
        return cart.addItem(product, quantity);
    }

    public synchronized boolean updateQuantity(int productId, int quantity) {
        return cart.updateQuantity(productId, quantity);
    }

    public synchronized boolean removeFromCart(int productId) {
        return cart.removeItem(productId);
    }

    public synchronized void clearCart() {
        cart.clear();
    }

    public synchronized List<CartItem> getItems() {
        return cart.getItems();
    }

    public synchronized int getTotalItemCount() {
        return cart.getTotalItemCount();
    }

    public synchronized BigDecimal getSubtotal() {
        return cart.getSubtotal();
    }

    public synchronized boolean isEmpty() {
        return cart.isEmpty();
    }
}
