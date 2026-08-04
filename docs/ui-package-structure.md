# 마이백(mybag) UI 패키지 분리표

Atomic Design 기준(Atom → Molecule → Organism → Screen)으로 UI를 구성했고,
Java 클래스는 현재 전부 삭제된 상태라 아래는 **다시 클래스를 만들 때 따라갈
패키지 구조 설계도**임. XML 레이아웃은 이미 완성되어 있으므로, 각 패키지에
대응하는 파일을 그대로 매핑해서 작업하면 됨.

기본 패키지: `com.example.mybaghackathon`

## 1. 전체 구조

| 계층 | 패키지 | 역할 |
|---|---|---|
| Atom | `ui.atoms` | 더 이상 쪼갤 수 없는 최소 단위 UI 부품 (커스텀 View) |
| Molecule | `ui.molecules` | Atom 여러 개를 조합한 재사용 단위 (뷰 바인딩 헬퍼) |
| Organism | `ui.organisms` | Molecule을 조합한 화면 상단/카드 등 큰 블록 (바인더 클래스) |
| Screen | `ui.[화면명]` | 화면 1개 = 패키지 1개 (Activity/Fragment + 그 화면 전용 로직) |
| Overlay | `ui.overlay` | 바텀시트 다이얼로그 3종 |
| Data | `data` | 화면 간 공유되는 단순 데이터 모델 |

## 2. Atom — `ui.atoms`

| 클래스명 | 관련 XML | 설명 |
|---|---|---|
| `AvatarView` | `view_avatar.xml` | 원형 이니셜 아바타 |
| `DDayBadgeView` | `view_dday_badge.xml` | D-day 뱃지 |
| `PriorityDotView` | (코드 전용, XML 없음) | 중요도 점 표시 |
| `CheckboxView` | `view_checkbox.xml` | 체크/제외 3-state 체크박스 |
| `ChipView` | `view_chip.xml` | 필터/세그먼트 칩 |
| `IconButtonView` | `view_icon_button.xml` | 원형 아이콘 버튼 |
| `WeatherIconView` | `view_weather_icon.xml` | 날씨 아이콘 |
| `RestrictionTagView` | `view_restriction_tag.xml` | 기내/위탁/반입금지 태그 |

## 3. Molecule — `ui.molecules`

| 클래스명(헬퍼) | 관련 XML | 설명 |
|---|---|---|
| `AvatarStackHelper` | `molecule_avatar_stack.xml` | 아바타 겹침 스택 동적 생성 |
| — | `molecule_section_header.xml` | 중요도 섹션 헤더 (필수/중간/선택) |
| — | `molecule_checklist_item_row.xml` | 체크리스트 항목 1줄 |
| — | `molecule_dashed_add_card.xml` | 점선 "추가" 카드 |
| — | `molecule_member_list_item.xml` | 참여자 목록 1줄 |
| — | `molecule_warning_banner.xml` | 경고 배너 |
| — | `molecule_upload_guide_card.xml` | 업로드 가이드 카드 |
| — | `molecule_review_field_card.xml` | 검토 화면 필드 카드 |
| — | `molecule_weather_day_row.xml` | 날짜별 날씨 1줄 |
| — | `molecule_assignment_row.xml` | 분담 현황 1줄 |
| — | `molecule_text_field.xml` | 라벨 있는 텍스트 입력 필드 |

## 4. Organism — `ui.organisms`

| 클래스명(바인더) | 관련 XML | 설명 |
|---|---|---|
| — | `organism_top_app_bar_large.xml` | 큰 타이틀 상단 바 |
| — | `organism_top_app_bar_compact.xml` | 뒤로가기 + 제목 상단 바 |
| `TripRoomCardBinder` | `organism_trip_room_card.xml` | 여행방 카드 (진행중/준비전/완료 3상태) |
| — | `organism_bottom_cta.xml` | 하단 고정 액션 버튼 영역 |
| — | `organism_empty_state.xml` | 빈 상태 화면 |

## 5. Screen — 화면별 패키지

| 화면 ID | 패키지 | 관련 XML | 호스트 방식 |
|---|---|---|---|
| S01 | `ui.splash` | `activity_splash.xml` | 단독 Activity (런처) |
| S02 | `ui.login` | `activity_login.xml` | 단독 Activity |
| S03 | `ui.home` | `fragment_home.xml` | MainActivity 하단 탭 |
| S04 | `ui.createroom` | `activity_create_room.xml` | 단독 Activity |
| S05 | `ui.roomdetail` | `activity_room_detail.xml` | 단독 Activity |
| S06 | `ui.upload` | `activity_schedule_upload.xml` | 단독 Activity |
| S07 | `ui.analyzing` | `activity_analyzing.xml` | 단독 Activity |
| S08 | `ui.review` | `activity_schedule_review.xml` | 단독 Activity |
| S09 | `ui.feedback` | `activity_weather_feedback.xml` | 단독 Activity |
| S10-12 | `ui.checklist` | `activity_checklist.xml` + `fragment_checklist_common/mine/assignment.xml` | 단독 Activity + 탭 3개 |
| S13 | `ui.archive` | `fragment_archive.xml` | MainActivity 하단 탭 |
| S14 | `ui.profile` | `fragment_profile.xml` | MainActivity 하단 탭 |
| S15 | `ui.settings` | `activity_notification_settings.xml` | 단독 Activity |
| — | `com.example.mybaghackathon` (루트) | `activity_main.xml` | 하단 네비게이션 호스트 (S03/S13/S14) |

## 6. Overlay — `ui.overlay`

| 화면 ID | 관련 XML | 설명 |
|---|---|---|
| BS01 | `sheet_invite.xml` | 초대 링크 공유 바텀시트 |
| BS02 | `sheet_add_item.xml` | 항목 직접 추가 바텀시트 |
| BS03 | `sheet_edit_item.xml` | 개인 물품 수정 바텀시트 |

## 7. Data — `data`

| 클래스명 | 설명 |
|---|---|
| `ChecklistItem` | 체크리스트 항목 (이름, 중요도, 체크상태, 담당자, 반입제한) 단순 모델 |

## 참고

- 리소스 값(색상/간격/타이포그래피/스타일)은 패키지와 무관하게 `res/values/`에 공용으로 존재함 (`colors.xml`, `dimens.xml`, `styles.xml`, `attrs.xml`).
- `res/values-v28/styles.xml`은 API 28 이상 전용 오버라이드(줄간격 지원), `res/values-sw600dp/dimens.xml`은 태블릿/폴더블 전용 여백 오버라이드임.
