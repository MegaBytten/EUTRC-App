package org.megabytten.exetertouchapp.utils;

import org.json.JSONException;
import org.json.JSONObject;

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

    public Training(JSONObject jsonObject) throws Exception {
        /*
        This method should only be called after jsonHasTraining() static method to see if it can be instantiated.
        There is still an exception thrown to ensure safety, however it should only be called after checking hasTraining()
         */

        System.out.println("Creating new Training from jsonObject: " + jsonObject);

        String date_day = jsonObject.getString("date_day");
        if ( date_day.equalsIgnoreCase("none") ){
            throw new Exception("Cannot Parse a JSON Object that has date_day == 'none'!");
        }

        this.dateDay = Integer.parseInt( jsonObject.getString("date_day") );
        this.dateMonth = Integer.parseInt( jsonObject.getString("date_month") );
        this.dateYear = Integer.parseInt( jsonObject.getString("date_year") );
        this.attendance = Integer.parseInt( jsonObject.getString("date_year") );
        this.team = jsonObject.getString("team");
        this.location = jsonObject.getString("location");
        this.drills = jsonObject.getString("drills");
        this.time = jsonObject.getString("time");
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

    public static boolean jsonHasTraining(JSONObject serverTrainingResponse) throws JSONException {
        /*
        The server responds to the app's "get upcoming week's trainings" by sending a json object containing 3 json objects (HP, DV, CB)
        If there are NO upcoming trainings - the json objects = { "date_day" : "none" }
        Therefore it is useless to parse a json object into a training before we know if the server has sent us a training, or a noTraining
        This methods checks which has been sent, and returns a boolean: True if itHasTraining, or False if itHasNotTraining
         */

        String date_day = serverTrainingResponse.getString("date_day");
        if (date_day.equalsIgnoreCase("none")){
            //No training!
            return false;
        } else {
            //has Training!
            return true;
        }
    }
}
