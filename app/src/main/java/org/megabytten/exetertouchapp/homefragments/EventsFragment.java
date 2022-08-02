package org.megabytten.exetertouchapp.homefragments;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.megabytten.exetertouchapp.CustomTextView;
import org.megabytten.exetertouchapp.R;
import org.megabytten.exetertouchapp.Training;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;

public class EventsFragment extends Fragment {
    private static EventsFragment eventsFragment;

    View view;
    boolean isCalendarInit = false;
    ArrayList<LinearLayout> calendarRowLinLays;
    ArrayList<CustomTextView> calendarCells;
    LocalDate calendarDate;

    //calendar cells views for ViewTrainings updates
    TextView trainingsInfoTitle;
    TextView trainingsInfo;
    Button closeBtn;
    int previousButtonId;


    public EventsFragment() {
        // Required empty public constructor
    }

    public static EventsFragment getInstance(){
        if (eventsFragment == null){
            System.out.println("Initialising eventsFragment.");
            eventsFragment = new EventsFragment();
        }
        return eventsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_events, container, false);

        getActivity().runOnUiThread( () -> {
            if (!isCalendarInit) calendarLinCellInit();
        });

        view.findViewById(R.id.nextMonthBtn).setOnClickListener(v -> {
            getActivity().runOnUiThread( () -> {
                onNextMonth();
            });
        });

        view.findViewById(R.id.lastMonthBtn).setOnClickListener(v -> {
            getActivity().runOnUiThread( () -> {
                onLastMonth();
            });
        });

        trainingsInfoTitle = view.findViewById(R.id.trainingInfoTitle);
        trainingsInfoTitle.setPaintFlags(trainingsInfoTitle.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        trainingsInfo = view.findViewById(R.id.trainingsInfo);
        trainingsInfo.setVisibility(View.INVISIBLE);
        trainingsInfo.setTextColor(Color.BLACK);

        closeBtn = view.findViewById(R.id.closeBtn);
        closeBtn.setVisibility(View.INVISIBLE);
        closeBtn.setOnClickListener(v ->
            getActivity().runOnUiThread(()->{
                onCloseBtn();
            })
        );

        return view;
    }


    /*
    ################################################################################################
    ################################################################################################
    ############################### Calendar Init/Updating methods #################################
    ################################################################################################
    ################################################################################################
     */

