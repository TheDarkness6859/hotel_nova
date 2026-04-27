package com.hotelNova.dao;

import java.util.List;
import java.util.Optional;

public interface GenericDAO <T> {

    List<T> findAll ();
    Optional<T> findByIdOrName (String value);
    boolean delete (String value);
    boolean edit (T object);
    boolean save (T object);

}
