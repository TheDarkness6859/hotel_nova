package com.hotelNova.services.impl;

import com.hotelNova.dao.impl.GenericDAOImpl;
import com.hotelNova.dao.impl.UserDAOImpl;
import com.hotelNova.db.DatabaseConnection;
import com.hotelNova.models.User;
import com.hotelNova.services.GenericService;
import com.hotelNova.services.UserService;
import com.hotelNova.utils.HashedPassword;
import com.hotelNova.utils.LogManager;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements UserService {

    private final GenericDAOImpl<User> userDAO = new UserDAOImpl();

    @Override
    public List<User> findAll () {

        return userDAO.findAll();

    }

    @Override
    public Optional<User> findByIdOrName (String value) {

        return userDAO.findByIdOrName(value);

    }

    @Override
    public boolean save (User user) {

        try {

            DatabaseConnection.startTransaction();

            boolean success = userDAO.save(user);

            if (success) {

                DatabaseConnection.commit();
                LogManager.addLog("INFO", "Commit created correctly in user service");
                return true;

            }else {

                DatabaseConnection.rollback();
                LogManager.addLog("WARNING", "Create rollback to save data");
                return false;

            }

        }catch (SQLException err){

            DatabaseConnection.rollback();
            LogManager.addLog("ERROR", "Error saving user: " + err.getMessage());
            return false;

        }

    }

    @Override
    public boolean edit(User user) {

        try {

            DatabaseConnection.startTransaction();

            boolean success = userDAO.edit(user);

            if (success) {

                DatabaseConnection.commit();
                LogManager.addLog("INFO", "User updated and commit executed correctly");
                return true;

            } else {

                DatabaseConnection.rollback();
                LogManager.addLog("WARNING", "User update failed, rollback executed");
                return false;

            }

        } catch (SQLException err) {

            DatabaseConnection.rollback();
            LogManager.addLog("ERROR", "Error updating user: " + err.getMessage());
            return false;

        }

    }

    @Override
    public boolean delete (String id) {

        try {

            DatabaseConnection.startTransaction();

            if (userDAO.delete(id)) {

                DatabaseConnection.commit();
                LogManager.addLog("INFO", "Commit deleted correctly in user service");
                return true;

            }else {

                DatabaseConnection.rollback();
                LogManager.addLog("WARNING", "deleting rollback to save data");
                return false;

            }

        } catch (SQLException err) {

            DatabaseConnection.rollback();
            LogManager.addLog("ERROR", "Error deleting user: " + err.getMessage());
            return false;

        }

    }

    @Override
    public Optional<User> login(String username, String password){

        return userDAO.findByIdOrName(username)
                .filter(user -> HashedPassword.checkPassword(password, user.getPassword())
        );

    }

}
