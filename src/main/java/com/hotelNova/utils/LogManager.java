package com.hotelNova.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

public class LogManager {

    private final static String path = "app.log";

    public static synchronized void  addLog (String level, String message){

        try(BufferedWriter bw = new BufferedWriter(new FileWriter(path, true))){

            String time = LocalDate.now().toString();

            bw.write("[" + time + "] [" + level + "]" + message);
            bw.newLine();

        }catch (IOException err){

            System.err.println("Sorry cannot write logs" + err.getMessage());

        }

    }

}
