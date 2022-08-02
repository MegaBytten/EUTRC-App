package org.megabytten.exetertouchapp;

public class Training {

    private int dateDay, dateMonth, dateYear, attendance;
    private String team, location, drills, time;


    public Training(int dateDay, int dateMonth, int dateYear, String team, String time) {
        this.dateDay = dateDay;
        this.dateMonth = dateMonth;
        this.dateYear = dateYear;
        this.team = team;
        this.time = time;
    }

    public int getDateDay() {
        return dateDay;
    }

    public void setDateDay(int dateDay) {
        this.dateDay = dateDay;
    }

    public int getDateMonth() {
        return dateMonth;
    }

    public void setDateMonth(int dateMonth) {
        this.dateMonth = dateMonth;
    }

    public int getDateYear() {
        return dateYear;
    }

    public void setDateYear(int dateYear) {
        this.dateYear = dateYear;
    }

    public int getAttendance() {
        return attendance;
    }

    public void setAttendance(int attendance) {
        this.attendance = attendance;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDrills() {
        return drills;
    }

    public void setDrills(String drills) {
        this.drills = drills;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFormattedDate(){
        String formattedDate = dateDay + "/" + dateMonth + "/" + dateYear;
        return formattedDate;
    }
}
