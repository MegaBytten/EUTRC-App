package org.megabytten.exetertouchapp.utils;

public class User {

    private static User instance;
    private String email, firstName, lastName, phoneNumber, password;
    private boolean isCoach;
    private int icon;

    public User(String email, String firstName, String lastName, String phoneNumber, String password, boolean coach, int icon) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.isCoach = coach;
        this.icon = icon;
    }

    public static synchronized void createInstance(String email, String firstName, String lastName, String phoneNumber, String password, boolean coach, int icon) {
        instance = new User(email, firstName, lastName, phoneNumber, password, coach, icon);
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

    public boolean isCoach() {
        return isCoach;
    }

    public void setCoach(boolean isCoach) {
        this.isCoach = isCoach;
    }

    /*
    Note: Icon images as found from: https://www.istockphoto.com/illustrations/touch-rugby
     */

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

}
