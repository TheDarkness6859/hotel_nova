package com.hotelNova.models;

public abstract class User {

    private String id;
    private String username;
    private String password;
    private String role;

    public User (String username, String password, String role){

        setUsername(username);
        this.password = password;
        setRole(role);

    }

    public User (String id, String username, String password, String role) {

        this(username, password, role);
        setId(id);

    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public void setUsername(String username) {

        if (username == null || !username.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")){

            throw new IllegalArgumentException("Invalid name: the name can't have numbers");

        }

        this.username = username;

    }

    public void setRole(String role) {

        if (role == null || !(role.equals("ADMIN") || role.equals("RECEPTIONIST") || role.equals("GUEST"))) {
            throw new IllegalArgumentException("Invalid role: Must be ADMIN, RECEPTIONIST, or GUEST.");
        }

        this.role = role;
    }

    public void setId (String id){

        this.id = id;

    }

}