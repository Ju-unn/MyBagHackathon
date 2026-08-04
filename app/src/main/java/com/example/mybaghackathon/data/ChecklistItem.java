package com.example.mybaghackathon.data;

/**
 * 체크리스트 화면들(S08, S10-S12)을 뒷받침하는 단순 인메모리 모델.
 *
 * 기능: 짐 항목 하나(라벨, 우선순위, 체크 상태, 담당자, 반입 제한 타입)를
 * 담는 데이터 클래스. 서버/DB 연동 없이 화면 코드에서 직접 생성해 씀.
 */
public class ChecklistItem {
    public String label;
    public int priority; // 0 = 높음, 1 = 중간, 2 = 낮음
    public int checkState; // CheckboxView.UNCHECKED/CHECKED/EXCLUDED 값과 대응
    public String assigneeInitial; // 미배정이면 null
    public int assigneeColor;
    public int restrictionType = -1; // -1 = 없음, 그 외에는 RestrictionTagView의 타입 값

    public ChecklistItem(String label, int priority) {
        this.label = label;
        this.priority = priority;
    }
}
