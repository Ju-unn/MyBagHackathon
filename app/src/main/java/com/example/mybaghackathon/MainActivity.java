package com.example.mybaghackathon;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.mybaghackathon.ui.EdgeToEdgeUtil;
import com.example.mybaghackathon.ui.archive.TripArchiveFragment;
import com.example.mybaghackathon.ui.home.HomeFragment;
import com.example.mybaghackathon.ui.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * BottomNavigationView로 S03 홈 / S13 공용여행 / S14 프로필 탭을 전환하는
 * 컨테이너 액티비티. 기본 진입 탭은 홈(S03)이다.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EdgeToEdgeUtil.applySystemBarPadding(this, findViewById(R.id.main), findViewById(R.id.mainBottomNav));

        BottomNavigationView bottomNav = findViewById(R.id.mainBottomNav);
        bottomNav.setOnItemSelectedListener(this::onNavItemSelected);

        if (savedInstanceState == null) {
            showFragment(new HomeFragment());
        }
    }

    private boolean onNavItemSelected(@NonNull android.view.MenuItem item) {
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
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainFragmentContainer, fragment)
                .commit();
    }
}
