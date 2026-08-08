package com.example.mybaghackathon.ui.profile;

/** AddItemSheet/EditItemSheet가 쓰는 우선순위 칩 레벨(0=필수,1=중간,2=선택)과 서버 priority 문자열 간 변환. */
final class PriorityLevels {

    static final int HIGH = 0;
    static final int MID = 1;
    static final int LOW = 2;

    private PriorityLevels() {
    }

    static String toApiValue(int priorityLevel) {
        switch (priorityLevel) {
            case HIGH:
                return "REQUIRED";
            case MID:
                return "RECOMMENDED";
            default:
                return "OPTIONAL";
        }
    }

    static int fromApiValue(String priority) {
        if ("REQUIRED".equals(priority)) return HIGH;
        if ("RECOMMENDED".equals(priority)) return MID;
        return LOW;
    }
}
