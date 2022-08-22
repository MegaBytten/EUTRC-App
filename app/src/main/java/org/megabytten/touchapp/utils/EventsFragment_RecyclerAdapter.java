package org.megabytten.touchapp.utils;

import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class EventsFragment_RecyclerAdapter extends RecyclerAdapter{
    Training training;

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Training tr = sortedList.get(position);
        holder.trainingTitleTxt.setText("Training #" + tr.getId());
        holder.id.setText(String.valueOf(tr.getId()));
        holder.date.setText(tr.getFormattedDate());
        holder.location.setText(tr.getLocation());
        holder.team.setText(tr.getTeam());
        holder.drills.setText(tr.getDrills());

        holder.available.setOnClickListener((v -> {
            training = tr;
            Thread rsvpThread = new Thread(()->{
                try {
                    rsvpAction(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            rsvpThread.start();
            Toast.makeText(v.getContext(), "Successfully RSVP'd: Available", Toast.LENGTH_SHORT).show();
        }));

        holder.unavailable.setOnClickListener((v -> {
            training = tr;
            Thread rsvpThread = new Thread(()->{
                try {
                    rsvpAction(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            rsvpThread.start();
            Toast.makeText(v.getContext(), "Successfully RSVP'd: Unavailable", Toast.LENGTH_SHORT).show();
        }));



    }

    private void rsvpAction(boolean rsvpYes) throws IOException {
        //establish variables required for POST
        String charset = java.nio.charset.StandardCharsets.UTF_8.name();
        String email = User.getInstance().getEmail();
        String password = User.getInstance().getPassword();
        int id = training.getId();

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
        } else {
            System.out.println("Error sending RSVP Update via EventsFrag.");
        }

    }

}
