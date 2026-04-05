package com.app.shared.models.item;

import com.app.shared.models.Entity;
import com.app.shared.models.user.User;

public abstract class Item extends Entity {
    protected String name, description;
    protected double startingPrice, currentHighestBid;
    protected String sellerId;
    protected transient User sellerObject;

    public Item(String name, String description, double startingPrice, String sellerId) {
        super();
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentHighestBid = startingPrice;
        this.sellerId = sellerId;
        this.sellerObject = null;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getStartingPrice() { return startingPrice; }
    public void setStartingPrice(double startingPrice) { this.startingPrice = startingPrice; }

    public double getCurrentHighestBid() { return currentHighestBid; }
    public void setCurrentHighestBid(double currentHighestBid) { this.currentHighestBid = currentHighestBid; }

    public String getSellerUsername() { return sellerId; }
    public void setSellerUsername(String sellerUsername) { this.sellerId = sellerId; }

}
