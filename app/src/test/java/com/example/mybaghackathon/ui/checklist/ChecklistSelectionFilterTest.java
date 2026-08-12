package com.example.mybaghackathon.ui.checklist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.example.mybaghackathon.model.PackingItem;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChecklistSelectionFilterTest {

    @Test
    public void exactCheckedNameKeepsOneOutOfFifteenRegardlessOfScope() {
        List<PackingItem> recommendations = new ArrayList<>();
        PackingItem checked = aiItem(1L, "passport", "PERSONAL");
        recommendations.add(checked);
        for (int index = 2; index <= 15; index++) {
            recommendations.add(aiItem(index, "item-" + index, "COMMON"));
        }

        List<PackingItem> filtered = ChecklistSelectionFilter.apply(
                recommendations,
                Collections.singleton(ChecklistSelectionFilter.normalize(" passport ")));

        assertEquals(1, filtered.size());
        assertSame(checked, filtered.get(0));
        assertEquals("COMMON", checked.getScope());
    }

    @Test
    public void defaultItemsAreKeptRegardlessOfRecommendationSelection() {
        PackingItem matchingDefault = item(20L, "passport", "DEFAULT");
        PackingItem nonMatchingDefault = item(21L, "charger", "DEFAULT");

        List<PackingItem> filtered = ChecklistSelectionFilter.apply(
                java.util.Arrays.asList(matchingDefault, nonMatchingDefault),
                Collections.singleton(ChecklistSelectionFilter.normalize("passport")));

        assertEquals(2, filtered.size());
    }

    private PackingItem aiItem(long id, String name, String scope) {
        PackingItem item = item(id, name, "AI");
        item.setScope(scope);
        return item;
    }

    private PackingItem item(long id, String name, String source) {
        PackingItem item = new PackingItem();
        item.setPackingItemId(id);
        item.setItemName(name);
        item.setSource(source);
        item.setItemStatus("ACTIVE");
        return item;
    }
}
