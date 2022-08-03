package org.megabytten.exetertouchapp.homefragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import org.megabytten.exetertouchapp.HomeActivity;
import org.megabytten.exetertouchapp.R;
import org.megabytten.exetertouchapp.SignupActivity;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class CreateTrainingFragment extends Fragment {

    private static CreateTrainingFragment createTrainingFragment;

    //section for UI views!
    View view;
    EditText timeInput, locationInput, drillsInput;
    Spinner teamSpinner;
    CalendarView calendarView;

    //calendar dates
    String formattedDate;

    public static CreateTrainingFragment getInstance(){
        if (createTrainingFragment == null){
            System.out.println("Initialising createTrainingFragment.");
            createTrainingFragment = new CreateTrainingFragment();
        }
        return createTrainingFragment;
    }

    private static final String ARG_PARAM_TEAM = "paramTeam";
    private static final String ARG_PARAM_DATE = "paramDate";
    private static final String ARG_PARAM_TIME = "paramTime";
    private static final String ARG_PARAM_LOCATION = "paramLocation";
    private static final String ARG_PARAM_DRILLS = "paramDrills";

    public CreateTrainingFragment() {
        // Required empty public constructor
    }

    public static void setFragmentArgs(String team, String time, String location, String drills){
        Bundle args = new Bundle();

        args.putString(ARG_PARAM_TEAM, team);
        args.putString(ARG_PARAM_TIME, time);
        args.putString(ARG_PARAM_LOCATION, location);
        args.putString(ARG_PARAM_DRILLS, drills);

        getInstance().setArguments(args);
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_create_training, container, false);

        //initialise views for use
        timeInput = view.findViewById(R.id.editTextTime);
        locationInput = view.findViewById(R.id.editTextLocation);
        drillsInput = view.findViewById(R.id.editTextDrills);
        teamSpinner = view.findViewById(R.id.teamSpinner);
        calendarView = view.findViewById(R.id.calendarView);

        //set up SpinnerView
        String[] arraySpinner = new String[] {"High Performance", "Development", "Club"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        teamSpinner.setAdapter(adapter);

        //need to set current date for formattedDate
        Date date = new Date();
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int year1  = localDate.getYear();
        int month1 = localDate.getMonthValue();
        int day1   = localDate.getDayOfMonth();
        formattedDate = formatDate(day1, month1, year1);

        //ON SUBMIT BUTTON CLICK - checks if submissionisOk before sending to confirmActionFragment
        Button submitBtn = view.findViewById(R.id.submitBtn);
        submitBtn.setOnClickListener((v) -> {
            if (!isTrainingOk()) {
                getActivity().runOnUiThread(()->{
                    Toast.makeText(getContext(), "Please enter a time.", Toast.LENGTH_SHORT).show();
                });
                return;
            }
            
            String team = teamSpinner.getSelectedItem().toString();
            String time = timeInput.getText().toString();
            String location = locationInput.getText().toString();
            String drills = drillsInput.getText().toString();

            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            HomeActivity.replaceFragmentExternal(fragmentTransaction, ConfirmActionFragment.newInstance(team, formattedDate, time, location, drills, true));
        });

        Button backButton = view.findViewById(R.id.backBtn);
        backButton.setOnClickListener((v) -> {
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            HomeActivity.replaceFragmentExternal(fragmentTransaction, HomeFragment.getInstance());
        });

        //save selected date when it changes
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            formattedDate = formatDate(dayOfMonth, month+1, year);
        });

        //this method LOADS all previous information into the fragment
        //prevents losing all training info upon not confirming!
        if (getArguments() != null) {
            String team = getArguments().getString(ARG_PARAM_TEAM);
            String time = getArguments().getString(ARG_PARAM_TIME);
            String location = getArguments().getString(ARG_PARAM_LOCATION);
            String drills = getArguments().getString(ARG_PARAM_DRILLS);

            //calendar date is not updated, must reselect date
            TextView calendarTxt = view.findViewById(R.id.calendarTxt);
            calendarTxt.setTextColor(Color.RED);

            if (team.equalsIgnoreCase("High performance")){
                teamSpinner.setSelection(0);
            } else if (team.equalsIgnoreCase("Development")){
                teamSpinner.setSelection(1);
            } else {
                teamSpinner.setSelection(2);
            }

            timeInput.setText(time);
            locationInput.setText(location);
            drillsInput.setText(drills);

        }

        return view;
    }


    private String formatDate(int dateDay, int dateMonth, int dateYear){
        String formattedDay, formattedMonth, formattedYear;

        if ( String.valueOf(dateDay).length() == 1){
            char day = String.valueOf(dateDay).charAt(0);
            formattedDay = new StringBuilder("0").append(day).toString();
        } else {
            formattedDay = String.valueOf(dateDay);
        }

        if (String.valueOf(dateMonth).length() == 1){
            char month = String.valueOf(dateMonth).charAt(0);
            formattedMonth = new StringBuilder("0").append(month).toString();
        } else {
            formattedMonth = String.valueOf(dateMonth);
        }

        formattedYear = new StringBuilder().append(String.valueOf(dateYear).charAt(String.valueOf(dateYear).length()-2)).append(String.valueOf(dateYear).charAt(String.valueOf(dateYear).length()-1)).toString();

        return formattedDay + "/" + formattedMonth + "/" + formattedYear;
    }

    private boolean isTrainingOk(){
        if (timeInput.getText().toString().equalsIgnoreCase("")){
            return false;
        }
        return true;
    }

}