package com.hotelNova.models;

import java.sql.*;
import java.time.temporal.ChronoUnit;

public class Reservation {

    private String id;
    private Date checkIn;
    private Date checkOut;
    private double totalCost;
    private String status;
    private Guest guest;
    private Room room;
    private User createdBy;

    public Reservation (Date checkIn,
                        Date checkOut,
                        String status,
                        Guest guest,
                        Room room,
                        User createdBy){

        setReservation(checkIn, checkOut);
        this.status = status;
        this.guest = guest;
        this.room = room;
        this.createdBy = createdBy;

    }

    public Reservation (String id,
                        Date checkIn,
                        Date checkOut,
                        double totalCost,
                        String status,
                        Guest guest,
                        Room room,
                        User createdBy ){

        this(checkIn, checkOut, status, guest, room, createdBy);
        this.id = id;
        this.totalCost = totalCost;

    }

    private void validateDate (Date check, String type){

        if (check == null){

            throw new IllegalArgumentException("The " + type +  " date can't be empty");

        }

        Date today = new Date(System.currentTimeMillis());
        if (check.before(today)){

            throw new IllegalArgumentException("The " + type + " date cannot be in the past.");

        }

    }

    private void setReservation (Date checkIn, Date checkOut){

        validateDate(checkIn, "check-in");
        validateDate(checkOut, "check-out");

        if (checkOut.after(checkIn)){

            this.checkIn = checkIn;
            this.checkOut = checkOut;

        }else {

            throw new IllegalArgumentException("Check-out date must be after check-in date");

        }

    }

    private int getNights () {

        return (int) ChronoUnit.DAYS.between(this.checkIn.toLocalDate(), this.checkOut.toLocalDate());

    }

    public double calculateTotalCost (double priceNight, double tax) {

        int nights = getNights();
        double subTotal = nights * priceNight;
        return subTotal + (subTotal * tax);

    }

    public Date getCheckOut() {
        return checkOut;
    }

    public Date getCheckIn() {
        return checkIn;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
}