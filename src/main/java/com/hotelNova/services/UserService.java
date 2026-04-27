package com.hotelNova.services;

import com.hotelNova.models.User;

import java.util.Optional;

public interface UserService extends GenericService<User> {

    Optional<User> login(String username, String password);

}
