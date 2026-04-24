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

import { User } from "./User";
import { Item } from "../item/Item";
import { auction } from "../auction/Auction";

export class Seller extends User {
    private totalRevenue: number = 0;
    private listedItems: Item[] = [];
    private auctionHistory: Auction[] = [];

    constructor(username: string, password: string | undefined, email: string) {
        super(username, password, email, "SELLER");
    }

    // Logic đặc thù của Seller
    public listItem(item: Item): void {
        this.listedItems.push(item);
    }

    public addAuctionToHistory(auction: Auction): void {
        this.auctionHistory.push(auction);
    }

    public collectRevenue(amount: number): void {
        this.totalRevenue += amount;
    }

    // Getters and Setters
    public getTotalRevenue(): number { return this.totalRevenue; }
    public setTotalRevenue(totalRevenue: number): void { this.totalRevenue = totalRevenue; }

    public getListedItems(): Item[] { return this.listedItems; }
    public getAuctionHistory(): Auction[] { return this.auctionHistory; }
}

