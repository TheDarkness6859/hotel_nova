package com.hotelNova.dao.impl;

import com.hotelNova.models.Guest;
import com.hotelNova.models.Reservation;
import com.hotelNova.models.Room;
import com.hotelNova.models.User;
import com.hotelNova.utils.LogManager;

import java.sql.*;
import java.util.Optional;

public class ReservationDAOImpl extends GenericDAOImpl <Reservation> {

    //Instance DAO:
    private final GenericDAOImpl<User> userDAO = new UserDAOImpl();
    private final GenericDAOImpl<Room> roomDAO = new RoomDAOImpl();

    //Queries:
    protected static final String SAVE = "INSERT INTO hotelnova.reservations (check_in, check_out, total_cost, status, guest_id, room_id, created_by) VALUES (?, ?, ?, ?, ?, ?, ?)";

    protected static final String EDIT = "UPDATE hotelnova.reservations SET check_in = ?, check_out = ?, total_cost = ?, status = ? WHERE id = ?";

    protected static final String FIND_BY = "SELECT * FROM hotelnova.reservations WHERE id = ?";

    protected static final String FIND_ALL = "SELECT * FROM hotelnova.reservations";

    protected static final String DELETE = "DELETE FROM hotelnova.reservations WHERE id = ?";


    @Override
    protected Reservation mapRow (ResultSet rs) throws SQLException {

        String id = rs.getString("id");
        Date checkIn = rs.getDate("check_in");
        Date checkOut = rs.getDate("check_out");
        double totalCost = rs.getDouble("total_cost");
        String status = rs.getString("status");

        String guestId = rs.getString("guest_id");
        String roomId = rs.getString("room_id");
        String createdById = rs.getString("created_by");

        Optional<Guest> guest = userDAO.findByIdOrName(guestId)
                .filter(user -> user instanceof Guest)
                .map(user -> (Guest) user)
        ;

        Optional<Room> room = roomDAO.findByIdOrName(roomId);

        Optional<User> creatorObj = userDAO.findByIdOrName(createdById);

        return new Reservation(
                id,
                checkIn,
                checkOut,
                totalCost,
                status,
                guest.orElseThrow(() -> new IllegalArgumentException("Guest not found with ID: " + guestId)),
                room.orElseThrow(() -> new IllegalArgumentException("Room not found with ID: " + roomId)),
                creatorObj.orElseThrow(() -> new IllegalArgumentException("Creator user not found with ID: " + createdById))
        );

    }

    @Override
    public boolean save (Reservation res){

        try {

            Connection conn = dc.getConnection();

            try(PreparedStatement ps = conn.prepareStatement(SAVE, Statement.RETURN_GENERATED_KEYS)){

                setSaveParams(ps, res);
                int rows = ps.executeUpdate();

                if (rows > 0) {

                    try (ResultSet rs = ps.getGeneratedKeys()) {

                        if (rs.next()) {

                            LogManager.addLog("INFO", "Reservation save correctly in database");
                            res.setId(rs.getString(1));
                            return true;

                        }

                    }

                }

                LogManager.addLog("WARNING", "Reservation cannot be saved in database");
                return false;

            }

        }catch (SQLException err) {

            LogManager.addLog("ERROR", "failed to execute save" + err.getMessage());
            return false;

        }

    }

    @Override
    protected void setEditParams (PreparedStatement ps, Reservation res) throws SQLException {

        ps.setDate(1, res.getCheckIn());
        ps.setDate(2, res.getCheckOut());
        ps.setDouble(3, res.getTotalCost());
        ps.setString(4, res.getStatus());
        ps.setObject(5, java.util.UUID.fromString(res.getId()));

    }

    @Override
    public void setSaveParams (PreparedStatement ps, Reservation res) throws SQLException {

        ps.setDate(1, res.getCheckIn());
        ps.setDate(2, res.getCheckOut());
        ps.setDouble(3, res.getTotalCost());
        ps.setString(4, res.getStatus());

        ps.setObject(5, java.util.UUID.fromString(res.getGuest().getId()));
        ps.setObject(6, java.util.UUID.fromString(res.getRoom().getId()));
        ps.setObject(7, java.util.UUID.fromString(res.getCreatedBy().getId()));

    }

    @Override
    protected void setDeleteParams(PreparedStatement ps, String id) throws SQLException {

        ps.setObject(1, java.util.UUID.fromString(id));

    }

    @Override
    protected void setFindByParams(PreparedStatement ps, String value) throws SQLException {

        ps.setObject(1, java.util.UUID.fromString(value));

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
