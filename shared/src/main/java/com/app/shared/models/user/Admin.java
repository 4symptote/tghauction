package com.app.shared.models.user;

public class Admin extends User {

    public Admin(String username, String password, String email) {
        super(username, password, email, "ADMIN");
    }


}
