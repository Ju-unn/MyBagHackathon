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
            boolean matchesSelection = selectedNames.contains(normalize(item.getItemName()));
            if ("AI".equalsIgnoreCase(item.getSource())) {
                if (matchesSelection) {
                    // Checked recommendations are common checklist items regardless of the
                    // legacy scope returned by create.php.
                    item.setScope("COMMON");
                    result.add(item);
                }
            } else if ("DEFAULT".equalsIgnoreCase(item.getSource())) {
                if (matchesSelection) {
                    result.add(item);
                }
            } else {
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
