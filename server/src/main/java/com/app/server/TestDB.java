package com.app.server;

import com.app.server.dao.UserDao;
import com.app.server.dao.UserDaoImpl;
import com.app.shared.models.user.User;

public class TestDB {
    public static void main(String[] args) {
        UserDao userDao = new UserDaoImpl();

        // 1. Thử chức năng Login với user có sẵn trong SQL (admin / admin123)
        User loggedInUser = userDao.getUserByUsernameAndPassword("admin", "admin123");
        if (loggedInUser != null) {
            System.out.println("Đăng nhập thành công! Chào: " + loggedInUser.getUsername());
        } else {
            System.out.println("Sai tài khoản hoặc mật khẩu!");
        }

        // 2. Thử chức năng tạo mới
        User newUser = new User("datdoz", "matkhau123", "datdoz@gmail.com", "BIDDER");
        boolean isRegistered = userDao.registerUser(newUser);
        if (isRegistered) {
            System.out.println("Đăng ký thành công User mới!");
        }
    }
}