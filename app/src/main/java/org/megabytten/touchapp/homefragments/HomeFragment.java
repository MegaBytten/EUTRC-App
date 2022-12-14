package org.megabytten.touchapp.homefragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import org.json.JSONException;
import org.json.JSONObject;
import org.megabytten.touchapp.HomeActivity;
import org.megabytten.touchapp.R;
import org.megabytten.touchapp.utils.Training;
import org.megabytten.touchapp.utils.User;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class HomeFragment extends Fragment {

    private static HomeFragment homeFragment;

    //Data-specific variables/objects
    User user;
    View view;

    //Training-pulling specific data/variables
    Training hpTraining, dvTraining, cbTraining;


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

        //initialises RSVP buttons
        Button hpUserAvailableBtn = view.findViewById(R.id.hpUserAvailableBtn);
        hpUserAvailableBtn.setOnClickListener((v) -> {
            rsvpWorkerThread("hp", true);
        });
        Button hpUserUnavailableBtn = view.findViewById(R.id.hpUserUnavailableBtn);
        hpUserUnavailableBtn.setOnClickListener((v) -> {
            rsvpWorkerThread("hp", false);
        });

        Button dvUserAvailableBtn = view.findViewById(R.id.dvUserAvailableBtn);
        dvUserAvailableBtn.setOnClickListener((v) -> {
            rsvpWorkerThread("dv", true);
        });
        Button dvUserUnavailableBtn = view.findViewById(R.id.dvUserUnavailableBtn);
        dvUserUnavailableBtn.setOnClickListener((v) -> {
            rsvpWorkerThread("dv", false);
        });

        Button cbUserAvailableBtn = view.findViewById(R.id.cbUserAvailableBtn);
        cbUserAvailableBtn.setOnClickListener((v) -> {
            rsvpWorkerThread("cb", true);
        });
        Button cbUserUnavailableBtn = view.findViewById(R.id.cbUserUnavailableBtn);
        cbUserUnavailableBtn.setOnClickListener((v) -> {
            rsvpWorkerThread("cb", false);
        });

        //updates upcoming trainings!
        Thread newVerifThread = new Thread(() -> {
            System.out.println("New Thread launched. Reseting training data and then pulling Training JSON then Loading it.");
            hpTraining = null;
            dvTraining = null;
            cbTraining = null;
            try {
                pullTrainingJSON();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        });
        newVerifThread.start();

        //loads coach/player specific stuff
        if (user.isCoach()){
            //coach layout init

            //firstly, hide RSVP containers, only used for users
            LinearLayout hpRSVPContainer = view.findViewById(R.id.hpRSVPContainer);
            hpRSVPContainer.setVisibility(View.GONE);

            LinearLayout dvRSVPContainer = view.findViewById(R.id.dvRSVPContainer);
            dvRSVPContainer.setVisibility(View.GONE);

            LinearLayout cbRSVPContainer = view.findViewById(R.id.cbRSVPContainer);
            cbRSVPContainer.setVisibility(View.GONE);

            //add listeners to Training buttons, only for coaches
            Button addTrainingBtn = view.findViewById(R.id.addTrainingBtn);
            Button deleteTrainingBtn = view.findViewById(R.id.deleteTrainingBtn);

            addTrainingBtn.setOnClickListener(v -> {
                getActivity().runOnUiThread(() -> {
                    onAddTrainingBtn();
                });
            });

            deleteTrainingBtn.setOnClickListener(v -> {
                getActivity().runOnUiThread(()->{
                    onDeleteTrainingBtn();
                });
            });

        } else {
            //player layout init

            //hide coaches section containing Training buttons!
            LinearLayout coachSectionContainer = view.findViewById(R.id.coachSectionContainer);
            coachSectionContainer.setVisibility(View.GONE);
        }

        return view;
    }

    private void pullTrainingJSON() throws IOException, JSONException {
        URL url = new URL("http://megabytten.org/eutrcapp/trainings.json");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(10000);
        con.setRequestProperty("request", "next");

        int responseCode = con.getResponseCode();
        System.out.println("GET Response Code :: " + responseCode);

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
            view.findViewById(R.id.hpTrainingContainer).setVisibility(View.VISIBLE);
            view.findViewById(R.id.hpRSVPContainer).setVisibility(View.VISIBLE);
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
            view.findViewById(R.id.dvTrainingContainer).setVisibility(View.VISIBLE);
            view.findViewById(R.id.dvRSVPContainer).setVisibility(View.VISIBLE);
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
            view.findViewById(R.id.cbTrainingContainer).setVisibility(View.VISIBLE);
            view.findViewById(R.id.cbRSVPContainer).setVisibility(View.VISIBLE);
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

    private void rsvpWorkerThread(String team, boolean rsvpYes){
        Thread rsvpActionThread = new Thread(()-> {
            try {
                rsvpAction(team, rsvpYes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        rsvpActionThread.start();
    }

    private void rsvpAction(String team, boolean rsvpYes) throws IOException{
        //establish variables required for POST
        String charset = java.nio.charset.StandardCharsets.UTF_8.name();
        String email = User.getInstance().getEmail();
        String password = User.getInstance().getPassword();

        int id = 0;
        if (team.equalsIgnoreCase("hp")){
            id = hpTraining.getId();
        } else if (team.equalsIgnoreCase("dv")){
            id = dvTraining.getId();
        } else if (team.equalsIgnoreCase("cb")){
            id = cbTraining.getId();
        } else {
            System.err.println("ERROR: rsvpYes in HomeFragment - team parameter is not HP, DV, or CB.");
        }


        URL obj = new URL("http://megabytten.org/eutrcapp/trainings/rsvp");

        //sets the encoded query
        String query = String.format("email=%s&password=%s&trainingID=%s&rsvp=%s"
                , URLEncoder.encode(email, charset)
                , URLEncoder.encode(password, charset)
                , URLEncoder.encode(String.valueOf(id), charset)
                , URLEncoder.encode(String.valueOf(rsvpYes), charset)
        );
        System.out.println("query = " + query);

        byte[] postData = query.getBytes(StandardCharsets.UTF_8 );
        int postDataLength = postData.length;

        //establishes HTTP connection with URL via the URL object, "obj"
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setConnectTimeout(10000);

        //post method set up!
        con.setDoOutput( true );
        con.setInstanceFollowRedirects( false );
        con.setRequestMethod( "POST" );
        con.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
        con.setRequestProperty( "charset", "utf-8");
        con.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
        con.setUseCaches( false );

        DataOutputStream os = new DataOutputStream( con.getOutputStream() );
        os.write(query.getBytes(charset));
        os.flush();
        os.close();

        int responseCode = con.getResponseCode();
        System.out.println("POST Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) { //success
            System.out.println("Successfully updated user's attendance! Is user attending: " + rsvpYes);
            getActivity().runOnUiThread(()->{
                String text;
                if (rsvpYes){
                    text = "Successfully RSVP'd: Available!";
                } else {
                    text = "Successfully RSVP'd: Unavailable!";
                }
                Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
            }); //toast for RSVP: Avail/Unavail success.
        }

    }

    private void onAddTrainingBtn(){
        FragmentTransaction fragmentTransaction = getActivity()
                .getSupportFragmentManager().beginTransaction();
        HomeActivity.replaceFragmentExternal(fragmentTransaction, new CreateTrainingFragment());
    }

    private void onDeleteTrainingBtn(){
        FragmentTransaction fragmentTransaction = getActivity()
                .getSupportFragmentManager().beginTransaction();
        HomeActivity.replaceFragmentExternal(fragmentTransaction, new DeleteTrainingFragment());
    }
}