package com.example.mybaghackathon.ui.checklist;

import com.example.mybaghackathon.model.PackingItem;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Applies the exact checked recommendation set without inferring selection from scope. */
final class ChecklistSelectionFilter {

    private ChecklistSelectionFilter() {
    }

    static List<PackingItem> apply(List<PackingItem> items, Set<String> selectedNames) {
        List<PackingItem> result = new ArrayList<>();
        if (items == null) {
            return result;
        }
        if (selectedNames == null) {
            result.addAll(items);
            return result;
        }

        for (PackingItem item : items) {
            if (item == null) {
                continue;
            }
            if ("AI".equalsIgnoreCase(item.getSource())) {
                if (selectedNames.contains(normalize(item.getItemName()))) {
                    // Checked recommendations are common checklist items regardless of the
                    // legacy scope returned by create.php.
                    item.setScope("COMMON");
                    result.add(item);
                }
            } else {
                // 프로필 기본 물품(DEFAULT)·직접 추가(USER) 물품은 추천 선택 여부와 무관하게
                // 방 참여 시점에 항상 내 목록에 보인다.
                result.add(item);
            }
        }
        return result;
    }

    static String normalize(String name) {
        if (name == null) {
            return "";
        }
        return Normalizer.normalize(name, Normalizer.Form.NFKC)
                .replaceAll("\\s+", "")
                .toLowerCase(Locale.ROOT);
    }
}
