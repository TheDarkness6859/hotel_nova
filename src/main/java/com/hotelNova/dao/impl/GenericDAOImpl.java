package com.hotelNova.dao.impl;

import com.hotelNova.dao.GenericDAO;
import com.hotelNova.db.DatabaseConnection;
import com.hotelNova.utils.LogManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class GenericDAOImpl<T> implements GenericDAO<T> {

    protected final DatabaseConnection dc = DatabaseConnection.getInstance();

    //Query return:
    protected abstract T mapRow(ResultSet rs) throws SQLException;

    //Get query:
    protected abstract String getSaveQuery ();
    protected abstract String getEditQuery();
    protected abstract String getFindQuery ();
    protected abstract String getFindByIdOrName();
    protected abstract String getDeleteQuery ();

    //Set Query:
    protected abstract void setSaveParams(PreparedStatement ps , T object) throws SQLException;
    protected abstract void setEditParams(PreparedStatement ps, T object) throws SQLException;
    protected abstract void setDeleteParams(PreparedStatement ps, String id) throws SQLException;
    protected abstract void setFindByParams(PreparedStatement ps, String value) throws SQLException;

    @Override
    public Optional<T> findByIdOrName (String value) {

        try{

            Connection conn = dc.getConnection();

            try(PreparedStatement ps = conn.prepareStatement(getFindByIdOrName())){

                setFindByParams(ps, value);

                try(ResultSet rs = ps.executeQuery()){

                    if (rs.next()){

                        LogManager.addLog("INFO", "User " + value + " found correctly");
                        return Optional.of(mapRow(rs));

                    }else {

                        LogManager.addLog("WARNING", "User " + value + " don´t found in database");
                        return Optional.empty();

                    }

                }

            }

        }catch (SQLException err){

            LogManager.addLog("ERROR", "Failed to execute findByIdOrName:" + err.getMessage());
            throw new RuntimeException("Database error: " + err);

        }

    }

    @Override
    public List<T> findAll () {

        List<T> users = new ArrayList<>();

        try{

            Connection conn = dc.getConnection();

            try(PreparedStatement ps = conn.prepareStatement(getFindQuery());
                ResultSet rs = ps.executeQuery()){

                while (rs.next()){

                    users.add(mapRow(rs));

                }

                if (users.isEmpty()) {

                    LogManager.addLog("INFO", "getAll executed: No records found in database.");

                } else {

                    LogManager.addLog("INFO", "getAll executed: " + users.size() + " records retrieved.");

                }

            }

        }catch (SQLException err){

            LogManager.addLog("ERROR", "failed to execute getAll" + err.getMessage());
            throw new RuntimeException("Database error", err);

        }

        return users;

    }

    @Override
    public boolean delete (String id) {

        try{

            Connection conn = dc.getConnection();

            try(PreparedStatement ps = conn.prepareStatement(getDeleteQuery())){

                setDeleteParams(ps, id);

                int rows = ps.executeUpdate();

                if (rows > 0){

                    LogManager.addLog("INFO", "User" + id + "Delete correctly");
                    return true;

                }else {

                    LogManager.addLog("WARNING", "The user cannot be delete" + id);
                    return false;

                }

            }

        }catch (SQLException err) {

            LogManager.addLog("ERROR", "Failed to execute delete: " + err.getMessage());
            throw new RuntimeException("Database error", err);

        }

    }

    @Override
    public boolean edit (T object){

        try {

            Connection conn = dc.getConnection();

            try(PreparedStatement ps = conn.prepareStatement(getEditQuery())){

                setEditParams(ps, object);

                int rows = ps.executeUpdate();

                if (rows > 0){

                    LogManager.addLog("INFO", "user updated correctly");
                    return true;

                }else {

                    LogManager.addLog("WARNING", "The user cannot be updated. Check if the ID exists.");
                    return false;

                }

            }

        }catch (SQLException err) {

            LogManager.addLog("ERROR", "failed to update: " + err.getMessage());
            throw new RuntimeException("Database error", err);

        }

    }

}
