package com.hotelNova.dao.impl;

import com.hotelNova.models.Admin;
import com.hotelNova.models.Guest;
import com.hotelNova.models.Receptionist;
import com.hotelNova.models.User;
import com.hotelNova.utils.LogManager;

import java.sql.*;
import java.util.UUID;

public class UserDAOImpl extends  GenericDAOImpl<User> {

    protected static final String SAVE = "INSERT INTO hotelnova.users (username, password, role) values (?, ?, ?)";
    protected static final String EDIT = "UPDATE hotelnova.users set username = ?, password = ?, role = ? WHERE id = ?";

    protected static final String FIND_BY =
            "SELECT u.*, g.first_name, g.last_name, g.email, g.phone, g.is_active " +
                    "FROM hotelnova.users u " +
                    "LEFT JOIN hotelnova.guests g ON u.id = g.id " +
                    "WHERE u.username = ? OR u.id = ?";

    protected static final String FIND_ALL =
            "SELECT u.*, g.first_name, g.last_name, g.email, g.phone, g.is_active " +
                    "FROM hotelnova.users u " +
                    "LEFT JOIN hotelnova.guests g ON u.id = g.id";

    protected static final String DELETE = "DELETE FROM hotelnova.users WHERE id = ?";

    @Override
    protected User mapRow(ResultSet rs) throws SQLException {

        String id = rs.getNString("id");
        String role = rs.getString("role");
        String username = rs.getString("username");
        String password = rs.getString("password");

        if ("ADMIN".equalsIgnoreCase(role)) {

            return new Admin(id, username, password);

        } else if ("RECEPTIONIST".equalsIgnoreCase(role)) {

            return new Receptionist(id, username, password);

        } else {

            return new Guest(id,
                    username,
                    password,
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("phone")
            );

        }

    }

    @Override
    public boolean save(User user) {

        try {

            Connection conn = dc.getConnection();

            try (PreparedStatement ps = conn.prepareStatement(SAVE, Statement.RETURN_GENERATED_KEYS)) {

                setSaveParams(ps, user);

                try (ResultSet rs = ps.getGeneratedKeys()) {

                    if (rs.next()) {

                        LogManager.addLog("INFO", "User save correctly in database");
                        user.setId(rs.getString(1));
                        return true;


                    } else {

                        LogManager.addLog("WARNING", "User cannot be saved in database");
                        return false;

                    }

                }

            }

        } catch (SQLException err) {

            LogManager.addLog("ERROR", "failed to execute save" + err.getMessage());
            throw new RuntimeException("Database error", err);

        }

    }

    @Override
    protected void setSaveParams(PreparedStatement ps, User object) throws SQLException {
        ps.setString(1, object.getUsername());
        ps.setString(2, object.getPassword());
        ps.setString(3, object.getRole());
    }

    @Override
    protected void setEditParams(PreparedStatement ps, User object) throws SQLException {
        ps.setString(1, object.getUsername());
        ps.setString(2, object.getPassword());
        ps.setString(3, object.getRole());
        ps.setString(4, object.getId());

    }

    @Override
    protected void setDeleteParams(PreparedStatement ps, String id) throws SQLException {
        ps.setString(1, id);
    }

    @Override
    protected void setFindByParams(PreparedStatement ps, String value) throws SQLException {
        ps.setString(1, value);

        try {
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