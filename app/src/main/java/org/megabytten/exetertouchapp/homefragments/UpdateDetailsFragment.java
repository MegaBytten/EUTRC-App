package org.megabytten.exetertouchapp.homefragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

public class UpdateDetailsFragment extends Fragment {

    View view;
    EditText firstNameInput, lastNameInput, emailInput, phoneNumberInput;
    String firstName, lastName, email, phoneNumber;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_update_details, container, false);

        firstNameInput = view.findViewById(R.id.firstNameInput);
        lastNameInput = view.findViewById(R.id.lastNameInput);
        emailInput = view.findViewById(R.id.emailInput);
        phoneNumberInput = view.findViewById(R.id.phoneNumberInput);

        Button changePswdBtn = view.findViewById(R.id.changePswdBtn);
        changePswdBtn.setOnClickListener((v) -> {
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            HomeActivity.replaceFragmentExternal(fragmentTransaction, new UpdatePasswordFragment());
        });

        Button submitBtn = view.findViewById(R.id.submitBtn);
        submitBtn.setOnClickListener((v -> {
            if (!checkDetails()){
                System.out.println("User attempted to change to incorrect details.");
                return;
            }
            Thread pushData = new Thread(() -> {
                try {
                    pushUserUpdates();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            pushData.start();
        }));

        Button backBtn = view.findViewById(R.id.backBtn);
        backBtn.setOnClickListener((v -> {
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            HomeActivity.replaceFragmentExternal(fragmentTransaction, ProfileFragment.getInstance());
        }));

        return view;
    }

    private boolean checkDetails(){
        firstName = firstNameInput.getText().toString();
        if (firstName.equalsIgnoreCase("null") || firstName.equalsIgnoreCase("")){
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "First Name cannot be null!", Toast.LENGTH_LONG).show();
            });
            return false;
        }

        lastName = lastNameInput.getText().toString();
        if (lastName.equalsIgnoreCase("null") || lastName.equalsIgnoreCase("")){
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Last Name cannot be null!", Toast.LENGTH_LONG).show();
            });
            return false;
        }

        email = emailInput.getText().toString();
        if (email.equalsIgnoreCase("null") || email.equalsIgnoreCase("")){
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Email cannot be null!", Toast.LENGTH_LONG).show();
            });
            return false;
        } else if (!email.contains("@exeter.ac.uk")){
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Email must be @exeter.ac.uk!", Toast.LENGTH_LONG).show();
            });
            return false;
        }

        phoneNumber = phoneNumberInput.getText().toString();
        if (phoneNumber.equalsIgnoreCase("null") || phoneNumber.equalsIgnoreCase("")){
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Phone Number cannot be null!", Toast.LENGTH_LONG).show();
            });
            return false;
        }

        return true;
    }

    private void pushUserUpdates() throws IOException {
        //establish variables
        String charset = java.nio.charset.StandardCharsets.UTF_8.name();
        String password = User.getInstance().getPassword();

        URL obj = new URL("http://megabytten.org/eutrcapp/user/update");

        //sets the encoded query
        String query = String.format("email=%s&newEmail=%s&password=%s&firstName=%s&lastName=%s&phoneNumber=%s"
                , URLEncoder.encode(User.getInstance().getEmail(), charset)
                , URLEncoder.encode(email, charset)
                , URLEncoder.encode(password, charset)
                , URLEncoder.encode(firstName, charset)
                , URLEncoder.encode(lastName, charset)
                , URLEncoder.encode(phoneNumber, charset));
        System.out.println("query = " + query);

        byte[] postData = query.getBytes(StandardCharsets.UTF_8 );
        int postDataLength = postData.length;

        //establishes HTTP connection with URL via the URL object, "obj"
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setConnectTimeout(5000);
        con.setRequestProperty("attribute", "all"); // SUPER IMPORTANT !! this will allow us to use the same link for numerous updates

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
            getActivity().runOnUiThread(()-> {
                Toast.makeText(getContext(), "Successfully updated details.", Toast.LENGTH_SHORT).show();
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                HomeActivity.replaceFragmentExternal(fragmentTransaction, ProfileFragment.getInstance());
            });

            User user = User.getInstance();
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPhoneNumber(phoneNumber);
            System.out.println("Successfully pushed User's Details Update, and updated User Object!");
        } else {
            System.out.println("ERROR PUSHING USER DETAILS UPDATE!");
        }
    }
}