package com.example.mybaghackathon.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mybaghackathon.R;
import com.example.mybaghackathon.app.MyBagApplication;
import com.example.mybaghackathon.data.repository.DefaultItemRepository;
import com.example.mybaghackathon.databinding.ActivityProfileItemsBinding;
import com.example.mybaghackathon.model.UserDefaultItem;
import com.example.mybaghackathon.ui.EdgeToEdgeUtil;
import com.example.mybaghackathon.ui.atoms.PriorityDotView;
import com.example.mybaghackathon.ui.overlay.AddItemSheet;
import com.example.mybaghackathon.ui.overlay.EditItemSheet;

import java.util.ArrayList;
import java.util.List;

/**
 * S15 · 내 기본 물품 전체보기 — ProfileFragment의 "전체보기"에서 진입하는 목록 화면.
 *
 * 기능: ProfileItemsPresenter가 불러온 전체 목록을 우선순위(필수/중간/선택) 필터 칩과
 * 카드 형태로 보여주고, 행을 누르면 EditItemSheet로 이름 변경/삭제를, 하단 버튼으로
 * AddItemSheet를 띄워 새 항목을 추가한다.
 */
public class ProfileItemsActivity extends AppCompatActivity implements ProfileItemsContract.View {

    private static final int FILTER_ALL = -1;

    private ActivityProfileItemsBinding binding;
    private ProfileItemsContract.Presenter presenter;
    private List<UserDefaultItem> allItems = new ArrayList<>();
    private int currentFilter = FILTER_ALL;

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

        binding.profileItemsFilterAll.setOnClickListener(v -> selectFilter(FILTER_ALL));
        binding.profileItemsFilterHigh.setOnClickListener(v -> selectFilter(PriorityLevels.HIGH));
        binding.profileItemsFilterMid.setOnClickListener(v -> selectFilter(PriorityLevels.MID));
        binding.profileItemsFilterLow.setOnClickListener(v -> selectFilter(PriorityLevels.LOW));

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
        allItems = items;
        renderCurrentFilter();
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showEditSheet(UserDefaultItem item) {
        EditItemSheet sheet = EditItemSheet.newInstance(
                item.getItemName(), PriorityLevels.fromApiValue(item.getPriority()));
        sheet.setOnItemEditedListener(new EditItemSheet.OnItemEditedListener() {
            @Override
            public void onItemRenamed(String newLabel, int priorityLevel) {
                presenter.renameItem(item.getDefaultItemId(), newLabel, priorityLevel);
            }

            @Override
            public void onItemDeleted() {
                presenter.deleteItem(item.getDefaultItemId());
            }
        });
        sheet.show(getSupportFragmentManager(), "edit_item");
    }

    private void selectFilter(int priorityLevel) {
        currentFilter = priorityLevel;
        binding.profileItemsFilterAll.setActive(priorityLevel == FILTER_ALL);
        binding.profileItemsFilterHigh.setActive(priorityLevel == PriorityLevels.HIGH);
        binding.profileItemsFilterMid.setActive(priorityLevel == PriorityLevels.MID);
        binding.profileItemsFilterLow.setActive(priorityLevel == PriorityLevels.LOW);
        renderCurrentFilter();
    }

    // "전체"는 필수/중간/선택 카드를 다 보여주고, 그 외엔 고른 우선순위 카드 하나만 보여줌 — 둘 다 같은 카드 디자인
    private void renderCurrentFilter() {
        LinearLayout sections = binding.profileItemsGroupedSections;
        sections.removeAllViews();
        if (currentFilter == FILTER_ALL || currentFilter == PriorityLevels.HIGH) {
            addPrioritySection(sections, PriorityLevels.HIGH, getString(R.string.review_priority_high));
        }
        if (currentFilter == FILTER_ALL || currentFilter == PriorityLevels.MID) {
            addPrioritySection(sections, PriorityLevels.MID, getString(R.string.review_priority_mid));
        }
        if (currentFilter == FILTER_ALL || currentFilter == PriorityLevels.LOW) {
            addPrioritySection(sections, PriorityLevels.LOW, getString(R.string.review_priority_low));
        }
    }

    private List<UserDefaultItem> filterByPriority(int priorityLevel) {
        List<UserDefaultItem> filtered = new ArrayList<>();
        for (UserDefaultItem item : allItems) {
            if (PriorityLevels.fromApiValue(item.getPriority()) == priorityLevel) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    private void addPrioritySection(LinearLayout sections, int level, String label) {
        List<UserDefaultItem> items = filterByPriority(level);
        if (items.isEmpty()) return;

        View header = LayoutInflater.from(this).inflate(R.layout.molecule_section_header, sections, false);
        ((PriorityDotView) header.findViewById(R.id.sectionHeaderDot)).setLevel(level);
        ((TextView) header.findViewById(R.id.sectionHeaderLabel))
                .setText(getString(R.string.review_priority_count_format, label, items.size()));
        LinearLayout.LayoutParams headerLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (sections.getChildCount() > 0) headerLp.topMargin = dp(16);
        sections.addView(header, headerLp);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card_lg_border);
        card.setPadding(dp(12), dp(4), dp(12), dp(4));

        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                View divider = new View(this);
                divider.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(1)));
                divider.setBackgroundColor(getColor(R.color.bag_border_divider));
                card.addView(divider);
            }

            UserDefaultItem item = items.get(i);
            View row = LayoutInflater.from(this).inflate(R.layout.molecule_default_item_row, card, false);
            ((TextView) row).setText(item.getItemName());
            row.setOnClickListener(v -> showEditSheet(item));
            card.addView(row);
        }

        sections.addView(card);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
