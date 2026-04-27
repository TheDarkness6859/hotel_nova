package com.hotelNova.models;

public class Receptionist extends User{

    public Receptionist(String username, String password) {

        super(username, password, "RECEPTIONIST");

    }

    public Receptionist(String id, String username, String password) {

        super(id, username, password, "RECEPTIONIST");

    }

}