    private void calendarLinCellInit(){
        calendarRowLinLays = new ArrayList<>();
        calendarCells = new ArrayList<>();

        int numWeekRows = 6;

        //This loop creates 6 linear layouts (rows) - one for each week of the month
        //all these lin layouts are then stored in calendarRowLinLays
        for (int i = 0; i < numWeekRows; i++){
            LinearLayout linLay = new LinearLayout(getContext());
            linLay.setOrientation(LinearLayout.HORIZONTAL);
            linLay.setLayoutParams(new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            calendarRowLinLays.add(linLay);

            //adds the row linLays one by one to the overarching calendar linLay
            LinearLayout calendarHolder = view.findViewById(R.id.calendar);
            calendarHolder.addView(linLay);
        }

        //util function - Pixel to DP for height/width setting
        final float scale = getContext().getResources().getDisplayMetrics().density;
        int pixels = (int) (50 * scale + 0.5f);

        //Each linLay (row) for the calendar requires 7 TextViews - one for each day of the week
        //function runs throw the rows - creating 7 new TextViews, setting the text and saving it to the linLay
        for (int y = 0; y < numWeekRows; y++){
            for (int x = 0; x < 7; x++){
                CustomTextView tv = new CustomTextView(getContext());
                tv.setText("W:" + String.valueOf(y+1) + ", D:" + String.valueOf(x+1));
                tv.setWidth(pixels);
                tv.setHeight(pixels);
                tv.setBackgroundColor(Color.WHITE);
                tv.setTextSize(12);
                tv.setTextColor(Color.BLACK);
                tv.setTextAlignment(LinearLayout.TEXT_ALIGNMENT_CENTER);
                tv.setPadding(2,2,2,2);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(10,20,10,10);
                params.weight = 1f;
                tv.setLayoutParams(params);

                //onclick listener for every TextView
                tv.setOnClickListener(v -> {
                    calendarCellOnClick(v);
                });

                calendarRowLinLays.get(y).addView(tv);
                calendarCells.add(tv);
            }
        }

        isCalendarInit = true;
        //Calendar Rows/Cells set up completed
        //now need to programmatically add visible numbers to each training
        System.out.println("EventsFragment: Calendar initialised!");

        //set correct month
        calendarDate = LocalDate.now();
        updateCalendar();

    }

    private void calendarCellOnClick(View v){
        CustomTextView selectedCell = null;
        if (previousButtonId != 0){
            for (CustomTextView ctv: calendarCells){
                if (ctv.getId() == previousButtonId){
                    ctv.setTextColor(Color.BLACK);
                    break;
                }
            }
        }

        for (CustomTextView ctv: calendarCells){
            if (ctv.getId() == v.getId()){
                selectedCell = ctv;
                previousButtonId = v.getId();
                break;
            }
        }

        closeBtn.setVisibility(View.VISIBLE);
        if (selectedCell == null){
            System.out.println("Unable to find selected cell.");
        } else {
            selectedCell.setTextColor(Color.RED);
            updateTextView(selectedCell.getTrainings(), trainingsInfo);
        }
    }

    private void updateCalendar(){
        TextView monthTitleTxt = view.findViewById(R.id.monthTitleTxt);

        if (calendarDate.getMonth().toString().equalsIgnoreCase(LocalDate.now().getMonth().toString())){
            monthTitleTxt.setTextColor(Color.RED);
        } else {
            monthTitleTxt.setTextColor(Color.BLACK);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(calendarDate.getMonth().toString());
        sb.append(" ");
        sb.append(calendarDate.getYear());
        monthTitleTxt.setText(sb.toString());

        //find weekday
        YearMonth month = YearMonth.from(calendarDate);
        LocalDate monthStart = month.atDay(1);
        LocalDate monthEnd = month.atEndOfMonth();

        //get index of weekday
        int weekdayIndex = getDayIndex(monthStart);

        //cycle through all 35 calendar cells
        int day = 1;
        for (int i = 0; i < calendarCells.size(); i++){

            //sets first row
            if (i+1 < weekdayIndex){
                //if the cell is before the beginning of the month
                calendarCells.get(i).setVisibility(View.INVISIBLE);
            } else if (day > monthEnd.getDayOfMonth()){
                //if the cell is after the end of the month
                calendarCells.get(i).setVisibility(View.INVISIBLE);
            } else {
                calendarCells.get(i).setVisibility(View.VISIBLE);
                calendarCells.get(i).setText(String.valueOf(day) + "\n");
                calendarCells.get(i).setId(day);
                day++;
            }
        }

        //pull JSON training data
        Thread updaterThread = new Thread( () -> {
            try {
                pullCalendarEvents();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        });
        updaterThread.start();


    }

    private int getDayIndex(LocalDate date) {
        switch (date.getDayOfWeek()) {
            case MONDAY:
                return 2;
            case TUESDAY:
                return 3;
            case WEDNESDAY:
                return 4;
            case THURSDAY:
                return 5;
            case FRIDAY:
                return 6;
            case SATURDAY:
                return 7;
            case SUNDAY:
                return 1;
            default:
                throw new IllegalArgumentException("The dayOfWeek (" + date.getDayOfWeek() + ") is not valid.");
        }
    }

     /*
    ################################################################################################
    ############################# Calendar TRAINING updater methods ################################
    ###################### Methods that are used to initialise or update ###########################
    ######################## the Training info, below the main calendar ############################
    ################################################################################################
     */

    private void pullCalendarEvents() throws IOException, JSONException {
        String month = calendarDate.getMonth().toString();
        String year = String.valueOf(calendarDate.getYear());

        URL url = new URL("http://megabytten.org/eutrcapp/trainings.json");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(10000);
        con.setRequestProperty("month", month);
        con.setRequestProperty("year", year);

        int responseCode = con.getResponseCode();
        System.out.println("POST Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));
            StringBuilder sb = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }

            //null check for response - if there are no trainings this month
            if (sb.toString().equalsIgnoreCase("null")){
                //return is null, no trainings for the month.
                System.out.println("Trainings this month.json = null");
                return;
            }

            JSONArray obj = new JSONArray(sb.toString());

            ArrayList<Training> trainings = new ArrayList<>();
            for (int i = 0; i < obj.length(); i++) {
                JSONObject trainingJSON = obj.getJSONObject(i);
                Training training = new Training(
                        Integer.parseInt( trainingJSON.getString("date_day") ),
                        Integer.parseInt(  trainingJSON.getString("date_month") ),
                        Integer.parseInt(  trainingJSON.getString("date_year") ),
                        trainingJSON.getString("team"),
                        trainingJSON.getString("time")
                );

                //null checks for nullable attributes
                if (!(trainingJSON.getString("location").equalsIgnoreCase("null"))){
                    training.setLocation( trainingJSON.getString("location") );
                }
                if (!(trainingJSON.getString("drills").equalsIgnoreCase("null"))){
                    training.setDrills( trainingJSON.getString("drills") );
                }
                if (!(trainingJSON.getString("attendance").equalsIgnoreCase("null"))){
                    int attendance = Integer.parseInt( trainingJSON.getString("attendance") );
                    training.setAttendance(attendance);
                }

                trainings.add(training);
            }

            getActivity().runOnUiThread(() -> {
                try {
                    updateCalendarEvents(obj, trainings);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });


        }
    }

    private void updateCalendarEvents(JSONArray obj, ArrayList<Training> trainings) throws JSONException {

        for (Training training : trainings){
            //this section obtains the int dateDay, and parses it into a string so that it may be formatted
            int trainingDay = training.getDateDay();
            String trainingDayStr = String.valueOf(trainingDay);
            StringBuilder editString = new StringBuilder(trainingDayStr);
            if (editString.charAt(0) == '0'){
                editString.deleteCharAt(0);
            }

            int formattedDateId = Integer.parseInt(editString.toString());
            for (CustomTextView tv: calendarCells){
                if (tv.getId() == formattedDateId){
                    //updates Display String to show training
                    String currentText = tv.getText().toString();
                    StringBuilder newText = new StringBuilder(currentText);
                    newText.append(training.getTeam().toUpperCase());
                    newText.append("\n");
                    tv.setText(newText.toString());

                    //updates CustomTextView by adding training to it
                    tv.addTraining(training);
                    break;
                }
            }

        }
    }

    public void updateTextView(ArrayList<Training> trainings, TextView trainingsInfo){
        trainingsInfoTitle.setVisibility(View.VISIBLE);
        trainingsInfo.setVisibility(View.VISIBLE);

        if (trainings.size() == 0){
            trainingsInfo.setText("No trainings/matches on selected day.");

        } else {
            trainingsInfo.setText("");
            for (int x = 0; x < trainings.size(); x++){
                System.out.println("Updating trainingsInfo, counter = " + x);
                System.out.println("Trainings on selected day: " + trainings);
                Training training = trainings.get(x);
                StringBuilder currentText = new StringBuilder(trainingsInfo.getText());
                currentText.append(training.getTeam().toUpperCase() + "\n");
                currentText.append(training.getFormattedDate() + "\n");
                currentText.append("\nTime: " + training.getTime());
                currentText.append("\nLocation: " + training.getLocation());
                currentText.append("\nDrills: " + training.getDrills() + "\n\n\n");

                trainingsInfo.setText(currentText.toString());

            }
        }


    }

    /*
    ################################################################################################
    ############################## Button onClick Listeners ########################################
    ################################################################################################
    ################################################################################################
    ################################################################################################
     */

    public void onNextMonth(){
        onCloseBtn();
        calendarDate = calendarDate.plusMonths(1);
        updateCalendar();
    }

    public void onLastMonth() {
        onCloseBtn();
        calendarDate = calendarDate.minusMonths(1);
        updateCalendar();
    }

    private void onCloseBtn(){
        trainingsInfo.setText("");
        trainingsInfo.setVisibility(View.INVISIBLE);
        closeBtn.setVisibility(View.INVISIBLE);
        trainingsInfoTitle.setVisibility(View.INVISIBLE);

        for (CustomTextView ctv: calendarCells){
            if (ctv.getId() == previousButtonId){
                ctv.setTextColor(Color.BLACK);
                break;
            }
        }
    }

}

