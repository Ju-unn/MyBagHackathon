package com.example.mybaghackathon.ui.profile;

import java.util.ArrayList;
import java.util.List;

/**
 * S15 프로필 · 내 기본 물품의 인메모리 목록.
 *
 * 백엔드 연동(ProfileRepository) 전까지 ProfileFragment와 ProfileItemsActivity가
 * 같은 목록을 공유해서 보게 하는 임시 저장소. 프로세스가 종료되면 초기화된다.
 * 목데이터 없이 빈 목록에서 시작하고, 사용자가 직접 추가한 항목만 담는다.
 */
final class DefaultItemStore {

    private static final List<String> items = new ArrayList<>();

    private DefaultItemStore() {
    }

    static List<String> getItems() {
        return items;
    }

    static void add(String label) {
        items.add(0, label);
    }

    static void rename(int index, String newLabel) {
        if (index >= 0 && index < items.size()) {
            items.set(index, newLabel);
        }
    }

    static void removeAt(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
        }
    }
}
