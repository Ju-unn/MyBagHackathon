package com.example.mybaghackathon.ui.checklist;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.databinding.ActivityChecklistBinding;
import com.example.mybaghackathon.ui.EdgeToEdgeUtil;
import com.google.android.material.tabs.TabLayout;

/**
 * S10-12 · 체크리스트 — 탭(공용 리스트 / 내 목록 / 분담 현황)을 수동으로
 * 프래그먼트 교체하는 방식으로 구현.
 *
 * 기능: TabLayout 탭 선택에 따라 3개의 체크리스트 프래그먼트(공용/내 목록/
 * 분담 현황) 중 하나를 갈아끼워주는 컨테이너 액티비티.
 */
public class ChecklistActivity extends AppCompatActivity {

    private ActivityChecklistBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChecklistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeUtil.applySystemBarPadding(this, binding.getRoot());

        TabLayout tabs = binding.checklistTabs;

        if (savedInstanceState == null) {
            showFragment(new ChecklistCommonFragment());
        }

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 1:
                        showFragment(new ChecklistMineFragment());
                        break;
                    case 2:
                        showFragment(new ChecklistAssignmentFragment());
                        break;
                    default:
                        showFragment(new ChecklistCommonFragment());
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.checklistFragmentContainer, fragment)
                .commit();
    }
}
