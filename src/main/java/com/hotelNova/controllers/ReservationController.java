package com.hotelNova.controllers;

import com.hotelNova.models.Reservation;
import com.hotelNova.services.GenericService;
import com.hotelNova.services.impl.ReservationServiceImpl;
import com.hotelNova.utils.LogManager;
import java.util.List;

public class ReservationController {

    private final GenericService<Reservation> reservationService = new ReservationServiceImpl();

    public boolean createReservation(Reservation reservation) {

        if (reservation.getCheckOut().before(reservation.getCheckIn())) {
            LogManager.addLog("WARNING", "Check-out date cannot be before check-in");
            return false;
        }

        return reservationService.save(reservation);

    }

    public List<Reservation> getAllReservations() {

        return reservationService.findAll();

    }

    public boolean cancelReservation(String id) {

        return reservationService.delete(id);

    }
}