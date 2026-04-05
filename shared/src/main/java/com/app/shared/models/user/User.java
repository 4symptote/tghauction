package com.app.shared.models.user;

import com.app.shared.models.Entity;

public abstract class User extends Entity {
    protected String username;
    protected String password;
    protected String email;
    protected String role; // bidder - seller - admin

    public User(String username, String password, String email, String role) {
        super();
        this.username = username;
        this.password = password;
        this.email    = email;
        this.role     = role;
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // todo (probably): User specific dashboard
}
