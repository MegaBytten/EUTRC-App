package org.megabytten.exetertouchapp.homefragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import org.json.JSONException;
import org.json.JSONObject;
import org.megabytten.exetertouchapp.HomeActivity;
import org.megabytten.exetertouchapp.R;
import org.megabytten.exetertouchapp.Training;
import org.megabytten.exetertouchapp.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HomeFragment extends Fragment {

    private static HomeFragment homeFragment;

    //Data-specific variables/objects
    User user;
    View view;

    //Training-pulling specific data/variables
    Training hpTraining, dvTraining, cbTraining;
    static boolean jsonPulled; //needs to be static!!


    public static HomeFragment getInstance(){
        if (homeFragment == null){
            System.out.println("Initializing homeFragment.");
            homeFragment = new HomeFragment();
        }
        return homeFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        user = User.getInstance();
        view = inflater.inflate(R.layout.fragment_home, container, false);
        TextView welcomeTxt = view.findViewById(R.id.welcomeTxt);

        if (user.isCoach()){
            welcomeTxt.setText("Welcome coach " + User.getInstance().getFirstName());
        } else {
            welcomeTxt.setText("Welcome player " + User.getInstance().getFirstName());
        }

        //check if JSON has been pulled - if not, first time set up, if yes, just load
        //pull training info async - if hasnt been pulled before!
        if (!jsonPulled){
            Thread newVerifThread = new Thread(() -> {
                System.out.println("New Thread launched. Pulling Training JSON then Loading it.");
                try {
                    jsonPulled = true;
                    pullTrainingJSON();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            });
            newVerifThread.start();
        } else {
            //this needs to occur or textviews will display default information from .xml!
            getActivity().runOnUiThread(() -> {
                try {
                    loadTrainingInfo();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        }

        //do communal stuff

        //do coach/player specific stuff

        if (user.isCoach()){
            //coach layout init
            Button addTrainingBtn = view.findViewById(R.id.addTrainingBtn);
            Button deleteTrainingBtn = view.findViewById(R.id.deleteTrainingBtn);

            addTrainingBtn.setOnClickListener(v -> {
                getActivity().runOnUiThread(() -> {
                    onAddTrainingBtn();
                });
            });

            deleteTrainingBtn.setOnClickListener(v -> {

            });

        } else {
            //player layout init
            LinearLayout coachSectionContainer = view.findViewById(R.id.coachSectionContainer);
            coachSectionContainer.setVisibility(View.GONE);
        }

        return view;
    }

    private void pullTrainingJSON() throws IOException, JSONException {
        URL url = new URL("http://megabytten.org/eutrcapp/trainings.json");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        int responseCode = con.getResponseCode();
        System.out.println("POST Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));
            StringBuilder sb = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }

            System.out.println("JSON Trainings completed :: " + sb);
            JSONObject obj = new JSONObject(sb.toString());

            //If the JSON objects contain training information (not null information), following method will convert them into
            //Training objects from JSON objects.
            try {
                saveJSONtoTrainings( obj.getJSONObject("hpTraining"), obj.getJSONObject("dvTraining"), obj.getJSONObject("cbTraining") );
            } catch (Exception e) {
                e.printStackTrace();
            }

            getActivity().runOnUiThread(() -> {
                try {
                    loadTrainingInfo();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        }
        con.disconnect();
    }

    private void saveJSONtoTrainings(JSONObject hpTrainingJSON, JSONObject dvTrainingJSON, JSONObject cbTrainingJSON) throws Exception {
        if (Training.jsonHasTraining(hpTrainingJSON)){
            hpTraining = new Training(hpTrainingJSON);
        }

        if (Training.jsonHasTraining(dvTrainingJSON)){
            dvTraining = new Training(dvTrainingJSON);
        }

        if (Training.jsonHasTraining(cbTrainingJSON)){
            cbTraining = new Training(cbTrainingJSON);
        }
    }

    private void loadTrainingInfo() throws JSONException {
        //some trainings may be null, need to null check before updating
        if (hpTraining == null){
            view.findViewById(R.id.hpTrainingContainer).setVisibility(View.GONE);
            view.findViewById(R.id.hpRSVPContainer).setVisibility(View.GONE);

        } else {
            //HP Training section load
            view.findViewById(R.id.hpNullTrainingDataTxt).setVisibility(View.GONE);

            TextView hpDate = view.findViewById(R.id.hpSectionDateData);
            hpDate.setText(hpTraining.getFormattedDate());

            TextView hpTime = view.findViewById(R.id.hpSectionTimeData);
            hpTime.setText(hpTraining.getTime());

            TextView hpLoc = view.findViewById(R.id.hpSectionLocationData);
            hpLoc.setText(hpTraining.getLocation());

            TextView hpDrills = view.findViewById(R.id.hpSectionDrillsData);
            hpDrills.setText(hpTraining.getDrills());

        }

        if (dvTraining == null){
            view.findViewById(R.id.dvTrainingContainer).setVisibility(View.GONE);
            view.findViewById(R.id.dvRSVPContainer).setVisibility(View.GONE);

        } else {
            //DV Training section load
            view.findViewById(R.id.dvNullTrainingDataTxt).setVisibility(View.GONE);

            TextView date = view.findViewById(R.id.dvSectionDateData);
            date.setText(dvTraining.getFormattedDate());

            TextView time = view.findViewById(R.id.dvSectionTimeData);
            time.setText(dvTraining.getTime());

            TextView loc = view.findViewById(R.id.dvSectionLocationData);
            loc.setText(dvTraining.getLocation());

            TextView drills = view.findViewById(R.id.dvSectionDrillsData);
            drills.setText(dvTraining.getDrills());
        }

        if (cbTraining == null){
            view.findViewById(R.id.cbTrainingContainer).setVisibility(View.GONE);
            view.findViewById(R.id.cbRSVPContainer).setVisibility(View.GONE);

        } else {
            //DV Training section load
            view.findViewById(R.id.cbNullTrainingDataTxt).setVisibility(View.GONE);

            TextView date = view.findViewById(R.id.cbSectionDateData);
            date.setText(cbTraining.getFormattedDate());

            TextView time = view.findViewById(R.id.cbSectionTimeData);
            time.setText(cbTraining.getTime());

            TextView loc = view.findViewById(R.id.cbSectionLocationData);
            loc.setText(cbTraining.getLocation());

            TextView drills = view.findViewById(R.id.cbSectionDrillsData);
            drills.setText(cbTraining.getDrills());
        }
    }

    public void onAddTrainingBtn(){
        FragmentTransaction fragmentTransaction = getActivity()
                .getSupportFragmentManager().beginTransaction();
        HomeActivity.replaceFragmentExternal(fragmentTransaction, CreateTrainingFragment.getInstance());
    }

    public void onDeleteTrainingBtn(){

    }
}