package com.example.mybaghackathon.ui.checklist;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.mybaghackathon.R;
import com.google.android.material.tabs.TabLayout;

/** S10-12 · Checklist — tabbed (공용 리스트 / 내 목록 / 분담 현황) via manual fragment swap. */
public class ChecklistActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);

        TextView title = findViewById(R.id.topAppBarCompactTitle);
        title.setText(R.string.checklist_title);
        findViewById(R.id.topAppBarBack).setOnClickListener(v -> finish());

        TabLayout tabs = findViewById(R.id.checklistTabs);

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
