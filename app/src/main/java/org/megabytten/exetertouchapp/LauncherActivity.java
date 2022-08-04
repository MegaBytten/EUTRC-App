package org.megabytten.exetertouchapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.megabytten.exetertouchapp.utils.User;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

    }

    public void megabyttenRedirectAction(View view){
        Uri uri = Uri.parse("http://megabytten.org");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public void signUpPromptAction(View view){
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }

    public void loginButtonAction(View view) throws IOException {
        Thread newVerifThread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("New Thread launched.");
                try {
                    loginButtonWorkerThread();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        //attempt to send Post req to server
        newVerifThread.start();
    }

    private void loginButtonWorkerThread() throws IOException{
        TextView emailInput = findViewById(R.id.emailAddressLoginInput);
        String email = emailInput.getText().toString();

        EditText passwordLoginInput = findViewById(R.id.passwordLoginInput);
        String password = passwordLoginInput.getText().toString();

        String charset = java.nio.charset.StandardCharsets.UTF_8.name();
        URL obj = new URL("http://megabytten.org/eutrcapp/login");

        //sets the encoded query
        String query = String.format("email=%s&password=%s"
                , URLEncoder.encode(email, charset)
                , URLEncoder.encode(password, charset));
        System.out.println("query = " + query);

        byte[] postData = query.getBytes(StandardCharsets.UTF_8);
        int postDataLength = postData.length;

        //establishes HTTP connection with URL via the URL object, "obj"
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setConnectTimeout(10000);

        //post method set up!
        con.setDoOutput(true);
        con.setInstanceFollowRedirects(false);
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setRequestProperty("charset", "utf-8");
        con.setRequestProperty("Content-Length", Integer.toString(postDataLength));
        con.setUseCaches(false);

        DataOutputStream os = new DataOutputStream(con.getOutputStream());
        os.write(query.getBytes(charset));
        os.flush();
        os.close();

        int responseCode = con.getResponseCode();

        System.out.println("POST Response Code :: " + responseCode);

        if (responseCode == 200) { //success
            System.out.println("User successfully logged in!");
            userInit(email, password);
            checkVerification();
        } else if (responseCode == 999) {
            //999 applicable when SQL query check was null
            runOnUiThread(() -> Toast.makeText(LauncherActivity.this, "Email Address not Recognised. Please re-enter Email or Sign up below,",
                    Toast.LENGTH_LONG).show());
        } else {
            //server only sends 998 otherwise, where passwords do not match
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(LauncherActivity.this, "Incorrect Password.",
                            Toast.LENGTH_LONG).show();
                }
            });
            passwordLoginInput.setText("");
        }
    }

    private void userInit (String email, String password) throws IOException {
        String charset = java.nio.charset.StandardCharsets.UTF_8.name();
        URL obj = new URL("http://megabytten.org/eutrcapp/user");

        //sets the encoded query
        String query = String.format("email=%s&password=%s"
                , URLEncoder.encode(email, charset)
                , URLEncoder.encode(password, charset));
        System.out.println("query = " + query);

        byte[] postData = query.getBytes(StandardCharsets.UTF_8);
        int postDataLength = postData.length;

        //establishes HTTP connection with URL via the URL object, "obj"
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setConnectTimeout(10000);

        //post method set up!
        con.setDoOutput(true);
        con.setInstanceFollowRedirects(false);
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setRequestProperty("charset", "utf-8");
        con.setRequestProperty("Content-Length", Integer.toString(postDataLength));
        con.setUseCaches(false);

        DataOutputStream os = new DataOutputStream(con.getOutputStream());
        os.write(query.getBytes(charset));
        os.flush();
        os.close();

        int responseCode = con.getResponseCode();
        System.out.println("POST Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));
            StringBuilder sb = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            System.out.println("TRACE TRACE :: " + sb.toString());
            String[] info=sb.toString().split(";");

            boolean isCoach = false;
            if (info[3].equalsIgnoreCase("true")){
                isCoach = true;
            }

            //User.createInstance(email, firstName, lastName, phoneNumber, password, coach);
            User.createInstance(email, info[0], info[1], info[2], password, isCoach, Integer.parseInt(info[4]));
            System.out.println(User.getInstance().getFirstName() + User.getInstance().getLastName());
        }
    }

    private void checkVerification() throws IOException {
        String email = User.getInstance().getEmail();
        String password = User.getInstance().getPassword();

        String charset = java.nio.charset.StandardCharsets.UTF_8.name();
        URL obj = new URL("http://megabytten.org/eutrcapp/checkverif");

        //sets the encoded query
        String query = String.format("email=%s&password=%s"
                , URLEncoder.encode(email, charset)
                , URLEncoder.encode(password, charset));
        System.out.println("query = " + query);

        byte[] postData = query.getBytes(StandardCharsets.UTF_8);
        int postDataLength = postData.length;

        //establishes HTTP connection with URL via the URL object, "obj"
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setConnectTimeout(10000);

        //post method set up!
        con.setDoOutput(true);
        con.setInstanceFollowRedirects(false);
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setRequestProperty("charset", "utf-8");
        con.setRequestProperty("Content-Length", Integer.toString(postDataLength));
        con.setUseCaches(false);

        DataOutputStream os = new DataOutputStream(con.getOutputStream());
        os.write(query.getBytes(charset));
        os.flush();
        os.close();

        int responseCode = con.getResponseCode();
        System.out.println("POST Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK){
            BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));
            StringBuilder sb = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            System.out.println("TRACE TRACE :: " + sb.toString());

            if (sb.toString().equalsIgnoreCase("true")){
                //verified
                System.out.println("User is verified!");
                Intent intent = new Intent(LauncherActivity.this, HomeActivity.class);
                startActivity(intent);
            } else {
                //not verified
                System.out.println("User is not verified!");
                Intent intent = new Intent(LauncherActivity.this, VerificationActivity.class);
                startActivity(intent);
            }
        }

    }


}