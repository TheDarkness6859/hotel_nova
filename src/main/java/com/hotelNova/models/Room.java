package com.hotelNova.models;

public class Room {

    private String id;
    private int roomNumber;
    private String type;
    private double pricePerNight;
    private boolean isAvailable;

    public Room (int roomNumber,
                 String type,
                 double pricePerNight,
                 boolean isAvailable){

        setRoomNumber(roomNumber);
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.isAvailable = isAvailable;

    }

    public Room (String id,
                 int roomNumber,
                 String type,
                 double pricePerNight,
                 boolean isAvailable) {

        this(roomNumber, type, pricePerNight, isAvailable);
        this.id = id;

    }

    public void setRoomNumber(int roomNumber) {

        if (roomNumber < 0){

            throw new IllegalArgumentException("the room can't be 0");

        }

        this.roomNumber = roomNumber;

    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public void setId(String id) {
        this.id = id;
    }
}