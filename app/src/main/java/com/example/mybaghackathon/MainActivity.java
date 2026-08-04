package com.example.mybaghackathon;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mybaghackathon.ui.EdgeToEdgeUtil;
import com.example.mybaghackathon.ui.archive.TripArchiveFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EdgeToEdgeUtil.applySystemBarPadding(this, findViewById(R.id.main));

        // TODO(temp-test): 탭 전환 로직 붙기 전까지 임시로 Archive 탭을 바로 띄움
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mainFragmentContainer, new TripArchiveFragment())
                    .commit();
        }
    }
}
