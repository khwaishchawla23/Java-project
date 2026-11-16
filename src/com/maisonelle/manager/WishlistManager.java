package com.maisonelle.manager;

import com.maisonelle.model.Product;
import com.maisonelle.util.DataManager;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Manages customer saved wishlist items and transfers to the active cart.
 */
public class WishlistManager {

    private final LinkedHashSet<Integer> wishlistIds;
    private final DataManager dataManager;
    private final ProductManager productManager;

    public WishlistManager(DataManager dataManager, ProductManager productManager) {
        this.dataManager = dataManager;
        this.productManager = productManager;
        this.wishlistIds = dataManager.loadWishlist();
    }

    public synchronized boolean isInWishlist(int productId) {
        return wishlistIds.contains(productId);
    }

    public synchronized boolean addToWishlist(int productId) {
        boolean added = wishlistIds.add(productId);
        if (added) {
            dataManager.saveWishlist(wishlistIds);
        }
        return added;
    }

    public synchronized boolean removeFromWishlist(int productId) {
        boolean removed = wishlistIds.remove(productId);
        if (removed) {
            dataManager.saveWishlist(wishlistIds);
        }
        return removed;
    }

    public synchronized boolean toggleWishlist(Product product) {
        if (product == null) return false;
        if (isInWishlist(product.getId())) {
            removeFromWishlist(product.getId());
            return false;
        } else {
            addToWishlist(product.getId());
            return true;
        }
    }

    public synchronized boolean moveToCart(int productId, CartManager cartManager) {
        Product p = productManager.getProductById(productId);
        if (p != null && p.getStock() > 0) {
            boolean added = cartManager.addToCart(p, 1);
            if (added) {
                removeFromWishlist(productId);
                return true;
            }
        }
        return false;
    }

    public synchronized List<Product> getWishlistProducts() {
        List<Product> list = new ArrayList<>();
        for (int id : wishlistIds) {
            Product p = productManager.getProductById(id);
            if (p != null) {
                list.add(p);
            }
        }
        return list;
    }

    public synchronized int getWishlistCount() {
        return wishlistIds.size();
    }
}
