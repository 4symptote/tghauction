package com.app.shared.models.item;

public class Vehicle extends Item {
    private String brand;

    public Vehicle(String name, String description, double startingPrice, String sellerId) {
        super(name, description, startingPrice, sellerId);
    }

    public String getBrand() { return this.brand; }

    public Vehicle setBrand(String brand) {
        this.brand = brand;
        return this;
    }

}
