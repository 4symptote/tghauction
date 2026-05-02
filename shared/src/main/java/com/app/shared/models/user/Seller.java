package com.app.shared.models.user;

//public class Seller extends User {
//    private double totalRevenue;
//
//    public Seller(String username, String password, String email) {
//        super(username, password, email, "SELLER");
//    }
//
//    public double getTotalRevenue() { return totalRevenue; }
//    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
//
//}

import com.app.shared.models.item.Item;
import com.app.shared.models.auction.Auction;
import java.util.ArrayList;
import java.util.List;

public class Seller extends User {
    private double totalRevenue;
    private List<Item> listedItems;
    private List<Auction> auctionHistory;

    public Seller(String username, String password, String email) {
        super(username, password, email, "SELLER");
        this.totalRevenue = 0.0;
        this.listedItems = new ArrayList<>();
        this.auctionHistory = new ArrayList<>();
    }

    public void listItem(Item item) {
        this.listedItems.add(item);
    }

    public void collectRevenue(double amount) {
        this.totalRevenue += amount;
    }

    public double getTotalRevenue() { return totalRevenue; }
    public List<Item> getListedItems() { return listedItems; }
}