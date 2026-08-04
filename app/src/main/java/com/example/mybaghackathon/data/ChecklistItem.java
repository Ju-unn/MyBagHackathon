package com.example.mybaghackathon.data;

/** Simple in-memory model backing the checklist screens (S08, S10-S12). */
public class ChecklistItem {
    public String label;
    public int priority; // 0 = High, 1 = Mid, 2 = Low
    public int checkState; // matches CheckboxView.UNCHECKED/CHECKED/EXCLUDED
    public String assigneeInitial; // null if unassigned
    public int assigneeColor;
    public int restrictionType = -1; // -1 = none, else RestrictionTagView type

    public ChecklistItem(String label, int priority) {
        this.label = label;
        this.priority = priority;
    }
}
