package com.app.server;

import com.app.server.dao.UserDao;
import com.app.server.dao.UserDaoImpl;
import com.app.shared.models.user.User;

public class TestDB {
    public static void main(String[] args) {
        UserDao userDao = new UserDaoImpl();

        // 1. Thử tạo tài khoản mới
        System.out.println("--- ĐANG THỬ ĐĂNG KÝ ---");
        User newUser = new User("datdoz", "matkhau123", "datdoz@gmail.com", "BIDDER");
        boolean isRegistered = userDao.registerUser(newUser);

        if (isRegistered) {
            System.out.println("-> Đăng ký thành công User mới!");
        } else {
            System.out.println("-> Tài khoản đã tồn tại.");
        }

        // 2. Thử chức năng đăng nhập
        System.out.println("\n--- ĐANG THỬ ĐĂNG NHẬP ---");
        User loggedInUser = userDao.getUserByUsernameAndPassword("datdoz", "matkhau123");

        if (loggedInUser != null) {
            System.out.println("-> Đăng nhập thành công! Chào mừng: " + loggedInUser.getUsername());
        } else {
            System.out.println("-> Sai tài khoản hoặc mật khẩu!");
        }
    }
}