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
    To create a TRAINING instance, use createConfirmAction_for_SubmitTraining (String team, date, time, location, drills)
     */


    View view;

    //Section for initialising a "submit training" confirmAction
    private String mParamTeam, mParamFormattedDate, mParamTime, mParamLocation, mParamDrills;
    private boolean isTraining;

    //Section for intialising a "delete training" confirmAction
    private String trainingId;
    private boolean isDeleteTraining;


    //Section for initialising a "delete account" confirmAction
    private boolean isDeleteAccount;

    public static ConfirmActionFragment createConfirmAction_for_SubmitTraining(String team, String formattedDate, String time, String location, String drills) {
        ConfirmActionFragment fragment = new ConfirmActionFragment();
        fragment.mParamTeam = team;
        fragment.mParamFormattedDate = formattedDate;
        fragment.mParamTime = time;
        fragment.mParamLocation = location;
        fragment.mParamDrills = drills;
        fragment.isTraining = true;

        return fragment;
    }

    public static ConfirmActionFragment createConfirmAction_for_DeleteTraining(String trainingID){
        ConfirmActionFragment fragment = new ConfirmActionFragment();
        fragment.isDeleteTraining = true;
        fragment.trainingId = trainingID;
        return fragment;
    }

    public static ConfirmActionFragment createConfirmAction_for_DeleteAccount(){
        ConfirmActionFragment frag = new ConfirmActionFragment();
        frag.isDeleteAccount = true;
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_confirm_action, container, false);

        //inits the UI if the confirmActionFrag is a Submit Training type
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
            noBtn.setText("Change Training");
            noBtn.setOnClickListener((v) -> {
                Thread pushTrainingData = new Thread(() -> {
                    onReturnToCreateTraining();
                });
                pushTrainingData.start();
            });
        }

        //inits the UI if confirmActionFrag is a Delete Training type
        if(isDeleteTraining){
            Button yesBtn = view.findViewById(R.id.yesBtn);
            yesBtn.setText("Delete Training");
            yesBtn.setOnClickListener((v) -> {
                Thread deleteTrainingThread = new Thread(() -> {
                    try {
                        onDeleteTraining();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                deleteTrainingThread.start();
            });

            Button noBtn = view.findViewById(R.id.noBtn);
            noBtn.setText("Change Training");
            noBtn.setOnClickListener((v) -> {
                Thread pushTrainingData = new Thread(() -> {
                    onReturnToSelectTraining();
                });
                pushTrainingData.start();
            });
        }

        //inits the UI if the confirmActionFrag is a Delete Account type
        if (isDeleteAccount){

            Button yesBtn = view.findViewById(R.id.yesBtn);
            yesBtn.setText("Delete Account");
            yesBtn.setOnClickListener((v) -> {
                Thread deleteAccountThread = new Thread(() -> {
                    try {
                        onDeleteAccount();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                deleteAccountThread.start();
            });

            Button noBtn = view.findViewById(R.id.noBtn);
            noBtn.setText("Return to Profile");
            noBtn.setOnClickListener((v) -> {
                Thread pushTrainingData = new Thread(() -> {
                    onReturnToProfile();
                });
                pushTrainingData.start();
            });
        }


        return view;
    }

    private void onSubmitTraining( ) throws IOException {
        //establish variables
        String charset = java.nio.charset.StandardCharsets.UTF_8.name();
        String email = User.getInstance().getEmail();
        String password = User.getInstance().getPassword();
        String coach = String.valueOf( User.getInstance().isCoach() );
        String team;
        if (mParamTeam.equalsIgnoreCase("High Performance")) {
            team = "hp";
        } else if (mParamTeam.equalsIgnoreCase("development")) {
            team = "dv";
        } else {
            team = "cb";
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

    private void onDeleteAccount() throws IOException{
        String charset = java.nio.charset.StandardCharsets.UTF_8.name();
        String email = User.getInstance().getEmail();
        String password = User.getInstance().getPassword();

        URL obj = new URL("http://megabytten.org/eutrcapp/user/delete");

        //sets the encoded query
        String query = String.format("email=%s&password=%s"
                , URLEncoder.encode(email, charset)
                , URLEncoder.encode(password, charset));
        System.out.println("query = " + query);

        byte[] postData = query.getBytes(StandardCharsets.UTF_8 );
        int postDataLength = postData.length;

        //establishes HTTP connection with URL via the URL object, "obj"
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setConnectTimeout(5000);

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
            getActivity().runOnUiThread(()-> Toast.makeText(getContext(), "Successfully Deleted Training", Toast.LENGTH_SHORT).show());
            getActivity().finish();
        } else {
            getActivity().runOnUiThread(()-> Toast.makeText(getContext(), "Unable to Delete Account", Toast.LENGTH_SHORT).show());

        }
    }

    private void onDeleteTraining() throws IOException{
        String charset = java.nio.charset.StandardCharsets.UTF_8.name();
        String email = User.getInstance().getEmail();
        String password = User.getInstance().getPassword();

        URL obj = new URL("http://megabytten.org/eutrcapp/trainings/delete");

        //sets the encoded query
        String query = String.format("email=%s&password=%s&trainingID=%s"
                , URLEncoder.encode(email, charset)
                , URLEncoder.encode(password, charset)
                , URLEncoder.encode(trainingId, charset));
        System.out.println("query = " + query);

        byte[] postData = query.getBytes(StandardCharsets.UTF_8 );
        int postDataLength = postData.length;

        //establishes HTTP connection with URL via the URL object, "obj"
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setConnectTimeout(5000);


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
            getActivity().runOnUiThread(()-> Toast.makeText(getContext(), "Successfully Deleted Training", Toast.LENGTH_SHORT).show());
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            HomeActivity.replaceFragmentExternal(fragmentTransaction, HomeFragment.getInstance());
        } else {
            getActivity().runOnUiThread(()-> Toast.makeText(getContext(), "Unable to Delete Training", Toast.LENGTH_SHORT).show());
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            HomeActivity.replaceFragmentExternal(fragmentTransaction, new DeleteTrainingFragment());
        }
    }



    /*
    ################################################################################################
    ##################################### Return section ###########################################
    ################### - Section contains all methods which involve going BACK ####################
    ################### - Returns user to previous page where they came from    ####################
    ################################################################################################
     */

    private void onReturnToSelectTraining(){
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        HomeActivity.replaceFragmentExternal(fragmentTransaction, new DeleteTrainingFragment());
    }

    private void onReturnToCreateTraining(){
        CreateTrainingFragment.setFragmentArgs(mParamTeam, mParamTime, mParamLocation, mParamDrills);
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        HomeActivity.replaceFragmentExternal(fragmentTransaction, CreateTrainingFragment.getInstance());
    }

    private void onReturnToProfile(){
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        HomeActivity.replaceFragmentExternal(fragmentTransaction, ProfileFragment.getInstance());
    }


}