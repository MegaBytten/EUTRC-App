package org.megabytten.exetertouchapp.homefragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.megabytten.exetertouchapp.HomeActivity;
import org.megabytten.exetertouchapp.R;
import org.megabytten.exetertouchapp.utils.Training;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ViewTrainingDetailsFragment extends Fragment {

    View view;
    Training training;
    TextView attendance, unavailability, notes;

    public ViewTrainingDetailsFragment(Training training){
        this.training = training;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_view_training_details, container, false);

        Button backBtn = view.findViewById(R.id.backBtn);
        backBtn.setOnClickListener((v)->{
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            HomeActivity.replaceFragmentExternal(fragmentTransaction, new DeleteTrainingFragment());
        });

        TextView titleTxt = view.findViewById(R.id.titleTxt);
        titleTxt.setText("Viewing Training #" + training.getId());

        notes = view.findViewById(R.id.notes);
        notes.setMovementMethod(new ScrollingMovementMethod());

        attendance = view.findViewById(R.id.attendance);
        unavailability = view.findViewById(R.id.unavailability);

        Thread getAttendanceData = new Thread(()->{
            try {
                pullAttendanceData();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        });
        getAttendanceData.start();

        return view;
    }

    private void pullAttendanceData() throws IOException, JSONException {
        URL url = new URL("http://megabytten.org/eutrcapp/trainings/availability.json");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(10000);
        con.setRequestProperty("id", String.valueOf(training.getId()));

        int responseCode = con.getResponseCode();
        System.out.println("GET Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));
            StringBuilder sb = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }

            System.out.println("JSON Availability received :: " + sb);
            JSONObject obj = new JSONObject(sb.toString());

            getActivity().runOnUiThread(() -> {
                try {
                    attendance.setText(String.valueOf( obj.get("attendance")) );
                    unavailability.setText(String.valueOf( obj.get("unavailability")) );
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        }

        con.disconnect();
    }

}