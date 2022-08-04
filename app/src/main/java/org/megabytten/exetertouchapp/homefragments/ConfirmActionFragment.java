package org.megabytten.exetertouchapp.homefragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.megabytten.exetertouchapp.HomeActivity;
import org.megabytten.exetertouchapp.R;
import org.megabytten.exetertouchapp.utils.User;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ConfirmActionFragment extends Fragment {
    /*
    this Class (unlike my other frags) must be instantiated!
    There are many different cases where confirm action may be required, so its important to scale it with instantiation
    To create a TRAINING instance, use newInstance (String team, date, time, location, drills)
     */


    View view;

    private static final String ARG_PARAM_BOOL_ISTRAINING = "isTraining";
    private static final String ARG_PARAM_TEAM = "paramTeam";
    private static final String ARG_PARAM_FORMATTED_DATE = "paramFormattedDate";
    private static final String ARG_PARAM_TIME = "paramTime";
    private static final String ARG_PARAM_LOCATION = "paramLocation";
    private static final String ARG_PARAM_DRILLS = "paramDrills";

    private String mParamTeam, mParamFormattedDate, mParamTime, mParamLocation, mParamDrills;
    private boolean isTraining;


    public static ConfirmActionFragment newInstance(String team, String formattedDate, String time, String location, String drills, boolean isTraining) {
        ConfirmActionFragment fragment = new ConfirmActionFragment();
        Bundle args = new Bundle();

        args.putBoolean(ARG_PARAM_BOOL_ISTRAINING, isTraining);
        args.putString(ARG_PARAM_TEAM, team);
        args.putString(ARG_PARAM_FORMATTED_DATE, formattedDate);
        args.putString(ARG_PARAM_TIME, time);
        args.putString(ARG_PARAM_LOCATION, location);
        args.putString(ARG_PARAM_DRILLS, drills);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isTraining = getArguments().getBoolean(ARG_PARAM_BOOL_ISTRAINING);
            mParamTeam = getArguments().getString(ARG_PARAM_TEAM);
            mParamFormattedDate = getArguments().getString(ARG_PARAM_FORMATTED_DATE);
            mParamTime = getArguments().getString(ARG_PARAM_TIME);
            mParamLocation = getArguments().getString(ARG_PARAM_LOCATION);
            mParamDrills = getArguments().getString(ARG_PARAM_DRILLS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_confirm_action, container, false);

        if (isTraining){
            Button yesBtn = view.findViewById(R.id.yesBtn);
            yesBtn.setText("Submit Training");
            yesBtn.setOnClickListener((v) -> {
                Thread pushTrainingData = new Thread(() -> {
                    try {
                        onSubmitTraining();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                pushTrainingData.start();
            });

            Button noBtn = view.findViewById(R.id.noBtn);
            noBtn.setText("Edit Training");
            noBtn.setOnClickListener((v) -> {
                Thread pushTrainingData = new Thread(() -> {
                    onEditTraining();
                });
                pushTrainingData.start();
            });
        }



        return view;
    }

    // TODO: 3/8/22 - APP STUFF
//          post req to server containing all info:
//          formatted date (dd/mm/yy), 2 letter team, location, drills, time

    // TODO: 3/8/22 - SERVER STUFF
//          need to check coach credentials (login) before allowing access to push
//          then need to add req.body info into sql query and push values into DB!


    private void onSubmitTraining( ) throws IOException {

        //establish variables
        String charset = java.nio.charset.StandardCharsets.UTF_8.name();
        String email = User.getInstance().getEmail();
        String password = User.getInstance().getPassword();
        String coach = String.valueOf( User.getInstance().isCoach() );
        String team;
        if (mParamTeam.equalsIgnoreCase("High Performance")) {
            team = "HP";
        } else if (mParamTeam.equalsIgnoreCase("development")) {
            team = "DV";
        } else {
            team = "CB";
        }

        URL obj = new URL("http://megabytten.org/eutrcapp/trainings/create");

        //sets the encoded query
        String query = String.format("email=%s&password=%s&coach=%s&team=%s&date=%s&time=%s&location=%s&drills=%s"
                , URLEncoder.encode(email, charset)
                , URLEncoder.encode(password, charset)
                , URLEncoder.encode(coach, charset)
                , URLEncoder.encode(team, charset)
                , URLEncoder.encode(mParamFormattedDate, charset)
                , URLEncoder.encode(mParamTime, charset)
                , URLEncoder.encode(mParamLocation, charset)
                , URLEncoder.encode(mParamDrills, charset));
        System.out.println("query = " + query);

        byte[] postData = query.getBytes(StandardCharsets.UTF_8 );
        int postDataLength = postData.length;

        //establishes HTTP connection with URL via the URL object, "obj"
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");

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
            System.out.println("Training successfully pushed to DB!");
            getActivity().runOnUiThread(()-> {
                Toast.makeText(getContext(), "Training successfully added!", Toast.LENGTH_LONG).show();
            });

            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            HomeActivity.replaceFragmentExternal(fragmentTransaction, HomeFragment.getInstance());

        } else {
            System.out.println("POST request not worked");
        }
    }

    private void onEditTraining(){
        CreateTrainingFragment.setFragmentArgs(mParamTeam, mParamTime, mParamLocation, mParamDrills);
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        HomeActivity.replaceFragmentExternal(fragmentTransaction, CreateTrainingFragment.getInstance());
    }

}