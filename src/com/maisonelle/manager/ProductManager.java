package com.maisonelle.manager;

import com.maisonelle.model.Product;
import com.maisonelle.util.DataManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages the boutique product catalogue, search, sorting, filtering, and stock adjustments.
 */
public class ProductManager {

    private final ArrayList<Product> products;
    private final DataManager dataManager;

    public ProductManager(DataManager dataManager) {
        this.dataManager = dataManager;
        this.products = dataManager.loadProducts();
    }

    public synchronized List<Product> getAllProducts() {
        return new ArrayList<>(products);
    }

    public synchronized Product getProductById(int id) {
        for (Product p : products) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    public synchronized boolean addProduct(Product product) {
        if (product == null) return false;

        if (product.getId() <= 0 || getProductById(product.getId()) != null) {
            int maxId = products.stream().mapToInt(Product::getId).max().orElse(100);
            product.setId(maxId + 1);
        }

        products.add(product);
        dataManager.saveProducts(products);
        return true;
    }

    public synchronized boolean updateProduct(Product updated) {
        if (updated == null) return false;

        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId() == updated.getId()) {
                products.set(i, updated);
                dataManager.saveProducts(products);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean deleteProduct(int id) {
        boolean removed = products.removeIf(p -> p.getId() == id);
        if (removed) {
            dataManager.saveProducts(products);
        }
        return removed;
    }

    public synchronized boolean updatePrice(int id, BigDecimal newPrice) {
        Product p = getProductById(id);
        if (p != null && newPrice != null && newPrice.compareTo(BigDecimal.ZERO) >= 0) {
            p.setPrice(newPrice);
            dataManager.saveProducts(products);
            return true;
        }
        return false;
    }

    public synchronized boolean updateStock(int id, int newStock) {
        Product p = getProductById(id);
        if (p != null && newStock >= 0) {
            p.setStock(newStock);
            dataManager.saveProducts(products);
            return true;
        }
        return false;
    }

    public synchronized List<Product> search(String query) {
        return filterAndSort("All", query, "Name: A to Z");
    }

    public synchronized List<Product> filterByCategory(String category) {
        return filterAndSort(category, "", "Price: Low to High");
    }

    public synchronized List<Product> filterAndSort(String category, String searchQuery, String sortBy) {
        return products.stream()
                .filter(p -> {
                    if (category == null || category.equalsIgnoreCase("All") || category.trim().isEmpty()) {
                        return true;
                    }
                    return p.getCategory().equalsIgnoreCase(category.trim());
                })
                .filter(p -> {
                    if (searchQuery == null || searchQuery.trim().isEmpty()) {
                        return true;
                    }
                    String q = searchQuery.trim().toLowerCase();
                    return p.getName().toLowerCase().contains(q)
                            || p.getBrand().toLowerCase().contains(q)
                            || p.getDescription().toLowerCase().contains(q);
                })
                .sorted((p1, p2) -> {
                    if (sortBy == null) return 0;
                    return switch (sortBy) {
                        case "Price: Low to High" -> p1.getPrice().compareTo(p2.getPrice());
                        case "Price: High to Low" -> p2.getPrice().compareTo(p1.getPrice());
                        case "Biggest Discounts (% Off)" -> Integer.compare(p2.getDiscountPercentage(), p1.getDiscountPercentage());
                        case "Top Customer Rating" -> Double.compare(p2.getRating(), p1.getRating());
                        case "Name: A to Z" -> p1.getName().compareToIgnoreCase(p2.getName());
                        default -> {
                            // Featured first, then rating
                            if (p1.isFeatured() != p2.isFeatured()) {
                                yield p1.isFeatured() ? -1 : 1;
                            }
                            yield Double.compare(p2.getRating(), p1.getRating());
                        }
                    };
                })
                .collect(Collectors.toList());
    }

    public synchronized List<Product> getFeaturedProducts() {
        return products.stream()
                .filter(Product::isFeatured)
                .collect(Collectors.toList());
    }

    public synchronized List<Product> getDiscountedProducts() {
        return products.stream()
                .filter(Product::hasDiscount)
                .sorted(Comparator.comparingInt(Product::getDiscountPercentage).reversed())
                .collect(Collectors.toList());
    }

    public synchronized List<Product> getLowStockProducts(int threshold) {
        return products.stream()
                .filter(p -> p.getStock() <= threshold)
                .sorted(Comparator.comparingInt(Product::getStock))
                .collect(Collectors.toList());
    }

    public synchronized List<String> getCategories() {
        List<String> list = new ArrayList<>();
        list.add("All");
        list.add("Electronics");
        list.add("Fashion");
        list.add("Home & Living");
        list.add("Accessories");
        list.add("Beauty & Lifestyle");
        return list;
    }

    public synchronized int getTotalProductsCount() {
        return products.size();
    }
}
