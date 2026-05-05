package com.app.server.dao;
import com.app.shared.models.user.User;

public interface UserDao {
    User getUserByUsernameAndPassword(String username, String password);
    boolean registerUser(User user);
}