package com.example.mybaghackathon.ui.profile;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.databinding.ActivityProfileItemsBinding;
import com.example.mybaghackathon.ui.EdgeToEdgeUtil;
import com.example.mybaghackathon.ui.overlay.AddItemSheet;
import com.example.mybaghackathon.ui.overlay.EditItemSheet;

import java.util.List;

/**
 * S15 · 내 기본 물품 전체보기 — ProfileFragment의 "전체보기"에서 진입하는 목록 화면.
 *
 * 기능: DefaultItemStore의 전체 목록을 보여주고, 행을 누르면 EditItemSheet로
 * 이름 변경/삭제를, 하단 버튼으로 AddItemSheet를 띄워 새 항목을 추가한다.
 */
public class ProfileItemsActivity extends AppCompatActivity {

    private ActivityProfileItemsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileItemsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeUtil.applySystemBarPadding(this, binding.getRoot());

        TextView title = binding.profileItemsTopBar.topAppBarCompactTitle;
        title.setText(R.string.profile_items_title);
        binding.profileItemsTopBar.topAppBarBack.setOnClickListener(v -> finish());

        renderList();

        binding.profileItemsAddButton.setOnClickListener(v -> {
            AddItemSheet sheet = new AddItemSheet();
            sheet.setOnItemAddedListener((label, priority) -> {
                DefaultItemStore.add(label);
                renderList();
            });
            sheet.show(getSupportFragmentManager(), "add_item");
        });
    }

    private void renderList() {
        LinearLayout list = binding.profileItemsList;
        list.removeAllViews();
        List<String> items = DefaultItemStore.getItems();
        for (int i = 0; i < items.size(); i++) {
            String label = items.get(i);
            int index = i;
            list.addView(ProfileItemRowViews.create(this, label, v -> {
                EditItemSheet sheet = EditItemSheet.newInstance(label);
                sheet.setOnItemEditedListener(new EditItemSheet.OnItemEditedListener() {
                    @Override
                    public void onItemRenamed(String newLabel) {
                        DefaultItemStore.rename(index, newLabel);
                        renderList();
                    }

                    @Override
                    public void onItemDeleted() {
                        DefaultItemStore.removeAt(index);
                        renderList();
                    }
                });
                sheet.show(getSupportFragmentManager(), "edit_item");
            }));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
