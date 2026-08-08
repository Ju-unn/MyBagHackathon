package com.example.mybaghackathon.ui.profile;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.app.MyBagApplication;
import com.example.mybaghackathon.data.repository.DefaultItemRepository;
import com.example.mybaghackathon.databinding.ActivityProfileItemsBinding;
import com.example.mybaghackathon.model.UserDefaultItem;
import com.example.mybaghackathon.ui.EdgeToEdgeUtil;
import com.example.mybaghackathon.ui.overlay.AddItemSheet;
import com.example.mybaghackathon.ui.overlay.EditItemSheet;

import java.util.List;

/**
 * S15 · 내 기본 물품 전체보기 — ProfileFragment의 "전체보기"에서 진입하는 목록 화면.
 *
 * 기능: ProfileItemsPresenter가 불러온 전체 목록을 RecyclerView로 보여주고,
 * 행을 누르면 EditItemSheet로 이름 변경/삭제를, 하단 버튼으로 AddItemSheet를
 * 띄워 새 항목을 추가한다.
 */
public class ProfileItemsActivity extends AppCompatActivity implements ProfileItemsContract.View {

    private ActivityProfileItemsBinding binding;
    private ProfileItemsContract.Presenter presenter;
    private ProfileItemAdapter itemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileItemsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeUtil.applySystemBarPadding(this, binding.getRoot());

        TextView title = binding.profileItemsTopBar.topAppBarCompactTitle;
        title.setText(R.string.profile_items_title);
        binding.profileItemsTopBar.topAppBarBack.setOnClickListener(v -> finish());

        DefaultItemRepository defaultItemRepository =
                ((MyBagApplication) getApplication()).getAppContainer().defaultItemRepository;
        presenter = new ProfileItemsPresenter(this, defaultItemRepository);

        itemAdapter = new ProfileItemAdapter((position, item) -> {
            EditItemSheet sheet = EditItemSheet.newInstance(item.getItemName());
            sheet.setOnItemEditedListener(new EditItemSheet.OnItemEditedListener() {
                @Override
                public void onItemRenamed(String newLabel) {
                    presenter.renameItem(item.getDefaultItemId(), newLabel);
                }

                @Override
                public void onItemDeleted() {
                    presenter.deleteItem(item.getDefaultItemId());
                }
            });
            sheet.show(getSupportFragmentManager(), "edit_item");
        });
        binding.profileItemsRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.profileItemsRecycler.setAdapter(itemAdapter);

        binding.profileItemsAddButton.setOnClickListener(v -> {
            AddItemSheet sheet = new AddItemSheet();
            sheet.setOnItemAddedListener((label, priority) -> presenter.addItem(label, priority));
            sheet.show(getSupportFragmentManager(), "add_item");
        });

        presenter.loadItems();
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        binding = null;
        super.onDestroy();
    }

    // ===== ProfileItemsContract.View =====

    @Override
    public void showItems(List<UserDefaultItem> items) {
        itemAdapter.submitList(items);
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
