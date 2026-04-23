package com.app.shared.models.user;

//public class Bidder extends User {
//
//    public Bidder(String username, String password, String email) {
//        super(username, password, email, "BIDDER");
//    }
//
//}

package com.app.shared.models.user;

import com.app.shared.models.BidTransaction;
import java.util.ArrayList;
import java.util.List;

public class Bidder extends User {
    private double balance; // Số dư tài khoản để đặt giá
    private List<BidTransaction> bidHistory; // Lịch sử các lần trả giá

    public Bidder(String username, String password, String email, double initialBalance) {
        super(username, password, email, "BIDDER");
        this.balance = initialBalance;
        this.bidHistory = new ArrayList<>();
    }

    // Logic đặc thù của Bidder
    public void addBidToHistory(BidTransaction transaction) {
        this.bidHistory.add(transaction);
    }

    // Getters and Setters
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public List<BidTransaction> getBidHistory() { return bidHistory; }
}

