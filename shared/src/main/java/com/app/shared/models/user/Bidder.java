package com.app.shared.models.user;

public class Bidder extends User {

    public Bidder(String username, String password, String email) {
        super(username, password, email, "BIDDER");
    }

}
