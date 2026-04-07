package com.app.shared.models.item;

public class Electronics extends Item {
    private String brand;

    public Electronics(String name, String desc, double startingPrice, String sellerId) {
        super(name, desc, startingPrice, sellerId);
    }

    public String getBrand() {
        return brand;
    }

    public Electronics setBrand(String brand) {
        this.brand = brand;
        return this;
    }

}
