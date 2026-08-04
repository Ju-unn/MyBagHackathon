package com.example.mybaghackathon;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.mybaghackathon.ui.archive.TripArchiveFragment;
import com.example.mybaghackathon.ui.home.HomeFragment;
import com.example.mybaghackathon.ui.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Hosts the three tab destinations (S03 Home / S13 Archive / S14 Profile)
 * behind a single BottomNavigationView, matching the README §6 Activity
 * map: everything else is a standalone Activity reached from here.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView nav = findViewById(R.id.mainBottomNav);

        if (savedInstanceState == null) {
            showFragment(new HomeFragment());
        }

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                showFragment(new HomeFragment());
                return true;
            } else if (id == R.id.nav_trips) {
                showFragment(new TripArchiveFragment());
                return true;
            } else if (id == R.id.nav_profile) {
                showFragment(new ProfileFragment());
                return true;
            }
            return false;
        });
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainFragmentContainer, fragment)
                .commit();
    }
}
