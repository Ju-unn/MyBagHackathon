package com.example.mybaghackathon.ui.profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * S15 프로필 · 내 기본 물품의 인메모리 목록.
 *
 * 백엔드 연동(ProfileRepository) 전까지 ProfileFragment와 ProfileItemsActivity가
 * 같은 목록을 공유해서 보게 하는 임시 저장소. 프로세스가 종료되면 초기화된다.
 */
final class DefaultItemStore {

    private static final List<String> items = new ArrayList<>(Arrays.asList(
            "선크림", "우산", "보조배터리", "상비약", "여권 커버", "목베개",
            "안대", "이어폰", "멀티탭", "세면도구 파우치", "칫솔", "치약"
    ));

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
