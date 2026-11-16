package com.maisonelle.model;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Represents a luxury boutique product in the Maisonelle catalogue.
 * Implements encapsulation and Serializable for local data persistence.
 */
public class Product implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String category;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private int stock;
    private String imagePath;
    private double rating;
    private String brand;
    private boolean featured;

    // Full constructor
    public Product(int id, String name, String category, String description,
                   BigDecimal price, BigDecimal originalPrice, int stock,
                   String imagePath, double rating, String brand, boolean featured) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.price = price != null ? price.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.originalPrice = originalPrice != null ? originalPrice.setScale(2, RoundingMode.HALF_UP) : this.price;
        this.stock = Math.max(0, stock);
        this.imagePath = imagePath;
        this.rating = rating;
        this.brand = brand;
        this.featured = featured;
    }

    // Overloaded constructor without discount
    public Product(int id, String name, String category, String description,
                   BigDecimal price, int stock, String imagePath,
                   double rating, String brand, boolean featured) {
        this(id, name, category, description, price, price, stock, imagePath, rating, brand, featured);
    }

    public boolean hasDiscount() {
        return originalPrice != null && originalPrice.compareTo(price) > 0;
    }

    public int getDiscountPercentage() {
        if (!hasDiscount() || originalPrice.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        BigDecimal diff = originalPrice.subtract(price);
        BigDecimal ratio = diff.divide(originalPrice, 4, RoundingMode.HALF_UP);
        return ratio.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).intValue();
    }

    public synchronized boolean reduceStock(int quantity) {
        if (quantity > 0 && this.stock >= quantity) {
            this.stock -= quantity;
            return true;
        }
        return false;
    }

    public synchronized void increaseStock(int quantity) {
        if (quantity > 0) {
            this.stock += quantity;
        }
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) {
        this.price = price != null ? price.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice != null ? originalPrice.setScale(2, RoundingMode.HALF_UP) : this.price;
    }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = Math.max(0, stock); }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public boolean isFeatured() { return featured; }
    public void setFeatured(boolean featured) { this.featured = featured; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product product)) return false;
        return id == product.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name + " (" + brand + ") - ₹" + price;
    }
}
