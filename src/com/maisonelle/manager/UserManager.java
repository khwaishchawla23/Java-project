package com.maisonelle.manager;

import com.maisonelle.model.Customer;
import com.maisonelle.util.DataManager;
import com.maisonelle.util.PasswordUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Manages customer registration, authentication, session state, and password security.
 */
public class UserManager {

    private final HashMap<Integer, Customer> customers;
    private final DataManager dataManager;
    private Customer currentCustomer;

    public UserManager(DataManager dataManager) {
        this.dataManager = dataManager;
        this.customers = dataManager.loadCustomers();
    }

    public synchronized Customer getCurrentUser() {
        return currentCustomer;
    }

    public synchronized void setCurrentUser(Customer customer) {
        this.currentCustomer = customer;
    }

    public synchronized boolean isLoggedIn() {
        return currentCustomer != null;
    }

    public synchronized boolean isAdmin() {
        return currentCustomer != null && currentCustomer.isAdmin();
    }

    public synchronized void logout() {
        this.currentCustomer = null;
    }

    public synchronized Customer authenticate(String email, String password) throws Exception {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Email and password cannot be empty.");
        }

        String cleanEmail = email.trim().toLowerCase();
        for (Customer c : customers.values()) {
            if (c.getEmail().equalsIgnoreCase(cleanEmail)) {
                boolean valid = PasswordUtil.verifyPassword(password, c.getPasswordHash(), c.getSalt());
                if (valid) {
                    this.currentCustomer = c;
                    return c;
                } else {
                    throw new IllegalArgumentException("Incorrect password. Please try again.");
                }
            }
        }
        throw new IllegalArgumentException("No account found with email: " + email);
    }

    public synchronized Customer register(String fullName, String email, String phone,
                                          String address, String city, String pincode,
                                          String password) throws Exception {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required.");
        }
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            throw new IllegalArgumentException("A valid email address is required.");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Contact phone number is required.");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }

        String cleanEmail = email.trim().toLowerCase();
        for (Customer c : customers.values()) {
            if (c.getEmail().equalsIgnoreCase(cleanEmail)) {
                throw new IllegalArgumentException("An account with this email address already exists.");
            }
        }

        int nextId = customers.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(password, salt);

        Customer newCustomer = new Customer(nextId, fullName.trim(), cleanEmail, phone.trim(),
                address != null ? address.trim() : "",
                city != null ? city.trim() : "",
                pincode != null ? pincode.trim() : "",
                hash, salt, Customer.Role.CUSTOMER);

        customers.put(nextId, newCustomer);
        dataManager.saveCustomers(customers);

        return newCustomer;
    }

    public synchronized List<Customer> getAllCustomers() {
        return new ArrayList<>(customers.values());
    }

    public synchronized Customer getCustomerById(int id) {
        return customers.get(id);
    }

    public synchronized int getTotalCustomerCount() {
        return customers.size();
    }
}
