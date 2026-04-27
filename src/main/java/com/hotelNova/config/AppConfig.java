package com.hotelNova.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {

    private static AppConfig instance;

    private final Properties appProps = new Properties();

    private AppConfig () {

        load("app.properties", appProps);

    }

    public static AppConfig getInstance() {

        if (instance == null){

            synchronized (AppConfig.class){

                if (instance == null) instance = new AppConfig();

            }

        }

        return instance;

    }


    private void load (String name, Properties prop) {

        try(InputStream is = getClass().getClassLoader().getResourceAsStream(name)){

            if (is == null) throw new RuntimeException("Don't file found: " + name);

            prop.load(is);

        }catch (IOException err){

            throw new RuntimeException("Error to connect with" + name, err);

        }

    }

    public String getUrl () {

        return appProps.getProperty("db.url");

    }

    public String getUser () {

        return appProps.getProperty("db.user");

    }

    public String getPass () {

        return appProps.getProperty("db.pass");

    }

    public String getDriver () {

        return appProps.getProperty("db.driver");

    }

    public int getCheckIn () {

        String value = appProps.getProperty("horaCheckIn");
        return Integer.parseInt(value);

    }

    public int getCheckOut () {

        String value = appProps.getProperty("horaCheckOut");
        return Integer.parseInt(value);

    }

    public double getIva () {

        String value = appProps.getProperty("iva");
        return Double.parseDouble(value);

    }


}
