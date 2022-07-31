package org.megabytten.exetertouchapp.homefragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.megabytten.exetertouchapp.LauncherActivity;
import org.megabytten.exetertouchapp.R;
import org.megabytten.exetertouchapp.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HomeFragment extends Fragment {

    public static HomeFragment homeFragment;

    User user;
    View view;

    static JSONObject hpTrainingJSON;
    static JSONObject dvTrainingJSON;
    static JSONObject cbTrainingJSON;

    static boolean jsonPulled = false;

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

        //pull training info async - if hasnt been pulled before!
        if (!jsonPulled){
            Thread newVerifThread = new Thread(() -> {
                System.out.println("New Thread launched. Pulling Training JSON then Loading it.");
                try {
                    pullTrainingJSON();
                    jsonPulled = true;
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            });
            newVerifThread.start();
        }



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
        //wait for JSON Pull to update UI
        Thread newVerifThread = new Thread(() -> {
            while(!jsonPulled){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            getActivity().runOnUiThread(() -> {
                try {
                    loadTrainingInfo();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        });
        newVerifThread.start();

// TODO: 30/7/22 Add User available/unavailable to next training sessions
// TODO: 30/7/22 add coach's 'ADD TRAINING' button
//      --> launches new fragment, survey to take in all required details (most inputs must be in specific format or length)
//      --> submit + back button (maybe even save draft?) : submit sends to database after input checks
        //do communal stuff

        //do coach/player specific stuff

        if (user.isCoach()){
            //coach layout init

        } else {
            //player layout init
            LinearLayout hpRSVPContainer = view.findViewById(R.id.hpRSVPContainer);
            LinearLayout dvRSVPContainer = view.findViewById(R.id.dvRSVPContainer);
            LinearLayout cbRSVPContainer = view.findViewById(R.id.cbRSVPContainer);

            hpRSVPContainer.setVisibility(View.VISIBLE);
            dvRSVPContainer.setVisibility(View.VISIBLE);
            cbRSVPContainer.setVisibility(View.VISIBLE);
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
            System.out.println("JSON Trainings completed :: " + sb.toString());
            JSONObject obj = new JSONObject(sb.toString());

            hpTrainingJSON = obj.getJSONObject("hpTraining");
            dvTrainingJSON = obj.getJSONObject("dvTraining");
            cbTrainingJSON = obj.getJSONObject("cbTraining");
            con.disconnect();
        }
    }



    private void loadTrainingInfo() throws JSONException {
        //some trainings may be null, need to null check before updating
        if (hpTrainingJSON.getString("date").equalsIgnoreCase("none")){
            ScrollView hpTrainingContainer = getActivity().findViewById(R.id.hpTrainingContainer);
            hpTrainingContainer.setVisibility(View.GONE);

            TextView hpNullTrainingDataTxt = getActivity().findViewById(R.id.hpNullTrainingDataTxt);
            hpNullTrainingDataTxt.setVisibility(View.VISIBLE);

            LinearLayout hpRSVPContainer = getActivity().findViewById(R.id.hpRSVPContainer);
            hpRSVPContainer.setVisibility(View.GONE);

        } else {
            //HP Training section load
            TextView hpSectionDateData = view.findViewById(R.id.hpSectionDateData);
            TextView hpSectionTimeData = view.findViewById(R.id.hpSectionTimeData);
            TextView hpSectionLocationData = view.findViewById(R.id.hpSectionLocationData);
            TextView hpSectionDrillsData = view.findViewById(R.id.hpSectionDrillsData);

            hpSectionDateData.setText(hpTrainingJSON.getString("date"));
            hpSectionTimeData.setText(hpTrainingJSON.getString("time"));
            hpSectionLocationData.setText(hpTrainingJSON.getString("location"));
            hpSectionDrillsData.setText(hpTrainingJSON.getString("drills"));
        }

        if (dvTrainingJSON.getString("date").equalsIgnoreCase("none")){
            //No trainign data for the next week! - from server
            //Function below: removes the scrollview containing all dummy data
            // makes the "no trainings this week" text visible in place of scroll view
            ScrollView dvTrainingContainer = getActivity().findViewById(R.id.dvTrainingContainer);
            dvTrainingContainer.setVisibility(View.GONE);

            TextView dvNullTrainingDataTxt = getActivity().findViewById(R.id.dvNullTrainingDataTxt);
            dvNullTrainingDataTxt.setVisibility(View.VISIBLE);

            LinearLayout dvRSVPContainer = getActivity().findViewById(R.id.dvRSVPContainer);
            dvRSVPContainer.setVisibility(View.GONE);

        } else {
            //DV Training section load
            TextView dvSectionDateData = view.findViewById(R.id.dvSectionDateData);
            TextView dvSectionTimeData = view.findViewById(R.id.dvSectionTimeData);
            TextView dvSectionLocationData = view.findViewById(R.id.dvSectionLocationData);
            TextView dvSectionDrillsData = view.findViewById(R.id.dvSectionDrillsData);

            dvSectionDateData.setText(dvTrainingJSON.getString("date"));
            dvSectionTimeData.setText(dvTrainingJSON.getString("time"));
            dvSectionLocationData.setText(dvTrainingJSON.getString("location"));
            dvSectionDrillsData.setText(dvTrainingJSON.getString("drills"));
        }

        if (cbTrainingJSON.getString("date").equalsIgnoreCase("none")){
            ScrollView cbTrainingContainer = getActivity().findViewById(R.id.cbTrainingContainer);
            cbTrainingContainer.setVisibility(View.GONE);

            TextView cbNullTrainingDataTxt = getActivity().findViewById(R.id.cbNullTrainingDataTxt);
            cbNullTrainingDataTxt.setVisibility(View.VISIBLE);

            LinearLayout cbRSVPContainer = getActivity().findViewById(R.id.cbRSVPContainer);
            cbRSVPContainer.setVisibility(View.GONE);

        } else {
            //CB Training section load
            TextView cbSectionDateData = view.findViewById(R.id.cbSectionDateData);
            TextView cbSectionTimeData = view.findViewById(R.id.cbSectionTimeData);
            TextView cbSectionLocationData = view.findViewById(R.id.cbSectionLocationData);
            TextView cbSectionDrillsData = view.findViewById(R.id.cbSectionDrillsData);

            cbSectionDateData.setText(cbTrainingJSON.getString("date"));
            cbSectionTimeData.setText(cbTrainingJSON.getString("time"));
            cbSectionLocationData.setText(cbTrainingJSON.getString("location"));
            cbSectionDrillsData.setText(cbTrainingJSON.getString("drills"));
        }
    }

    private void coachSetup(){
        //make gone elements visible!
        //eg add training button! --> launches new fragment which asks for details and submits to mg.org


    }
}