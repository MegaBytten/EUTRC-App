package org.megabytten.touchapp.homefragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.megabytten.touchapp.HomeActivity;
import org.megabytten.touchapp.R;
import org.megabytten.touchapp.utils.User;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class UpdatePasswordFragment extends Fragment {

    View view;

    EditText currentPasswordInput, newPassword1, newPassword2;
    String password1, password2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_update_password, container, false);

        currentPasswordInput = view.findViewById(R.id.currentPasswordInput);
        newPassword1 = view.findViewById(R.id.newPassword1);
        newPassword2 = view.findViewById(R.id.newPassword2);

        Button backBtn = view.findViewById(R.id.backBtn);
        backBtn.setOnClickListener((v)-> {
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            HomeActivity.replaceFragmentExternal(fragmentTransaction, new UpdateDetailsFragment());
        });

        Button submitBtn = view.findViewById(R.id.submitBtn);
        submitBtn.setOnClickListener((v) -> {
            if (!passwordsMatch()){
                System.out.println("Passwords do not match for User password change!");
                Toast.makeText(getContext(), "New Passwords do not match.", Toast.LENGTH_LONG).show();
            }
            Thread pushUpdate = new Thread(()->{
                try {
                    pushPasswordUpdate();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            pushUpdate.start();
        });


        return view;
    }

    private boolean passwordsMatch(){
        password1 = newPassword1.getText().toString();
        password2 = newPassword2.getText().toString();

        if (password1.equals(password2)){
            return true;
        }
        return false;
    }

    private void pushPasswordUpdate() throws IOException {
        String currentPassword = currentPasswordInput.getText().toString();

        String charset = java.nio.charset.StandardCharsets.UTF_8.name();

        URL obj = new URL("http://megabytten.org/eutrcapp/user/update");

        //sets the encoded query
        String query = String.format("email=%s&password=%s&newPassword=%s"
                , URLEncoder.encode(User.getInstance().getEmail(), charset)
                , URLEncoder.encode(currentPassword, charset)
                , URLEncoder.encode(password1, charset));
        System.out.println("query = " + query);

        byte[] postData = query.getBytes(StandardCharsets.UTF_8 );
        int postDataLength = postData.length;

        //establishes HTTP connection with URL via the URL object, "obj"
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setConnectTimeout(5000);
        con.setRequestProperty("attribute", "password"); // SUPER IMPORTANT !! this will allow us to use the same link for numerous updates

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
                Toast.makeText(getContext(), "Successfully updated password.", Toast.LENGTH_SHORT).show();
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                HomeActivity.replaceFragmentExternal(fragmentTransaction, ProfileFragment.getInstance());
            });

            User user = User.getInstance();
            user.setPassword(password1);
            System.out.println("Successfully pushed User's Password Update, and updated User.password!");
        } else {
            getActivity().runOnUiThread(()-> {
                Toast.makeText(getContext(), "Current Password Incorrect.", Toast.LENGTH_SHORT).show();
            });
            System.out.println("ERROR PUSHING USER PASSWORD UPDATE!");
        }
    }

}