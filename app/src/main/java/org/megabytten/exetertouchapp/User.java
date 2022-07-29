package org.megabytten.exetertouchapp;

public class User {

    private static User instance;
    private String email, firstName, lastName, phoneNumber, password;

    public User(String email, String firstName, String lastName, String phoneNumber, String password) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.password = password;
    }

    public static synchronized void createInstance(String email, String firstName, String lastName, String phoneNumber, String password) {
        instance = new User(email, firstName, lastName, phoneNumber, password);
        System.out.println("User created successfully!");
    }

    public static synchronized User getInstance(){
        if (null == instance) {
            System.out.println("Error: User instance has not been initialized!");
            return null;
        }
        return instance;
    }



    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }



}
