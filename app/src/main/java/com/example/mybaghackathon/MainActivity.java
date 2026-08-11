package com.example.mybaghackathon;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.mybaghackathon.ui.EdgeToEdgeUtil;
import com.example.mybaghackathon.ui.archive.TripArchiveFragment;
import com.example.mybaghackathon.ui.home.HomeFragment;
import com.example.mybaghackathon.ui.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * BottomNavigationView로 S03 홈 / S13 공용여행 / S14 프로필 탭을 전환하는
 * 컨테이너 액티비티. 탭마다 프래그먼트 인스턴스를 하나만 유지하고 add + hide/show로
 * 전환한다 — 매번 replace()로 새로 만들면 전환할 때마다 화면이 다시 그려지며
 * 깜빡이고, 데이터도 매번 다시 불러오게 된다. 기본 진입 탭은 홈(S03)이다.
 */
public class MainActivity extends AppCompatActivity {

    private static final long EXIT_CONFIRM_WINDOW_MS = 2000L;

    private Fragment activeFragment;
    private long lastBackPressedAt = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EdgeToEdgeUtil.applySystemBarPadding(this, findViewById(R.id.main), findViewById(R.id.mainBottomNav));

        BottomNavigationView bottomNav = findViewById(R.id.mainBottomNav);
        bottomNav.setOnItemSelectedListener(this::onNavItemSelected);

        // 앱의 루트 화면이라 뒤로가기 누르면 바로 종료됨 — 실수로 나가는 걸 막기 위해
        // 2초 안에 한 번 더 눌러야 진짜 종료되게 처리
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                long now = System.currentTimeMillis();
                if (now - lastBackPressedAt < EXIT_CONFIRM_WINDOW_MS) {
                    finish();
                } else {
                    lastBackPressedAt = now;
                    Toast.makeText(MainActivity.this, R.string.main_press_back_again_to_exit, Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (savedInstanceState == null) {
            showFragment(R.id.nav_home, HomeFragment::new);
        } else {
            activeFragment = getSupportFragmentManager().findFragmentByTag(tagFor(bottomNav.getSelectedItemId()));
        }
    }

    private boolean onNavItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            showFragment(id, HomeFragment::new);
            return true;
        } else if (id == R.id.nav_trips) {
            showFragment(id, TripArchiveFragment::new);
            return true;
        } else if (id == R.id.nav_profile) {
            showFragment(id, ProfileFragment::new);
            return true;
        }
        return false;
    }

    private void showFragment(int navId, FragmentFactory factory) {
        String tag = tagFor(navId);
        FragmentManager fm = getSupportFragmentManager();
        Fragment target = fm.findFragmentByTag(tag);

        FragmentTransaction tx = fm.beginTransaction();
        if (target == null) {
            target = factory.create();
            tx.add(R.id.mainFragmentContainer, target, tag);
        }
        if (activeFragment != null && activeFragment != target) {
            tx.hide(activeFragment);
        }
        tx.show(target);
        tx.commit();
        activeFragment = target;
    }

    private String tagFor(int navId) {
        return "nav_fragment_" + navId;
    }

    private interface FragmentFactory {
        Fragment create();
    }
}
