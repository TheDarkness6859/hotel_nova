package com.hotelNova.dao.impl;

import com.hotelNova.models.Reservation;
import com.hotelNova.models.Room;
import com.hotelNova.utils.LogManager;

import java.sql.*;
import java.util.UUID;

public class RoomDAOImpl extends GenericDAOImpl<Room>{

    protected static final String SAVE = "INSERT INTO hotelnova.rooms (room_number, type, price_per_night, is_available) VALUES (?, ?, ?, ?)";

    protected static final String EDIT = "UPDATE hotelnova.rooms SET room_number = ?, type = ?, price_per_night = ?, is_available = ? WHERE id = ?";

    protected static final String FIND_BY = "SELECT * FROM hotelnova.rooms WHERE room_number = ? OR id = ?";

    protected static final String FIND_ALL = "SELECT * FROM hotelnova.rooms";

    protected static final String DELETE = "DELETE FROM hotelnova.rooms WHERE id = ?";

    @Override
    public Room mapRow (ResultSet rs) throws SQLException{

        String id = rs.getString("id");
        int roomNumber = rs.getInt("room_number");
        String type = rs.getString("type");
        double pricePerNight = rs.getDouble("price_per_night");
        boolean isAvailable = rs.getBoolean("is_available");

        return new Room(id,
                roomNumber,
                type,
                pricePerNight,
                isAvailable)
        ;

    }

    @Override
    public boolean save (Room room){

        try {

            Connection conn = dc.getConnection();

            try(PreparedStatement ps = conn.prepareStatement(SAVE, Statement.RETURN_GENERATED_KEYS)){

                setSaveParams(ps, room);
                int rows = ps.executeUpdate();

                if (rows > 0) {

                    try (ResultSet rs = ps.getGeneratedKeys()) {

                        if (rs.next()) {

                            LogManager.addLog("INFO", "Room save correctly in database");
                            room.setId(rs.getString(1));
                            return true;

                        }

                    }

                }

                LogManager.addLog("WARNING", "Room cannot be saved in database");
                return false;

            }

        }catch (SQLException err) {

            LogManager.addLog("ERROR", "failed to execute save" + err.getMessage());
            return false;

        }

    }

    @Override
    protected void setDeleteParams(PreparedStatement ps, String id) throws SQLException {

        ps.setObject(1, UUID.fromString(id));

    }

    @Override
    protected void setSaveParams (PreparedStatement ps, Room object) throws SQLException{

        ps.setInt(1, object.getRoomNumber());
        ps.setString(2, object.getType());
        ps.setDouble(3, object.getPricePerNight());
        ps.setBoolean(4, object.isAvailable());

    }

    @Override
    protected void setEditParams (PreparedStatement ps, Room object) throws SQLException {

        ps.setInt(1, object.getRoomNumber());
        ps.setString(2, object.getType());
        ps.setDouble(3, object.getPricePerNight());
        ps.setBoolean(4, object.isAvailable());
        ps.setObject(5, UUID.fromString(object.getId()));

    }

    @Override
    protected void setFindByParams (PreparedStatement ps, String value) throws SQLException {

        ps.setString(1, value);

        try{

            ps.setObject(2, UUID.fromString(value));

        }catch (IllegalArgumentException err){

            ps.setObject(2, null);

        }

    }

    @Override
    protected String getSaveQuery() {
        return SAVE;
    }

    @Override
    protected String getEditQuery() {
        return EDIT;
    }

    @Override
    protected String getFindQuery() {
        return FIND_ALL;
    }

    @Override
    protected String getFindByIdOrName() {
        return FIND_BY;
    }

    @Override
    protected String getDeleteQuery() {
        return DELETE;
    }



}
