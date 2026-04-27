package com.hotelNova.services.impl;

import com.hotelNova.dao.impl.GenericDAOImpl;
import com.hotelNova.dao.impl.ReservationDAOImpl;
import com.hotelNova.dao.impl.RoomDAOImpl;
import com.hotelNova.db.DatabaseConnection;
import com.hotelNova.models.Reservation;
import com.hotelNova.models.Room;
import com.hotelNova.services.GenericService;
import com.hotelNova.utils.LogManager;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ReservationServiceImpl implements GenericService<Reservation> {

    private final GenericDAOImpl<Reservation> reservationDAO = new ReservationDAOImpl();
    private final GenericDAOImpl<Room> roomDAO = new RoomDAOImpl();

    @Override
    public List<Reservation> findAll() {

        return reservationDAO.findAll();

    }

    @Override
    public Optional<Reservation> findByIdOrName(String value) {

        return reservationDAO.findByIdOrName(value);

    }

    @Override
    public boolean save(Reservation res) {
        try {

            DatabaseConnection.startTransaction();

            boolean resSaved = reservationDAO.save(res);

            Room room = res.getRoom();
            room.setAvailable(false);
            boolean roomUpdated = roomDAO.edit(room);

            if (resSaved && roomUpdated) {

                DatabaseConnection.commit();
                LogManager.addLog("INFO", "Commit created correctly in reservation service");
                return true;

            } else {

                DatabaseConnection.rollback();
                LogManager.addLog("WARNING", "Create rollback to save reservation data");
                return false;

            }

        } catch (SQLException err) {

            DatabaseConnection.rollback();
            LogManager.addLog("ERROR", "Error saving reservation: " + err.getMessage());
            return false;

        }
    }

    @Override
    public boolean edit(Reservation res) {

        try {

            DatabaseConnection.startTransaction();

            if (reservationDAO.edit(res)) {

                DatabaseConnection.commit();
                LogManager.addLog("INFO", "Reservation updated and commit executed correctly");
                return true;

            } else {

                DatabaseConnection.rollback();
                LogManager.addLog("WARNING", "Reservation update failed, rollback executed");
                return false;

            }

        } catch (SQLException err) {

            DatabaseConnection.rollback();
            LogManager.addLog("ERROR", "Error updating reservation: " + err.getMessage());
            return false;

        }
    }

    @Override
    public boolean delete(String id) {
        try {

            DatabaseConnection.startTransaction();

            if (reservationDAO.delete(id)) {

                DatabaseConnection.commit();
                LogManager.addLog("INFO", "Reservation deleted and commit executed correctly");
                return true;

            } else {

                DatabaseConnection.rollback();
                LogManager.addLog("WARNING", "Reservation delete failed, rollback executed");
                return false;

            }

        } catch (SQLException err) {

            DatabaseConnection.rollback();
            LogManager.addLog("ERROR", "Error deleting reservation: " + err.getMessage());
            return false;

        }
    }

}