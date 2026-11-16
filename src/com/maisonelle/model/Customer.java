package com.maisonelle.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a customer or administrator account in Maisonelle.
 * Passwords are never stored in plain text.
 */
public class Customer implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public enum Role {
        CUSTOMER,
        ADMIN
    }

    private int id;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String pincode;
    private String passwordHash;
    private String salt;
    private Role role;
    private LocalDateTime registeredDate;

    public Customer(int id, String fullName, String email, String phone,
                    String address, String city, String pincode,
                    String passwordHash, String salt, Role role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email != null ? email.trim().toLowerCase() : "";
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.pincode = pincode;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.role = role != null ? role : Role.CUSTOMER;
        this.registeredDate = LocalDateTime.now();
    }

    // Overloaded convenience constructor for registration
    public Customer(int id, String fullName, String email, String phone,
                    String passwordHash, String salt) {
        this(id, fullName, email, phone, "", "", "", passwordHash, salt, Role.CUSTOMER);
    }

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        this.email = email != null ? email.trim().toLowerCase() : "";
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getRegisteredDate() { return registeredDate; }
    public void setRegisteredDate(LocalDateTime registeredDate) { this.registeredDate = registeredDate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer customer)) return false;
        return id == customer.id || Objects.equals(email, customer.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return fullName + " (" + email + ") [" + role + "]";
    }
}
