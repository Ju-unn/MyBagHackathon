package com.example.mybaghackathon.ui.checklist;

/** Activity의 체크리스트 데이터가 갱신됐을 때 현재 Fragment를 다시 그린다. */
interface ChecklistDataConsumer {
    void renderChecklist();
}
