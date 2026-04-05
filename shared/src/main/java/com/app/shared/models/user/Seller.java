package com.app.shared.models.user;

public class Seller extends User {
    private double totalRevenue;

    public Seller(String username, String password, String email) {
        super(username, password, email, "SELLER");
    }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

}
