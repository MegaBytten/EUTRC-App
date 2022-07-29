package org.megabytten.exetertouchapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.google.android.material.navigation.NavigationBarItemView;

import org.megabytten.exetertouchapp.databinding.ActivityHomeBinding;
import org.megabytten.exetertouchapp.homefragments.EventsFragment;
import org.megabytten.exetertouchapp.homefragments.HomeFragment;
import org.megabytten.exetertouchapp.homefragments.ProfileFragment;

public class HomeActivity extends AppCompatActivity {

    ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this section listens to the navigation bar and sets an ItemSelectedListener
        //it uses a switch case to cycle and find which one was selected, and then replaces
        //the frame above the nav bar via the replaceFragment() method
        //sidenote: not fully sure what binding is, I think it is to do with setting a partial view
        // and working with fragments
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            System.out.println("Registered on button click! Id: " + item.getItemId());
            switch (item.getItemId()){
                case R.id.homeNavCalendarButton:
                    replaceFragment(new EventsFragment());
                    System.out.println("Replaced fragment with new EventsFragment");
                    break;
                case R.id.homeNavHomeButton:
                    replaceFragment(new HomeFragment());
                    System.out.println("Replaced fragment with new HomeFragment");
                    break;
                case R.id.homeNavProfileButton:
                    replaceFragment(new ProfileFragment());
                    System.out.println("Replaced fragment with new ProfileFragment");
                    break;
            }

            return true;
        });

        // these 3 lines sets the home button as selected and loads homeFragment when the user loads in
        replaceFragment(new HomeFragment());
        NavigationBarItemView homeNavButton = findViewById(R.id.homeNavHomeButton);
        homeNavButton.setSelected(true);
    }

    private void replaceFragment(Fragment fragment){
        /*
        This function works to switch the frame (fragment) above the navigation bar
        when buttons on the navigation bar are tapped
         */
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainerView, fragment.getClass(), null);
        fragmentTransaction.setReorderingAllowed(true);
        fragmentTransaction.addToBackStack("BackStack");
        fragmentTransaction.commit();
    }

}