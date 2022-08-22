package org.megabytten.touchapp.homefragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import org.megabytten.touchapp.HomeActivity;
import org.megabytten.touchapp.R;
import org.megabytten.touchapp.utils.User;

public class ProfileFragment extends Fragment {

    private static ProfileFragment profileFragment;

    View view;
    boolean isPswdHidden = true;

    public static ProfileFragment getInstance(){
        if (profileFragment == null){
            System.out.println("Initialising profileFragment");
            profileFragment = new ProfileFragment();
        }
        return profileFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        //update all UI features
        TextView passwordTxt = view.findViewById(R.id.passwordTxt);
        passwordTxt.setText(User.getInstance().getPassword());

        TextView userTxt = view.findViewById(R.id.userTxt);
        if (User.getInstance().isCoach()){
            userTxt.setText("Coach Profile");
        } else {
            userTxt.setText("Player Profile");
        }

        ImageView imageView = view.findViewById(R.id.imageView);
        try {
            if (User.getInstance().getIcon() == 1){
                imageView.setImageResource(R.drawable.user_icon_1);
            } else if (User.getInstance().getIcon() == 2){
                imageView.setImageResource(R.drawable.user_icon_2);
            } else if (User.getInstance().getIcon() == 3){
                imageView.setImageResource(R.drawable.user_icon_3);
            }  else if (User.getInstance().getIcon() == 4){
                imageView.setImageResource(R.drawable.user_icon_4);
            } else {
                throw new Exception("No image for that icon int!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        imageView.setOnClickListener((v) -> {
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            HomeActivity.replaceFragmentExternal(fragmentTransaction, new UpdateIconFragment());
        });

        TextView nameTxt = view.findViewById(R.id.nameTxt);
        nameTxt.setText(User.getInstance().getFirstName() + " " + User.getInstance().getLastName());

        TextView emailTxt = view.findViewById(R.id.emailTxt);
        emailTxt.setText(User.getInstance().getEmail());

        TextView phoneNumberTxt = view.findViewById(R.id.phoneNumberTxt);
        phoneNumberTxt.setText(User.getInstance().getPhoneNumber());

        Button viewPswdBtn = view.findViewById(R.id.viewPswdBtn);
        viewPswdBtn.setOnClickListener((v) -> {
            TextView passwordTitle = view.findViewById(R.id.passwordTitle);
            if (isPswdHidden){
                passwordTitle.setVisibility(View.VISIBLE);
                passwordTxt.setVisibility(View.VISIBLE);
                viewPswdBtn.setText("Hide Password");
                isPswdHidden = false;
            } else {
                passwordTitle.setVisibility(View.GONE);
                passwordTxt.setVisibility(View.GONE);
                viewPswdBtn.setText("View Password");
                isPswdHidden = true;
            }
        });

        Button editDetailsBtn = view.findViewById(R.id.editDetails);
        editDetailsBtn.setOnClickListener((v) -> {
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            HomeActivity.replaceFragmentExternal(fragmentTransaction, new UpdateDetailsFragment());
        });

        TextView deleteAccount = view.findViewById(R.id.deleteAccount);
        deleteAccount.setOnClickListener((v) -> {
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            HomeActivity.replaceFragmentExternal(fragmentTransaction, ConfirmActionFragment.createConfirmAction_for_DeleteAccount());
        });

        return view;
    }
}