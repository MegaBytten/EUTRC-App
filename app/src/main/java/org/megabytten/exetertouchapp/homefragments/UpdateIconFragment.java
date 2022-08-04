package org.megabytten.exetertouchapp.homefragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import org.megabytten.exetertouchapp.HomeActivity;
import org.megabytten.exetertouchapp.R;
import org.megabytten.exetertouchapp.SignupActivity;
import org.megabytten.exetertouchapp.VerificationActivity;
import org.megabytten.exetertouchapp.utils.User;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class UpdateIconFragment extends Fragment {

    View view;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_update_icon, container, false);

        ImageView icon1 = view.findViewById(R.id.icon1);
        icon1.setOnClickListener((v) -> {
            User.getInstance().setIcon(1);
            Thread updateDBIcon = new Thread(() -> {
                try {
                    pushIconChange(1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            updateDBIcon.start();
        });

        ImageView icon2 = view.findViewById(R.id.icon2);
        icon2.setOnClickListener((v) -> {
            User.getInstance().setIcon(2);
            Thread updateDBIcon = new Thread(() -> {
                try {
                    pushIconChange(2);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            updateDBIcon.start();
        });

        ImageView icon3 = view.findViewById(R.id.icon3);
        icon3.setOnClickListener((v) -> {
            User.getInstance().setIcon(3);
            Thread updateDBIcon = new Thread(() -> {
                try {
                    pushIconChange(3);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            updateDBIcon.start();
        });

        ImageView icon4 = view.findViewById(R.id.icon4);
        icon4.setOnClickListener((v) -> {
            User.getInstance().setIcon(4);
            Thread updateDBIcon = new Thread(() -> {
                try {
                    pushIconChange(4);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            updateDBIcon.start();
        });

        return view;
    }

    private void pushIconChange(int icon) throws IOException {
        //establish variables
        String charset = java.nio.charset.StandardCharsets.UTF_8.name();
        String email = User.getInstance().getEmail();
        String password = User.getInstance().getPassword();

        URL obj = new URL("http://megabytten.org/eutrcapp/user/update");

        //sets the encoded query
        String query = String.format("email=%s&password=%s&icon=%s"
                , URLEncoder.encode(email, charset)
                , URLEncoder.encode(password, charset)
                , URLEncoder.encode(String.valueOf(icon), charset));
        System.out.println("query = " + query);

        byte[] postData = query.getBytes(StandardCharsets.UTF_8 );
        int postDataLength = postData.length;

        //establishes HTTP connection with URL via the URL object, "obj"
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setConnectTimeout(5000);
        con.setRequestProperty("attribute", "icon"); // SUPER IMPORTANT !! this will allow us to use the same link for numerous updates

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
            System.out.println("Successfully update user's icon.");
            getActivity().runOnUiThread(()-> {
                Toast.makeText(getContext(), "Successfully updated icon.", Toast.LENGTH_SHORT).show();
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                HomeActivity.replaceFragmentExternal(fragmentTransaction, ProfileFragment.getInstance());
            });

        } else {
            System.out.println("ERROR PUSHING ICON UPDATE!");
        }
    }

}