package com.hotelNova.controllers;

import com.hotelNova.models.User;
import com.hotelNova.services.UserService;
import com.hotelNova.services.impl.UserServiceImpl;
import com.hotelNova.utils.LogManager;
import java.util.List;
import java.util.Optional;

public class UserController {

    private final UserService userService = new UserServiceImpl();

    public Optional<User> login(String username, String password) {

        if (username.isBlank() || password.isBlank()) {

            LogManager.addLog("WARNING", "Login attempt with empty fields");
            return Optional.empty();

        }

        return userService.login(username, password);

    }

    public boolean saveUser(User user) {

        return userService.save(user);

    }

    public List<User> getAllUsers() {

        return userService.findAll();

    }

    public boolean deleteUser(String id) {

        return userService.delete(id);

    }
}