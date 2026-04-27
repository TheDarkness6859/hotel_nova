package com.hotelNova.services.impl;

import com.hotelNova.dao.impl.GenericDAOImpl;
import com.hotelNova.dao.impl.RoomDAOImpl;
import com.hotelNova.db.DatabaseConnection;
import com.hotelNova.models.Room;
import com.hotelNova.services.GenericService;
import com.hotelNova.utils.LogManager;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class RoomServiceImpl implements GenericService<Room> {

    private final GenericDAOImpl<Room> roomDAO = new RoomDAOImpl();

    @Override
    public List<Room> findAll() {

        return roomDAO.findAll();

    }

    @Override
    public Optional<Room> findByIdOrName(String value) {

        return roomDAO.findByIdOrName(value);

    }

    @Override
    public boolean save(Room room) {

        try {

            DatabaseConnection.startTransaction();

            boolean success = roomDAO.save(room);

            if (success) {

                DatabaseConnection.commit();
                LogManager.addLog("INFO", "Commit created correctly in room service");
                return true;

            }else {

                DatabaseConnection.rollback();
                LogManager.addLog("WARNING", "Create rollback to save data");
                return false;

            }



        } catch (SQLException err) {

            DatabaseConnection.rollback();
            LogManager.addLog("ERROR", "Error saving room: " + err.getMessage());
            return false;

        }

    }

    @Override
    public boolean edit(Room room) {

        try {

            DatabaseConnection.startTransaction();

            if (roomDAO.edit(room)) {

                DatabaseConnection.commit();
                LogManager.addLog("INFO", "Commit updated correctly in room service");
                return true;

            }else {

                DatabaseConnection.rollback();
                LogManager.addLog("WARNING", "updated rollback to save data");
                return false;

            }


        } catch (SQLException err) {

            DatabaseConnection.rollback();
            LogManager.addLog("ERROR", "Error editing room: " + err.getMessage());
            return false;

        }

    }

    @Override
    public boolean delete(String id) {

        try {

            DatabaseConnection.startTransaction();

            if (roomDAO.delete(id)) {

                DatabaseConnection.commit();
                LogManager.addLog("INFO", "Commit deleted correctly in room service");
                return true;

            }else {

                DatabaseConnection.rollback();
                LogManager.addLog("WARNING", "deleting rollback to save data");
                return false;

            }



        } catch (SQLException err) {

            DatabaseConnection.rollback();
            LogManager.addLog("ERROR", "Error deleting room: " + err.getMessage());
            return false;

        }
    }

}