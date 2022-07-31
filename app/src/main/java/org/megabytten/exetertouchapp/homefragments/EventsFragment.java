package org.megabytten.exetertouchapp.homefragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import org.megabytten.exetertouchapp.R;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;

public class EventsFragment extends Fragment {
    private static EventsFragment eventsFragment;

    View view;
    static boolean isCalendarInit = false;
    ArrayList<LinearLayout> calendarRowLinLays;
    ArrayList<TextView> calendarCells;
    LocalDate calendarDate;


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

        return view;
    }


    /*
    ################################################################################################
    ################################################################################################
    ############################### Calendar Init/Updating methods #################################
    ################################################################################################
    ################################################################################################
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
                TextView tv = new TextView(getContext());
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
                calendarCells.get(i).setText(String.valueOf(day));
                day++;
            }
        }

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
    ################################################################################################
    ################################################################################################
    ################################################################################################
     */




    /*
    ################################################################################################
    ############################## Button onClick Listeners ########################################
    ################################################################################################
    ################################################################################################
    ################################################################################################
     */

    public void onNextMonth(){
        calendarDate = calendarDate.plusMonths(1);
        updateCalendar();
    }


    public void onLastMonth(){
        calendarDate = calendarDate.minusMonths(1);
        updateCalendar();
    }

}

