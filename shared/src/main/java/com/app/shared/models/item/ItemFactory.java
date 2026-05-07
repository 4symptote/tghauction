package com.app.shared.models.item;

public class ItemFactory {
    // Factory method để tạo các loại Item khác nhau
    public static Item createItem(String type, String name, String desc, double startingPrice, String sellerId) {
        switch (type.toUpperCase()) {
            case "ELECTRONICS":
                return new Electronics(name, desc, startingPrice, sellerId);
            case "ART":
                return new Art(name, desc, startingPrice, sellerId);
            case "VEHICLE":
                return new Vehicle(name, desc, startingPrice, sellerId );
            default:
                throw new IllegalArgumentException("Loại sản phẩm không hợp lệ: " + type);
        }
    }
}