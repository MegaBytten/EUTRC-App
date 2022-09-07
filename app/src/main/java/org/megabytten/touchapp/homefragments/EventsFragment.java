package org.megabytten.touchapp.homefragments;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.megabytten.touchapp.utils.CustomTextView;
import org.megabytten.touchapp.R;
import org.megabytten.touchapp.utils.EventsFragment_RecyclerAdapter;
import org.megabytten.touchapp.utils.Training;

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
    RecyclerView recyclerView;
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

        //CHANGED:
//        getActivity().runOnUiThread( () -> {
//            if (!isCalendarInit) calendarLinCellInit();
//        });
//        TO:
//        if (!isCalendarInit) calendarLinCellInit();


        if (!isCalendarInit) calendarLinCellInit();

        view.findViewById(R.id.nextMonthBtn).setOnClickListener(v -> {
            getActivity().runOnUiThread(this::onNextMonth);
        });

        view.findViewById(R.id.lastMonthBtn).setOnClickListener(v -> {
            getActivity().runOnUiThread(this::onLastMonth);
        });

        trainingsInfoTitle = view.findViewById(R.id.trainingInfoTitle);
        trainingsInfoTitle.setPaintFlags(trainingsInfoTitle.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);


        //RecyclerView Init
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setVisibility(View.GONE);
        EventsFragment_RecyclerAdapter rA = new EventsFragment_RecyclerAdapter();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(rA);

        closeBtn = view.findViewById(R.id.closeBtn);
        closeBtn.setVisibility(View.GONE);
        closeBtn.setOnClickListener(v ->
            getActivity().runOnUiThread(this::onCloseBtn)
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

        //This loop creates 6 linear myresources (rows) - one for each week of the month
        //all these lin myresources are then stored in calendarRowLinLays
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
                tv.setText("W:" + (y + 1) + ", D:" + (x + 1));
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
            trainingsInfoTitle.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            if (selectedCell.getTrainings().size() == 0){
                System.out.println("No trainings on selected day!");
                recyclerView.setVisibility(View.GONE);
                closeBtn.setVisibility(View.GONE);
                trainingsInfoTitle.setVisibility(View.GONE);
            } else {
                EventsFragment_RecyclerAdapter rA = (EventsFragment_RecyclerAdapter) recyclerView.getAdapter();
                rA.replaceAll(selectedCell.getTrainings());
            }
        }
    }

    private void updateCalendar(){
        TextView monthTitleTxt = view.findViewById(R.id.monthTitleTxt);

        if (calendarDate.getMonth().toString().equalsIgnoreCase(LocalDate.now().getMonth().toString())){
            monthTitleTxt.setTextColor(Color.RED);
        } else {
            monthTitleTxt.setTextColor(Color.WHITE);
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
                calendarCells.get(i).setText(day + "\n");
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
        con.setRequestProperty("request", "calendar");

        int responseCode = con.getResponseCode();
        System.out.println("GET Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));
            StringBuilder sb = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }

            //null check for response - if there are no trainings this month
            if (sb.toString().equalsIgnoreCase("null")){
                //return is null, no trainings!
                System.out.println("Trainings.json == null");
                return;
            }

            //This section obtains the JSONArray which contains all trainings in JSONObject format from the server
            //it loops through the Array, creating new Training objects from the JSONObjects and saving them to an ArrayList
            //this arrayList containing the trainings then gets passed to updateCalendarEvents which updates the UI for all trainings.
            JSONArray obj = new JSONArray(sb.toString());
            ArrayList<Training> trainings = new ArrayList<>();
            for (int i = 0; i < obj.length(); i++) {
                JSONObject trainingJSON = obj.getJSONObject(i);

                Training training = null;
                if (Training.jsonHasTraining(trainingJSON)){
                    System.out.println("Events fragment: JSONObject in JSONArray has Training! Creating new training object from JSONObject.");
                    try {
                        training = new Training(trainingJSON);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (training != null){
                    trainings.add(training);
                }
            }

            getActivity().runOnUiThread(() -> {
                try {
                    updateCalendarEvents(trainings);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });


        }
    }

    private void updateCalendarEvents(ArrayList<Training> trainings) throws JSONException {

        for (Training training : trainings){
            //this section obtains the int dateDay, and parses it into a string so that it may be formatted
            //IF I MODIFY THIS CODE THE EVENTS FRAG BREAKS
            int dateDay = training.getDateDay();
            String unformattedTrainingID = String.valueOf(dateDay);
            StringBuilder editString = new StringBuilder(unformattedTrainingID);
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
        recyclerView.setVisibility(View.GONE);
        closeBtn.setVisibility(View.GONE);
        trainingsInfoTitle.setVisibility(View.GONE);

        for (CustomTextView ctv: calendarCells){
            if (ctv.getId() == previousButtonId){
                ctv.setTextColor(Color.BLACK);
                break;
            }
        }
    }

}

