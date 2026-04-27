package com.hotelNova.services;

import com.hotelNova.models.User;

import java.util.List;
import java.util.Optional;

public interface GenericService <T> {

    List<T> findAll ();
    Optional<T> findByIdOrName (String value);
    boolean delete (String value);
    boolean edit (T object);
    boolean save (T object);

}
