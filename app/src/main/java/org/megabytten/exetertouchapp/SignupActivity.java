package org.megabytten.exetertouchapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
    }

    public void backButtonAction(View view){
        Intent intent = new Intent(this, LauncherActivity.class);
        startActivity(intent);
    }

    public void submitButtonAction(View view){
        /*
        This function describes the behaviour of the "Submit" button on the signup activity page
        When the button is clicked, it firstly checks whether the provided email address is
        registered at @exeter.ac.uk via the "confirmExeterEmail(String) method
        If the email is registered at Exeter, it assigns the input to a String
        All other data provided is saved as strings, which is used to create a new User() object
        The user is then uploaded to the MYSQL database via the pushUserToDatabase() method
         */

        //checking and saving Email as "email"

        String email;
        TextView emailAddressInput = findViewById(R.id.emailAddressInput);
        if (confirmExeterEmail (emailAddressInput.getText().toString()) ){
            email = emailAddressInput.getText().toString();
        } else {
            //Email does not match Exeter email
            Toast.makeText(this, "Email is not a @exeter.ac.uk address!", Toast.LENGTH_LONG).show();
            backButtonAction(view);
            return;
        }

        //saving firstName, lastName, and phoneNumber
        TextView firstNameInput = findViewById(R.id.firstNameInput);
        String firstName = firstNameInput.getText().toString();

        TextView lastNameInput = findViewById(R.id.lastNameInput);
        String lastName = lastNameInput.getText().toString();

        TextView phoneNumberInput = findViewById(R.id.phoneNumberInput);
        String phoneNumber = phoneNumberInput.getText().toString();

        //Checking if passwords match and then saving it to password
        TextView passwordInput1 = findViewById(R.id.createPasswordInput);
        TextView passwordInput2 = findViewById(R.id.confirmPasswordInput);
        String passwordAttempt1 = passwordInput1.getText().toString();
        String passwordAttempt2 = passwordInput2.getText().toString();

        if (!passwordAttempt1.equals(passwordAttempt2)){
            //Passwords do not match
            Toast.makeText(this, "Passwords do not match. Try again.", Toast.LENGTH_LONG).show();
            return;
        }

        User.createInstance(email, firstName, lastName, phoneNumber, passwordAttempt1);

        Thread newUserThread = new Thread(() -> { //this is syntax for a lambda function!
            System.out.println("New Thread launched.");
            try {
                sendPOST(User.getInstance());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        //attempt to send Post req to server
        newUserThread.start();

    }

    private boolean confirmExeterEmail(String email){
        /*
        This function checks to see if the provided email String contains @exeter.ac.uk
         */
        return email.contains("@exeter.ac.uk");
    }

    private void sendPOST(User user) throws IOException {

        //establish variables
        String charset = java.nio.charset.StandardCharsets.UTF_8.name();
        String email = user.getEmail();
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        String phoneNumber = user.getPhoneNumber();
        String password = user.getPassword();
        URL obj = new URL("http://megabytten.org/eutrcapp/signup");

        //sets the encoded query
        String query = String.format("email=%s&firstName=%s&lastName=%s&phoneNumber=%s&password=%s"
                , URLEncoder.encode(email, charset)
                , URLEncoder.encode(firstName, charset)
                , URLEncoder.encode(lastName, charset)
                , URLEncoder.encode(phoneNumber, charset)
                , URLEncoder.encode(password, charset));
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
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print message from server
            String serverMessage = response.toString();
            System.out.println(serverMessage);

            if (serverMessage.equalsIgnoreCase("added")){

                Intent intent = new Intent(SignupActivity.this, VerificationActivity.class);
                startActivity(intent);
                finish();

            } else if (serverMessage.equalsIgnoreCase("exists")){
                //Server has checked database and sees user already exists!
                //launch toast that alerts user
                runOnUiThread(() -> Toast.makeText(SignupActivity.this,
                        "Email Already exists! Try logging in instead.",
                        Toast.LENGTH_LONG).show());
            }
        } else {
            System.out.println("POST request not worked");
        }
    }


}