package org.megabytten.exetertouchapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class VerificationActivity extends AppCompatActivity {

    private String userEmail;
    Button sendVerifEmailBtn, submitBtn;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        submitBtn = findViewById(R.id.submitBtn);
        sendVerifEmailBtn = findViewById(R.id.button);
        TextView explanationTxt = findViewById(R.id.textView);

        user = User.getInstance();
        userEmail = user.getEmail();
        explanationTxt.setText("Email Address: " + userEmail + " has not been Verified. You must verify email before continuing!");

        //adds click listener for invisible Submit Button
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText editTextNumberPassword = findViewById(R.id.editTextNumberPassword);
                String userVerifCode = editTextNumberPassword.getText().toString();

                if (userVerifCode.length() >= 10){
                    Toast.makeText(VerificationActivity.this
                            , "Incorrect Verification code."
                            , Toast.LENGTH_SHORT).show();
                    return;
                }

                Thread newVerifThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("New Thread launched.");
                        try {
                            onSubmitBtn();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                newVerifThread.start();
            }
        });

        sendVerifEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread newVerifThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("New Thread launched.");
                        try {
                            onVerifButton();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                //attempt to send Post req to server
                newVerifThread.start();
            }
        });
    }

    private void onVerifButton() throws IOException {
        //establish variables
        String charset = java.nio.charset.StandardCharsets.UTF_8.name();
        URL obj = new URL("http://megabytten.org/eutrcapp/verfbot");

        //sets the encoded query
        String query = String.format("email=%s&password=%s"
                , URLEncoder.encode(userEmail, charset)
                , URLEncoder.encode(user.getPassword(), charset));
        System.out.println("query = " + query);

        byte[] postData = query.getBytes(StandardCharsets.UTF_8);
        int postDataLength = postData.length;

        //establishes HTTP connection with URL via the URL object, "obj"
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");

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

        if (responseCode == HttpURLConnection.HTTP_OK) { //success
            System.out.println("Successfully sent verification email to email: " + userEmail);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(VerificationActivity.this,
                            "Verification Email Sent!",
                            Toast.LENGTH_LONG).show();
                }
            });

            updateUIVerif();


        } else {
            //Server returned status 999 or 998 instead of 200
            //Meaning User password or email did not match database, although JUST signing up with those details
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(VerificationActivity.this,
                            //This else {} clause may result in error 001
                            //Caused when User.getInstance.getPassword() query to megabytten.org/eutrcapp/signup/verification does not work!
                            "Please contact admin (Error:001)",
                            Toast.LENGTH_LONG).show();
                }
            });
        }

    }

    private void onSubmitBtn() throws IOException {
        EditText editTextNumberPassword = findViewById(R.id.editTextNumberPassword);
        String userVerifCode = editTextNumberPassword.getText().toString();

        String charset = java.nio.charset.StandardCharsets.UTF_8.name();
        URL obj = new URL("http://megabytten.org/eutrcapp/verify");

        //sets the encoded query
        String query = String.format("email=%s&verifCode=%s"
                , URLEncoder.encode(userEmail, charset)
                , URLEncoder.encode(userVerifCode, charset));
        System.out.println("query = " + query);

        byte[] postData = query.getBytes(StandardCharsets.UTF_8);
        int postDataLength = postData.length;

        //establishes HTTP connection with URL via the URL object, "obj"
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");

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

        if (responseCode == HttpURLConnection.HTTP_OK) { //success
            System.out.println("User successfully verified!");

            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(VerificationActivity.this,
                            "Verification successful.",
                            Toast.LENGTH_LONG).show();
                }
            });

            Intent intent = new Intent(VerificationActivity.this, HomeActivity.class);
            startActivity(intent);
        } else if (responseCode == 998){
            //Verification codes do not match!
            System.out.println("Verification codes do not match!");
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(VerificationActivity.this,
                            "Incorrect Verification Code",
                            Toast.LENGTH_LONG).show();
                }
            });
        } else {
            //Server returned status 999 instead of 200
            //Meaning User verification code == null
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(VerificationActivity.this,
                            //This else {} clause may result in error 002
                            //Caused when User.getInstance.getPassword() query to megabytten.org/eutrcapp/signup/verification does not work!
                            "User already verified. Returning to log in.",
                            Toast.LENGTH_LONG).show();
                }
            });
            Intent intent = new Intent(VerificationActivity.this, LauncherActivity.class);
            startActivity(intent);
        }

    }

    public void alreadyHaveVerifAction(View view){
        updateUIVerif();
    }

    private void updateUIVerif(){


        runOnUiThread(new Runnable() {
            public void run() {
                TextView verifCodeText = findViewById(R.id.verifCodeText);
                EditText editTextNumberPassword = findViewById(R.id.editTextNumberPassword);
                TextView spamWarningText = findViewById(R.id.spamWarningText);
                Button changeUIBtn = findViewById(R.id.changeUIBtn);

                sendVerifEmailBtn.setVisibility(View.GONE);
                changeUIBtn.setVisibility(View.GONE);
                verifCodeText.setVisibility(View.VISIBLE);
                editTextNumberPassword.setVisibility(View.VISIBLE);
                submitBtn.setVisibility(View.VISIBLE);
                spamWarningText.setVisibility(View.VISIBLE);
            }
        });
    }

    public void megabyttenVerificationLink(View view){
        Uri uri = Uri.parse("http://megabytten.org/eutrcapp");
        updateUIVerif();

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);

    }

}