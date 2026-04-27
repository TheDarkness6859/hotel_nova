package com.hotelNova.utils;

import org.mindrot.jbcrypt.BCrypt;

public class HashedPassword {

    private static final int SALT_ROUNDS = 12;

    public static String hashedPassword (String password){

        return BCrypt.hashpw(password, BCrypt.gensalt(SALT_ROUNDS));

    }

    public static boolean checkPassword (String password, String passwordHashed) {

        try {

            return BCrypt.checkpw(password, passwordHashed);

        }catch (Exception e) {

            LogManager.addLog("ERROR", "Error verifying password: " + e.getMessage());
            return false;

        }

    }

}
