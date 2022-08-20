package org.megabytten.exetertouchapp.homefragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.megabytten.exetertouchapp.HomeActivity;
import org.megabytten.exetertouchapp.R;
import org.megabytten.exetertouchapp.utils.RecyclerAdapter;
import org.megabytten.exetertouchapp.utils.Training;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class DeleteTrainingFragment extends Fragment {

    View view;
    boolean filterOptionsToggled = false;

    //filtering and RecycleView components
    private ArrayList<Training> trainingsList = new ArrayList<>();
    private ArrayList<Training> filteredData = new ArrayList<>();

    //recycler-specific views
    private RecyclerView recyclerView;
    private SearchView searchView;

    //filtering specific views
    LinearLayout filterSettingsLinLay;
    CheckBox filteringOptionID, filteringOptionDate, filteringOptionTeam, filteringOptionTime;
    boolean filterId, filterDate, filterTeam, filterTime;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_delete_training, container, false);

        //views init
        recyclerView = view.findViewById(R.id.recyclerView);
        searchView = view.findViewById(R.id.searchView);
        Button editBtn = view.findViewById(R.id.editBtn);
        Button attendanceBtn = view.findViewById(R.id.attendanceBtn);
        Button delBtn = view.findViewById(R.id.deleteBtn);
        Button backBtn = view.findViewById(R.id.backBtn);

        //Filtering section init
        ImageButton filterBtn = view.findViewById(R.id.filterBtn);
        filterSettingsLinLay = view.findViewById(R.id.filterSettingsLinLay);
        filteringOptionID = view.findViewById(R.id.filteringOptionID);
        filteringOptionDate = view.findViewById(R.id.filteringOptionDate);
        filteringOptionTeam = view.findViewById(R.id.filteringOptionTeam);
        filteringOptionTime = view.findViewById(R.id.filteringOptionTime);

        filterId = filteringOptionID.isChecked();
        filterDate = filteringOptionDate.isChecked();
        filterTeam = filteringOptionTeam.isChecked();
        filterTime = filteringOptionTime.isChecked();

        //check changed listeners for checkBoxes
        filteringOptionID.setOnCheckedChangeListener((buttonView, isChecked) -> {
            filterId = isChecked;
            filterList();
        });
        filteringOptionDate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            filterDate = isChecked;
            filterList();
        });
        filteringOptionTeam.setOnCheckedChangeListener((buttonView, isChecked) -> {
            filterTeam = isChecked;
            filterList();
        });
        filteringOptionTime.setOnCheckedChangeListener((buttonView, isChecked) -> {
            filterTime = isChecked;
            filterList();
        });


        //click Listeners for buttons
        editBtn.setOnClickListener((v)->{
            RecyclerAdapter rA = (RecyclerAdapter) recyclerView.getAdapter();
            if (rA.getSelectedTrainingTitleTxt() != null){
                Training selectedTraining = rA.getSelectedTraining();
                CreateTrainingFragment frag = new CreateTrainingFragment(selectedTraining.getTeam(), selectedTraining.getTime(), selectedTraining.getLocation(), selectedTraining.getDrills(), false, String.valueOf(selectedTraining.getId()) );
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                HomeActivity.replaceFragmentExternal(fragmentTransaction, frag);


            } else { //selected Button == null, training has not been selected
                Toast.makeText(getContext(), "Please select a training to edit", Toast.LENGTH_SHORT).show();
            }
        });
        
        attendanceBtn.setOnClickListener((v -> {
            // TODO: 20/8/22 Finish view page
//              - Add a new fragment coaches only which displays attendance
//              - uses a recylcer view which inflates a new list Item that holds details:
//                  > Full attendance count
//                  > first/last (full) name
//                  > email + phone number
        }));

        delBtn.setOnClickListener((v)->{
            RecyclerAdapter rA = (RecyclerAdapter) recyclerView.getAdapter();
            if (rA.getSelectedTrainingTitleTxt() != null){
                ConfirmActionFragment confirmActionFragment = ConfirmActionFragment.createConfirmAction_for_DeleteTraining(String.valueOf( rA.getSelectedTraining().getId() ));
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                HomeActivity.replaceFragmentExternal(fragmentTransaction, confirmActionFragment);
            } else { //selected Button == null, training has not been selected
                Toast.makeText(getContext(), "Please select a training to delete", Toast.LENGTH_SHORT).show();
            }
        });

        backBtn.setOnClickListener((v -> {
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            HomeActivity.replaceFragmentExternal(fragmentTransaction, HomeFragment.getInstance());
        }));

        filterBtn.setOnClickListener((v -> {
            if (filterOptionsToggled){
                filterSettingsLinLay.setVisibility(View.GONE);
                filterBtn.setImageResource(R.drawable.ic_filter_foreground);
                filterOptionsToggled = false;
            } else {
                filterSettingsLinLay.setVisibility(View.VISIBLE);
                filterBtn.setImageResource(R.drawable.ic_filter_opened_foreground);
                filterOptionsToggled = true;
            }

        }));


        //searchView event listeners
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //not sure what textSubmit does :D
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList();
                return true;
            }
        });


        //pull all training info from server and displays it via recyclerAdapter
        Thread pullData = new Thread(()->{
            try {
                getTrainings();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        });
        pullData.start(); // this thread (in getTrainings()) also sets the recycler adapter

        //searchView functionality and filtering


        return view;
    }

    private void getTrainings() throws IOException, JSONException {

        URL url = new URL("http://megabytten.org/eutrcapp/trainings.json");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(10000);
        con.setRequestProperty("request", "modify");

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
            if (sb.toString().equalsIgnoreCase("null")) {
                //return is null, no trainings for the month.
                System.out.println("Trainings this month.json = null");
                return;
            }

            //This section obtains the JSONArray which contains all trainings in JSONObject format from the server
            //it loops through the Array, creating new Training objects from the JSONObjects and saving them to an ArrayList
            //this arrayList containing the trainings then gets passed to updateCalendarEvents which updates the UI for all trainings.
            JSONArray obj = new JSONArray(sb.toString());
            for (int i = 0; i < obj.length(); i++) {
                JSONObject trainingJSON = obj.getJSONObject(i);

                Training training = null;
                if (Training.jsonHasTraining(trainingJSON)) { // !jsonHasTraining occurs when date_day == "none", N/a in this case.
                    System.out.println("Events fragment: JSONObject in JSONArray has Training! Creating new training object from JSONObject.");
                    try {
                        training = new Training(trainingJSON);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (training != null) {
                    trainingsList.add(training);
                }
            }
        }

        getActivity().runOnUiThread(()-> setRecyclerAdapter());
    }

    private void setRecyclerAdapter(){
        RecyclerAdapter rA = new RecyclerAdapter();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(rA);

        rA.replaceAll(trainingsList);
    }


    /*
    ################################################################################################
    ############################### Filtering Section ##############################################
    ############### - Section below contains all methods relevant for search result ################
    ###############    filtering and any related util/methods.                      ################
    ################################################################################################
    ################################################################################################
     */

    private void filterList(){
        filteredData.clear();
        String query = searchView.getQuery().toString();

        //if query is empty, display all trainings
        if (query.equalsIgnoreCase("")){
            RecyclerAdapter rA = (RecyclerAdapter) recyclerView.getAdapter();
            rA.replaceAll(trainingsList);
            return;
        }

        if (isNumeric(query)){ //handle int and strings differently
            int queryInt = Integer.valueOf(query);
            for (Training tr : trainingsList){

                if (filterId) { //enable inclusion from ID matches
                    if (tr.getId() == queryInt){
                        if (!filteredData.contains(tr)){ // if filteredDate does NOT contain training, add it.
                            filteredData.add(tr);
                        }
                    }
                }

                if (filterDate){ //enable inclusion from Date matches
                    if (tr.getFormattedDate().contains(query)){ //formattedDate and query do match
                        if (!filteredData.contains(tr)){ // if filteredDate does NOT contain training, add it.
                            filteredData.add(tr);
                        }
                    }
                }

                if (filterTime) { //enable inclusion from Time matching
                    if (tr.getTime().contains(query)){ //training time and query do match
                        if (!filteredData.contains(tr)){ // if filteredDate does NOT contain training, add it.
                            filteredData.add(tr);
                        }
                    }
                }
            }
        } else { //handle as string
            for (Training tr : trainingsList) {

                if (filterDate){
                    if(tr.getFormattedDate().contains(query)){
                        if (!filteredData.contains(tr)){ // if filteredDate does NOT contain training, add it.
                            filteredData.add(tr);
                        }
                    }
                }

                if (filterTeam){
                    if (tr.getTeam().contains(query)){
                        if (!filteredData.contains(tr)){ // if filteredDate does NOT contain training, add it.
                            filteredData.add(tr);
                        }
                    }
                }

                if (filterTime){
                    if (tr.getTime().contains(query)){
                        if (!filteredData.contains(tr)){ // if filteredDate does NOT contain training, add it.
                            filteredData.add(tr);
                        }
                    }
                }
            }
        }
        RecyclerAdapter rA = (RecyclerAdapter) recyclerView.getAdapter();
        rA.replaceAll(filteredData);
    }

    private boolean isNumeric(String strNum) {
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}