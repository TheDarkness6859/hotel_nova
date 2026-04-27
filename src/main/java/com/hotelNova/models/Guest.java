package com.hotelNova.models;

public class Guest extends User{

    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    public Guest (String username,
                  String password,
                  String firstName,
                  String lastName,
                  String email,
                  String phone){

        super(username, password, "GUEST");

        setFirstName(firstName);
        setLastName(lastName);
        setEmail(email);
        this.phone= phone;

    }

    public Guest(String id,
                 String username,
                 String password,
                 String firstName,
                 String lastName,
                 String email,
                 String phone){

        super(id, username, password, "Guest");

        setFirstName(firstName);
        setLastName(lastName);
        setEmail(email);
        this.phone = phone;

    }

    private void validateNames (String value, String fieldName) {

        if (value == null || !value.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")){

            throw new IllegalArgumentException("Invalid " + fieldName + " : it can't have numbers");

        }

    }

    public void setFirstName(String firstName) {

        validateNames(firstName, "first name");
        this.firstName = firstName;

    }

    public void setLastName(String lastName) {

        validateNames(lastName, "last name");
        this.lastName = lastName;

    }

    public void setEmail(String email) {

        if (email == null || !email.matches("^[\\w.-]+@[a-zA-Z\\d.-]+\\.[a-zA-Z]{2,6}$")){

            throw new IllegalArgumentException("Invalid email format: Please enter a valid email address (e.g., user@example.com)");

        }

        this.email = email;

    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    private boolean active = true;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}