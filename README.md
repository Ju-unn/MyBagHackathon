# 여행가방 대신 싸줘

여행 일정표와 숙소 예약 화면 등 **여러 장의 캡처 이미지**를 AI로 분석하여 여행 날짜, 장소, 국가, 숙소 정보를 추출하고, 여행지 날씨와 항공기 반입 제한 정보를 바탕으로 준비물 목록을 생성하는 Android 애플리케이션입니다.

생성한 준비물은 같은 여행방에 참여한 동행자들과 공유하며, 담당자와 준비 상태를 함께 관리합니다.

> 이 문서는 Manyfast의 기능명세서와 유저플로우를 기준으로 작성했습니다.  
> Android는 **Java + XML Layout**을 사용합니다. (초기 설계에서는 MVP 패턴을 계획했으나, 실제 구현은 Fragment/Activity가 `AppContainer`로 조립된 Repository를 직접 호출하는 구조입니다. 별도 Presenter 계층은 없습니다.)  
> 서버는 구축이 완료된 **AWS EC2 + Apache2 + PHP + MySQL** 환경을 사용합니다. 서버 API는 이 문서(9~10번 섹션)가 아니라 **`mybag` 백엔드 레포의 `APIs.md`**를 최신 기준으로 참고하세요.

---

## 1. 문서화 범위

이 README는 팀원들이 개발 전에 구조를 이해하고 기능별 담당을 나눌 수 있는 수준까지 작성합니다.

- 사용할 API와 선정 이유
- API 및 서버 예상 비용
- Android Java 구조
- AWS EC2 서버와 Android 앱의 연결 구조
- 전체 패키지 분리
- 주요 클래스 이름과 책임
- MySQL 데이터 구조
- 기능명세와 유저플로우의 화면 연결
- 개인정보, API Key, AI 오분석 위험 대응
- 3일 MVP 구현 순서

### 구현 예정

현재 문서에서는 구조와 책임만 정의하고 다음 내용은 개발 과정에서 구현합니다.

- 함수 내부 구현 코드
- 모든 메서드의 상세 매개변수와 반환형
- XML 화면 디자인 완성본
- 전체 국가·항공사의 반입 규정 수집
- 배포 자동화와 운영 모니터링 상세 설정
- FCM 푸시 알림 앱 수신·표시 (서버 발송 로직은 구현 완료, 8번 섹션 `service` 참고)

---

## 2. 기술 스택

### Android

- Java
- XML Layout
- MVP 패턴
- Retrofit2
- OkHttp
- Gson
- Glide
- WorkManager

### Server

- AWS EC2
- Ubuntu
- Apache2
- PHP
- REST API

### Database

- MySQL

### External API

- OpenAI Responses API
- Open-Meteo Forecast API
- Open-Meteo Geocoding API
- Kakao Login API
- 국토교통부·한국교통안전공단 반입 제한 공공데이터 - 미착수(현재는 GPT가 반입 제한 항목을 추론)
- Firebase Cloud Messaging - **서버 발송 로직 구현 완료**(담당자 지정 알림, 출발 D-7·D-3·D-1 알림). 앱에서 실제로 수신해 알림으로 표시하는 부분은 미구현

---

## 3. 핵심 기능

### 여러 장의 일정·숙소 캡처 분석

- 여행 일정표와 숙소 예약 화면을 여러 장 선택합니다.
- 이미지는 업로드 전에 압축합니다.
- EC2의 PHP 서버가 이미지를 받아 OpenAI API로 전달합니다.
- AI가 날짜, 도시, 국가, 숙소, 체크인·체크아웃 정보를 추출합니다.
- 사용자는 분석 결과를 직접 검토하고 수정합니다.

> 이미지 분석 대상은 여행 일정표와 숙소 예약 캡처로 한정합니다.

### 날씨·의식주 피드백

- 확정된 여행 장소를 위도·경도로 변환합니다.
- 일정별 기온, 강수, 날씨 상태를 조회합니다.
- 날씨와 여행 기간을 바탕으로 옷과 생활 준비물을 추천합니다.

### 항공기 반입 제한 안내

- 여행 국가와 준비물 이름을 기준으로 반입 제한 정보를 제공합니다.
- AI 판단만 사용하지 않고 공공데이터의 반입 제한 목록과 비교합니다.
- 결과에는 공식 사이트 최종 확인 안내를 표시합니다.

### 여행방과 동행자

- 사용자가 여행방을 생성합니다.
- 초대 링크로 동행자가 참여합니다. 링크 내부의 난수 코드는 서버가 참여 요청을 검증할 때 사용합니다.
- 여행방 참여자는 같은 준비물 목록을 조회합니다.
- 준비물 담당자를 지정하거나 취소합니다.

### 공용·개인 체크리스트

- 필수, 중간, 선택 중요도로 목록을 구분합니다.
- 담당자별 개인 목록을 확인합니다.
- 준비 상태를 v/x로 최종 확정합니다.
- 사용자가 준비물을 직접 추가·수정·삭제할 수 있습니다.

### 아카이브

- 참여했던 여행방 목록을 확인합니다.
- 종료된 여행방의 일정과 준비물 목록을 다시 조회합니다.

### 알림

- WorkManager 기반 기기 내부 알림 대신, **서버가 FCM으로 발송**하는 방식으로 확정했습니다.
- 담당자 본인 지정 시 트립의 다른 참여자에게 `CHECKLIST_ASSIGNED` 알림 발송 — 서버 구현·배포·검증 완료.
- 출발 D-7·D-3·D-1 알림은 서버 crontab이 매일 09:00(KST) 대상자를 계산해 `DEPARTURE_D7`/`DEPARTURE_D3`/`DEPARTURE_D1` 알림 발송 — 서버 구현·배포·검증 완료.
- 두 경우 다 FCM data 페이로드만 사용(`notification` 페이로드 아님) — **앱에서 `FcmMessagingService.onMessageReceived()`를 채워서 로컬 알림(채널·아이콘·딥링크)으로 표시하는 작업이 아직 남아있습니다.** 페이로드 필드는 안드로이드팀 전달 문서 참고.

---

## 4. 유저플로우

1. 앱 실행 후 로그인 상태를 확인합니다.
2. 로그인 정보가 없거나 만료되었으면 S02에서 카카오 로그인을 진행합니다.
3. EC2 서버가 카카오 토큰을 확인하고 앱 전용 로그인 토큰을 발급합니다.
4. S03 홈에서 진행 중인 여행과 준비 중인 여행을 확인합니다.
5. S04에서 여행방을 만들거나 공유받은 초대 링크로 기존 방에 참여합니다.
6. S05에서 일정표, 숙소 확인서, 추가 자료를 여러 장 업로드합니다.
7. S06에서 EC2 서버가 OpenAI API에 분석을 요청하고 진행 상태를 확인합니다.
8. S07에서 추출된 분석 결과를 먼저 확인합니다.
9. S08에서 날짜, 장소, 국가, 숙소 정보를 검토·수정한 뒤 확정합니다.
10. S09 여행방 상세에서 여행 정보와 참여자를 확인하고 초대 링크를 공유합니다.
11. S10에서 일정별 날씨와 의식주 피드백을 확인합니다.
12. 체크리스트에서 S11 공용, S12 내 목록, S13 분담 현황 탭을 사용합니다.
13. 참여자가 준비물 담당자를 지정하고 준비 상태를 v/x로 변경합니다.
14. S14에서 종료된 여행을 아카이브로 확인합니다.
15. S15에서 프로필과 기본 물품을 관리합니다.
16. S16에서 D-7, D-3, D-1 등의 알림 설정을 관리합니다.

---

## 5. 사용할 API와 서비스

### OpenAI Responses API

사용 목적:

- 여러 장의 일정·숙소 캡처 이미지 분석
- 날짜, 장소, 국가, 숙소 정보의 구조화
- 확정된 일정과 날씨를 바탕으로 준비물 후보 생성

추천 모델:

- `gpt-5.4-mini`

선정 이유:

- 이미지 입력을 지원합니다.
- Structured Outputs를 사용할 수 있습니다.
- 상위 모델보다 비용이 낮습니다.
- 자유 문장이 아닌 정해진 JSON 형태로 결과를 받을 수 있습니다.

호출 방식:

- Android 앱이 OpenAI API를 직접 호출하지 않습니다.
- Android 앱이 이미지를 AWS EC2 서버로 전송합니다.
- EC2의 PHP 서버가 OpenAI API를 호출합니다.
- OpenAI API Key는 EC2 서버 환경설정에만 보관합니다.

### Open-Meteo API

사용 목적:

- 여행 도시명을 좌표로 변환
- 여행 일정별 날씨 조회
- 기온, 강수, 날씨 코드 확인

선정 이유:

- 비상업 프로젝트는 API Key 없이 사용할 수 있습니다.
- Forecast와 Geocoding API를 함께 사용할 수 있습니다.
- 해커톤 MVP 구현이 간단합니다.

### Kakao Login API

사용 목적:

- 카카오 계정 로그인
- 사용자 기본 프로필 확인

처리 방식:

- Android에서 Kakao SDK로 로그인합니다.
- 발급받은 Kakao access token을 EC2 서버로 전달합니다.
- PHP 서버가 카카오 사용자 정보 API로 토큰을 확인합니다.
- 확인이 끝나면 앱에서 사용할 로그인 토큰을 발급합니다.

### AWS EC2 PHP REST API

사용 목적:

- 사용자 인증
- 여행방 생성과 참여
- 일정 이미지 업로드와 OpenAI 분석
- 여행 일정 저장
- 준비물 생성과 관리
- 담당자와 준비 상태 동기화
- 아카이브 조회

현재 상태:

- EC2 구축 완료
- Apache2 구축 완료
- PHP 실행 환경 구축 완료
- MySQL 구축 완료

### MySQL

사용 목적:

- 사용자 정보 저장
- 여행방과 참여자 저장
- 분석된 일정 저장
- 준비물과 담당자 저장
- 아카이브 저장

### FCM

현재 상태:

- 서버 발송 로직 구현·배포·검증 완료 (담당자 지정 알림, 출발 D-7·D-3·D-1 알림)
- 앱에서 토큰 등록(`POST /api/auth/fcm-token.php`)까지는 연결돼 있음
- 앱에서 수신 후 실제 알림으로 표시하는 부분(`FcmMessagingService.onMessageReceived()`)은 미구현

사용 목적:

- 준비물 담당자 지정 알림 (`CHECKLIST_ASSIGNED`)
- 출발 예정 D-7·D-3·D-1 알림 (`DEPARTURE_D7`/`DEPARTURE_D3`/`DEPARTURE_D1`)
- 공용 체크리스트 변경 알림 — 미구현, 필요 여부 팀 결정 남음

---

## 6. 예상 비용

### OpenAI API

`gpt-5.4-mini` 공식 가격 기준:

- 입력: USD 0.75 / 1M tokens
- 출력: USD 4.50 / 1M tokens

여행 1건을 다음과 같이 가정합니다.

- 일정표·숙소 예약 캡처 최대 4장
- AI 분석 1회
- 준비물 생성 1회
- 총 입력 약 7,000 tokens
- 총 출력 약 2,000 tokens
- 계획 환율 1 USD = 1,400원

예상 사용 비용:

```text
입력 비용 = 7,000 / 1,000,000 × $0.75 = $0.00525
출력 비용 = 2,000 / 1,000,000 × $4.50 = $0.00900
여행 1건 = 약 $0.01425 = 약 20원
```

규모별 계획 비용:

- 여행 30건: 약 600원
- 여행 300건: 약 6,000원
- 여행 3,000건: 약 60,000원

API를 처음 사용하는 계정은 일반적으로 최소 USD 5의 선불 크레딧 구매가 필요합니다. 실제 사용 금액은 충전된 크레딧에서 사용량만큼 차감됩니다.

### AWS EC2

EC2는 이미 구축되어 있으므로 실제 월 비용은 팀 AWS 계정에서 확인합니다.

비용을 확인할 때 다음 항목을 함께 확인해야 합니다.

- EC2 인스턴스 종류
- 사용 리전
- 월 실행 시간
- EBS 저장 공간
- 외부 데이터 전송량
- Elastic IP 과금 여부
- AWS Free Tier 또는 크레딧 적용 여부

현재 인스턴스 종류와 리전이 문서에 없으므로 정확한 월 금액은 임의로 작성하지 않습니다.

### Apache2·PHP·MySQL

- 소프트웨어 라이선스 비용은 0원입니다.
- EC2 컴퓨팅, 저장 공간, 데이터 전송 비용은 별도로 발생할 수 있습니다.

### Open-Meteo

- 비상업 프로젝트는 일 10,000회까지 무료 정책을 제공합니다.
- 무료 API에는 가동 시간 보장이 없습니다.

### FCM

- FCM 자체 사용 비용은 무료입니다.
- 현재는 연결 전이며 구현 예정입니다.

### 비용 절감 방법

- 업로드 이미지는 긴 변 1,600px 이하로 압축합니다.
- 이미지 개수를 최대 4장으로 제한합니다.
- 동일 이미지의 중복 분석을 제한합니다.
- AI 출력 token 수를 제한합니다.
- 재분석 버튼의 연속 호출을 제한합니다.
- OpenAI 자동 충전을 끄거나 상한을 설정합니다.
- EC2에 업로드한 원본 이미지는 분석 후 삭제합니다.
- 개발 기간 외에는 불필요한 EC2 인스턴스 실행 여부를 확인합니다.

---

## 7. 전체 시스템 구조

- Android Java 앱은 HTTPS로 AWS EC2에 요청합니다.
- AWS EC2에서는 Apache2와 PHP REST API가 요청을 처리합니다.
- PHP REST API는 MySQL에 데이터를 저장합니다.
- PHP REST API는 필요한 경우 OpenAI, Open-Meteo, Kakao API를 호출합니다.
- PHP 서버가 FCM으로 담당자 지정·출발 예정 알림을 발송합니다(구현 완료). 앱에서 수신해 로컬 알림으로 표시하는 부분만 남았습니다.

### 데이터 흐름

1. Android 앱이 로그인 토큰과 요청 데이터를 EC2 서버로 전송합니다.
2. Apache2가 PHP API 요청을 처리합니다.
3. PHP 서버가 로그인 토큰과 여행방 권한을 검사합니다.
4. 필요한 경우 PHP 서버가 OpenAI 또는 Open-Meteo를 호출합니다.
5. 서버가 결과를 검증하고 MySQL에 저장합니다.
6. Android 앱이 JSON 응답을 받아 화면에 표시합니다.

---

## 8. Android 패키지 구조 (2026-08-07 기준 실제 구조)

기본 패키지는 `com.example.mybaghackathon`입니다. UI는 S01~S16 화면 흐름을 기준으로 구성하고, 서버 연결 코드는 `data` 아래에서 Api·DTO·Mapper·Repository로 분리합니다. DI는 별도 프레임워크 없이 `app/AppContainer.java`가 생성자 조립으로 직접 처리합니다.

```text
com.example.mybaghackathon
├── MainActivity.java
├── app
│   ├── MyBagApplication.java
│   └── AppContainer.java                  # Repository·Api 조립하는 DI 컨테이너
├── common
│   ├── AppResult.java                     # 성공/실패 래퍼 (Repository 반환 타입)
│   ├── AppError.java
│   └── Constants.java
├── service
│   └── FcmMessagingService.java           # FCM 토큰 갱신·수신 진입점. 로컬 알림 표시는 미구현
├── model
│   ├── User.java
│   ├── Trip.java
│   ├── TripMember.java
│   ├── TripInvite.java
│   ├── TripUpload.java
│   ├── TripSchedule.java
│   ├── Accommodation.java
│   ├── AnalysisResult.java
│   ├── RestrictedItem.java
│   ├── PackingItem.java
│   ├── DefaultItem.java                   # ⚠️ 미사용 — 아래 "정리 필요" 참고
│   ├── UserDefaultItem.java                # 실제 사용되는 "내 기본 물품" 모델
│   ├── NotificationSettings.java
│   ├── Weather.java
│   └── WeatherFeedback.java
├── data
│   ├── ChecklistItem.java                 # UI 확인용 더미 데이터, 실 연동 시 model/PackingItem으로 교체 예정
│   ├── remote
│   │   ├── api
│   │   │   ├── ApiClient.java
│   │   │   ├── AuthApi.java
│   │   │   ├── TripApi.java
│   │   │   ├── UploadApi.java
│   │   │   ├── AnalysisApi.java
│   │   │   ├── PackingApi.java
│   │   │   ├── WeatherApi.java
│   │   │   ├── DefaultItemApi.java
│   │   │   ├── CreationSessionApi.java    # ⚠️ 빈 인터페이스, 미사용 — 아래 참고
│   │   │   ├── InviteApi.java             # ⚠️ 빈 인터페이스, 미사용
│   │   │   ├── ProfileApi.java            # ⚠️ 빈 인터페이스, 미사용
│   │   │   └── NotificationApi.java       # ⚠️ 빈 인터페이스, 미사용
│   │   └── dto
│   │       ├── common/ApiResponseDto.java
│   │       ├── auth/{KakaoLoginRequestDto, AuthTokenDto}.java
│   │       ├── trip/{TripDto, TripMemberDto, TripInviteDto, TripCreateRequestDto,
│   │       │         TripDetailResponseDto, TripJoinRequestDto, TripJoinResponseDto,
│   │       │         TripListResponseDto, TripMembersResponseDto}.java
│   │       ├── upload/TripUploadDto.java
│   │       ├── analysis/{AnalysisRequestDto, AnalysisResponseDto, ConfirmRequestDto,
│   │       │            RestrictedItemDto}.java
│   │       ├── packing/{ChecklistItemDto, ChecklistListResponseDto, ChecklistCreateRequestDto,
│   │       │           ChecklistCreateResponseDto, ChecklistCheckResponseDto, ChecklistAssignRequestDto,
│   │       │           ChecklistUpdateRequestDto, ChecklistItemIdRequestDto, ChecklistGenerateRequestDto,
│   │       │           PackingItemDto, DefaultItemDto}.java   # 이 DefaultItemDto는 ⚠️ 중복(아래 참고)
│   │       ├── defaultitem/{DefaultItemDto, DefaultItemCreateRequestDto, DefaultItemCreateResponseDto,
│   │       │               DefaultItemUpdateRequestDto, DefaultItemIdRequestDto,
│   │       │               DefaultItemListResponseDto}.java   # 실제 사용되는 쪽
│   │       ├── weather/{WeatherDto, WeatherForecastDto, WeatherFeedbackDto}.java
│   │       ├── profile/ProfileDto.java
│   │       ├── creation/CreationSessionDto.java   # ⚠️ 미사용
│   │       └── notification/{NotificationSettingsDto, FcmTokenDto}.java
│   ├── mapper
│   │   ├── UserMapper.java
│   │   ├── TripMapper.java
│   │   ├── AnalysisMapper.java
│   │   ├── PackingItemMapper.java
│   │   ├── WeatherMapper.java
│   │   ├── DefaultItemMapper.java
│   │   └── NotificationMapper.java
│   ├── repository
│   │   ├── AuthRepository.java / AuthRepositoryImpl.java
│   │   ├── TripRepository.java / TripRepositoryImpl.java
│   │   ├── UploadRepository.java / UploadRepositoryImpl.java
│   │   ├── AnalysisRepository.java / AnalysisRepositoryImpl.java
│   │   ├── PackingRepository.java / PackingRepositoryImpl.java   # 체크리스트 담당
│   │   ├── WeatherRepository.java / WeatherRepositoryImpl.java
│   │   ├── DefaultItemRepository.java / DefaultItemRepositoryImpl.java
│   │   ├── CreationSessionRepository.java / CreationSessionRepositoryImpl.java   # ⚠️ Impl 빈 클래스, 미사용
│   │   ├── ProfileRepository.java / ProfileRepositoryImpl.java                   # ⚠️ Impl 빈 클래스, 미사용
│   │   └── NotificationRepository.java / NotificationRepositoryImpl.java         # ⚠️ Impl 빈 클래스, 미사용
│   └── local
│       └── TokenStorage.java
├── ui
│   ├── EdgeToEdgeUtil.java
│   ├── splash/SplashActivity.java
│   ├── login/LoginActivity.java            # 카카오 SDK 실제 연동 전, 임시로 바로 MainActivity 이동
│   ├── home/
│   │   ├── HomeFragment.java               # S03, 더미 데이터
│   │   ├── TripRoomUiModel.java
│   │   └── adapter/TripRoomAdapter.java
│   ├── createroom/CreateRoomActivity.java  # S04
│   ├── upload/ScheduleUploadActivity.java  # S05
│   ├── analyzing/AnalyzingActivity.java    # S06
│   ├── analysisresult/AnalysisResultActivity.java  # S07
│   ├── review/ScheduleReviewActivity.java  # S08
│   ├── roomdetail/RoomDetailActivity.java  # S09, 아직 하드코딩 상태 + ChecklistActivity로 trip_id 미전달
│   ├── feedback/WeatherFeedbackActivity.java  # S10
│   ├── checklist/
│   │   ├── ChecklistActivity.java
│   │   ├── ChecklistCommonFragment.java    # S11
│   │   ├── ChecklistMineFragment.java      # S12
│   │   └── ChecklistAssignmentFragment.java  # S13
│   ├── archive/TripArchiveFragment.java    # S14, 탭 UI는 있고 데이터는 더미
│   ├── profile/ProfileFragment.java        # S15
│   ├── settings/NotificationSettingsActivity.java  # S16
│   ├── overlay/{AddItemSheet, EditItemSheet, InviteShareSheet}.java
│   ├── atoms/{AvatarView, CheckboxView, ChipView, DDayBadgeView, IconButtonView,
│   │         PriorityDotView, RestrictionTagView, WeatherIconView}.java
│   ├── molecules/AvatarStackHelper.java
│   └── organisms/TripRoomCardBinder.java
└── util
    ├── ImageCompressor.java
    ├── DateUtils.java
    ├── PrefsManager.java
    └── ReminderScheduler.java
```

### 패키지별 책임

- `app`은 `AppContainer`로 `TokenStorage → ApiClient → 각 Api → Repository` 순으로 조립·보관합니다. 별도 DI 프레임워크(Dagger/Hilt) 없이 생성자 주입만 사용합니다.
- `service`는 FCM 백그라운드 서비스입니다. UI 패키지가 아니라 최상위에 독립적으로 있습니다.
- `model`은 화면이 실제로 사용하는 앱 내부 데이터를 정의합니다. Presenter가 아니라 Fragment/Activity가 직접 사용합니다.
- `data.remote.api`는 EC2 PHP REST API의 Retrofit 요청을 정의합니다.
- `data.remote.dto`는 서버의 요청·응답 JSON 형식을 기능별로 구분합니다.
- `data.mapper`는 서버 DTO를 Android Model로 변환합니다.
- `data.repository`는 화면이 사용할 데이터 접근 규칙과 구현체를 제공하며, `AppContainer`에 조립된 것만 실제로 쓰입니다.
- `data.local`은 로그인 token처럼 기기에 보관해야 하는 값만 관리합니다.
- `ui`는 S01~S16 화면 흐름과 오버레이, 그리고 `atoms`/`molecules`/`organisms` 공용 컴포넌트를 기능별 패키지로 구분합니다.

### ⚠️ 정리가 필요한 것들

초기 기획 단계(README 초안)에서 미리 만들어둔 스캐폴딩 중 실제 서버 구현과 안 맞거나 아무도 참조하지 않는 코드가 남아있습니다. 삭제하거나 실제로 필요한지 재검토가 필요합니다.

- **`CreationSessionApi`/`CreationSessionRepository`, `InviteApi`, `ProfileApi`/`ProfileRepository`, `NotificationApi`/`NotificationRepository`** — 전부 메서드 없는 빈 클래스/인터페이스. 서버엔 "생성 세션"(`creation_sessions`) 리소스 자체가 없고(`upload_id`→`analysis_id`→`trip_id` 체이닝 방식으로 대체됨), 초대는 `TripApi.join()`으로, FCM 토큰 등록은 `AuthApi`로 이미 처리되고 있어서 이 4쌍은 예전 설계 유물로 보입니다.
- **`model/DefaultItem.java`** — 아무 코드에서도 참조하지 않는 죽은 클래스입니다. 실제로 쓰이는 건 `model/UserDefaultItem.java`(서버 응답 필드에 맞춰 새로 만든 것)입니다.
- **`data/remote/dto/packing/DefaultItemDto.java`** — `data/remote/dto/defaultitem/DefaultItemDto.java`와 이름이 같은 별개 클래스입니다. `DefaultItemMapper`가 실제로 쓰는 건 `defaultitem` 패키지 쪽이고, `packing` 패키지 쪽은 미사용으로 보입니다.
- **`data/ChecklistItem.java`** — UI 임시 더미. 실제 연동 시 `model/PackingItem.java` + `packingRepository`로 교체 필요.

### 화면 연결 시 참고할 흐름

- S08 "방 생성 완료" 시점에 `tripRepository.createTrip()`을 호출해 서버가 반환한 `trip_id`부터 이후 화면에서 `TripRepository`를 사용합니다. (별도 "생성 세션" 단계 없이, `upload_id`/`analysis_id`만으로 여기까지 진행됩니다.)
- S09는 날씨 팁과 체크리스트로 이동하는 여행방 허브 화면입니다.
- S10은 S09에서 진입하고 뒤로 가기로 복귀하며 체크리스트로 직접 이동하지 않습니다.
- 참여자가 1명이면 `ChecklistMineFragment`만 표시하고, 2명 이상이면 체크리스트 3개 탭을 표시합니다.

---

## 9. EC2 PHP 서버 구조 — ⚠️ 초기 설계안, 실제 구현과 다릅니다

> **이 섹션(9~10번)은 개발 시작 전 계획 단계에서 작성한 초안입니다.** 실제 서버는 "생성 세션"(`creation_sessions`) 리소스 없이 `upload_id`→`analysis_id`→`trip_id` 체이닝 방식으로 구현됐고, REST 라우팅(`/api/trips/{tripId}` 같은 경로 파라미터·PATCH/DELETE)이 아니라 `html/api/도메인/동작.php` 형태의 파일 기반 엔드포인트로 구현됐습니다. **최신 API 스펙은 `mybag` 백엔드 레포 루트의 `APIs.md`를, 서버 구현 히스토리는 백엔드팀이 관리하는 `내가방서버현황.md`를 참고하세요.** 아래 내용은 최초 기획 의도를 참고하는 용도로만 남겨둡니다.

EC2 PHP 서버는 인증, 방 생성 전 임시 세션, 여러 장 업로드, AI 분석, 여행방 확정, 체크리스트 공유를 처리합니다. S04에서 실제 방을 바로 만들지 않고 S08의 목록 아이템 생성 시점에 방과 방장 권한을 확정하는 최신 흐름을 기준으로 합니다.

### 서버 요청 흐름

```text
Android 앱
└── HTTPS JSON 또는 Multipart 요청
    └── Apache2
        └── public/index.php
            ├── Router
            ├── Middleware
            ├── Controller
            ├── Service
            ├── Repository
            └── MySQL

외부 연동
├── Kakao 사용자 정보 API
├── OpenAI Responses API
├── Open-Meteo API
└── FCM HTTP v1 API · 구현 예정
```

### 권장 디렉터리 구조

```text
server
├── public
│   ├── index.php
│   └── .htaccess
├── bootstrap
│   └── app.php
├── config
│   ├── database.php
│   ├── environment.php
│   ├── cors.php
│   └── upload.php
├── routes
│   ├── auth.php
│   ├── creation_sessions.php
│   ├── trips.php
│   ├── invites.php
│   ├── uploads.php
│   ├── analyses.php
│   ├── packing.php
│   ├── weather.php
│   ├── profile.php
│   └── notifications.php
├── src
│   ├── Controller
│   │   ├── AuthController.php
│   │   ├── CreationSessionController.php
│   │   ├── TripController.php
│   │   ├── InviteController.php
│   │   ├── UploadController.php
│   │   ├── AnalysisController.php
│   │   ├── PackingController.php
│   │   ├── WeatherController.php
│   │   ├── ProfileController.php
│   │   └── NotificationController.php
│   ├── Service
│   │   ├── AuthService.php
│   │   ├── CreationSessionService.php
│   │   ├── TripService.php
│   │   ├── InviteService.php
│   │   ├── UploadService.php
│   │   ├── AnalysisService.php
│   │   ├── PackingService.php
│   │   ├── WeatherService.php
│   │   └── NotificationService.php
│   ├── Repository
│   │   ├── UserRepository.php
│   │   ├── AuthSessionRepository.php
│   │   ├── CreationSessionRepository.php
│   │   ├── TripRepository.php
│   │   ├── TripMemberRepository.php
│   │   ├── TripInviteRepository.php
│   │   ├── TripUploadRepository.php
│   │   ├── AnalysisRepository.php
│   │   ├── ScheduleRepository.php
│   │   ├── AccommodationRepository.php
│   │   ├── PackingItemRepository.php
│   │   └── NotificationRepository.php
│   ├── Middleware
│   │   ├── AuthMiddleware.php
│   │   ├── CreationSessionOwnerMiddleware.php
│   │   ├── TripMemberMiddleware.php
│   │   ├── TripOwnerMiddleware.php
│   │   └── RateLimitMiddleware.php
│   ├── Client
│   │   ├── KakaoApiClient.php
│   │   ├── OpenAiApiClient.php
│   │   ├── OpenMeteoApiClient.php
│   │   └── FcmApiClient.php
│   └── Support
│       ├── ApiResponse.php
│       ├── Validator.php
│       ├── FileUploader.php
│       ├── TokenIssuer.php
│       └── ErrorHandler.php
├── storage
│   ├── uploads
│   │   ├── creation_sessions
│   │   └── trips
│   └── logs
├── sql
│   └── schema.sql
├── .env.example
└── composer.json
```

`public`만 Apache2의 DocumentRoot로 공개합니다. 업로드 원본, 환경변수, 로그, SQL 파일은 외부에서 직접 접근할 수 없는 위치에 둡니다.

### 계층별 책임

- `Controller`는 요청값 확인과 응답 반환을 담당합니다.
- `Service`는 생성 세션 확정, 초대 참여처럼 여러 저장 작업이 묶이는 업무 규칙을 담당합니다.
- `Repository`는 MySQL 조회·저장을 담당합니다.
- `Middleware`는 로그인, 생성 세션 소유자, 여행방 참여자·방장 권한을 검사합니다.
- `Client`는 Kakao, OpenAI, Open-Meteo, FCM 외부 호출을 한곳에 모읍니다.
- `FileUploader`는 여러 장 이미지의 MIME, 용량, 파일명과 저장 위치를 검사합니다.

### 최종 흐름 기준 REST API

```text
인증 · S02
POST   /api/auth/kakao
POST   /api/auth/refresh
POST   /api/auth/logout

방 생성 전 임시 세션 · S04~S08
POST   /api/creation-sessions
GET    /api/creation-sessions/{sessionId}
PATCH  /api/creation-sessions/{sessionId}
DELETE /api/creation-sessions/{sessionId}
POST   /api/creation-sessions/{sessionId}/uploads
GET    /api/creation-sessions/{sessionId}/uploads
DELETE /api/creation-sessions/{sessionId}/uploads/{uploadId}
POST   /api/creation-sessions/{sessionId}/analyses
GET    /api/creation-sessions/{sessionId}/analyses/{analysisId}
PUT    /api/creation-sessions/{sessionId}/analysis-result
POST   /api/creation-sessions/{sessionId}/confirm

확정된 여행방 · S03, S09, S14
GET    /api/trips?view=home
GET    /api/trips/{tripId}
PATCH  /api/trips/{tripId}
DELETE /api/trips/{tripId}
GET    /api/trips?status=ARCHIVED
GET    /api/trips/{tripId}/members

초대 링크·동행자 · 방장 전용 발급
POST   /api/trips/{tripId}/invites
POST   /api/invites/{inviteCode}/join
DELETE /api/trips/{tripId}/members/{userId}

일정 재분석 · 방장 전용
POST   /api/trips/{tripId}/analysis-sessions

날씨·의식주 팁 · S10
GET    /api/trips/{tripId}/weather-feedback

체크리스트 · S11~S13
GET    /api/trips/{tripId}/packing-items?scope=COMMON
GET    /api/trips/{tripId}/packing-items?assignee=me
GET    /api/trips/{tripId}/packing-items?groupBy=assignee
POST   /api/trips/{tripId}/packing-items
PATCH  /api/trips/{tripId}/packing-items/{itemId}
DELETE /api/trips/{tripId}/packing-items/{itemId}
PATCH  /api/trips/{tripId}/packing-items/{itemId}/assignee
PATCH  /api/trips/{tripId}/packing-items/{itemId}/completion

프로필·기본 물품 · S15
GET    /api/me
PATCH  /api/me
GET    /api/me/default-items
POST   /api/me/default-items
PATCH  /api/me/default-items/{defaultItemId}
DELETE /api/me/default-items/{defaultItemId}

알림 · S16
GET    /api/me/notification-settings
POST   /api/me/fcm-tokens              · 구현 예정
DELETE /api/me/fcm-tokens/{deviceId}   · 구현 예정
```

S16의 D-7, D-3, D-1 알림은 MVP에서 읽기 전용으로 표시합니다. 사용자별 알림 시점 변경 API는 추후 구현합니다.

### 권한 규칙

- 생성 세션의 업로드·분석·확정은 해당 세션을 만든 사용자만 수행할 수 있습니다.
- 방 만들기, 일정 업로드·재분석, 초대 링크 발급은 방장 전용입니다.
- 여행방 참여자만 여행 정보와 체크리스트를 조회할 수 있습니다.
- Android에서 버튼을 숨기는 것과 별개로 PHP 서버가 권한을 다시 검사합니다.
- 권한이 없으면 HTTP 403을 반환합니다.

### 반드시 트랜잭션으로 처리할 작업

- 생성 세션 확정: 여행방 생성 → 생성자를 OWNER로 등록 → 일정·숙소 저장 → 공용·개인 준비물 저장 → 분석 확정 → 생성 세션 완료
- 초대 참여: 초대 유효성 확인 → 중복 참여 확인 → 참여자 등록 → 초대 사용 횟수 증가
- 내 목록 슬라이드 삭제: 삭제 권한 확인 → 준비물 소프트 삭제
- 여행방 삭제: 관련 데이터의 소프트 삭제 또는 외래키 정책 적용

---

## 10. MySQL 데이터 구조 — ⚠️ 초기 설계안, 실제 구현과 다릅니다

> 9번 섹션과 동일하게 이 섹션도 계획 단계 초안입니다. 실제로는 "생성 세션" 테이블이 없고, `trip_uploads`/`ai_analyses`가 `trip_id NULL` 상태로 먼저 생성됐다가 방 생성 확정 시 `UPDATE`로 채워지는 방식입니다. `trips.status`로 진행중/지난 여행을 구분하는 방식도 폐기되고 `end_date` 기준 조회로 대체됐습니다. **실제 스키마는 백엔드 레포의 `내가방서버현황.md` 8번 섹션(`CREATE TABLE` 원문)을 참고하세요.**

MySQL은 사용자, 방 생성 전 임시 세션, 확정된 여행방, 분석 결과, 체크리스트와 알림 데이터를 관리합니다. 이 문서에서는 데이터 영역과 관계만 정의하며 테이블별 상세 컬럼과 `CREATE TABLE` 문은 작성하지 않습니다.

### 설계 기준

- MySQL 8.0, InnoDB, `utf8mb4`를 사용합니다.
- Android는 MySQL에 직접 접속하지 않고 EC2 PHP REST API를 통해서만 접근합니다.
- S04에서 입력한 방 정보는 실제 여행방이 아니라 생성 세션으로 임시 보관합니다.
- S08에서 목록 아이템 생성을 확정할 때 실제 여행방과 OWNER 권한을 생성합니다.
- AI 원문과 사용자가 확정한 일정·숙소·준비물을 구분합니다.
- 업로드 이미지는 MySQL BLOB으로 저장하지 않고 EC2 비공개 경로에 저장합니다.
- 날짜·시간은 UTC로 저장하고 Android에서 사용자 시간대로 표시합니다.
- 아카이브는 별도 데이터를 복사하지 않고 여행방 상태와 종료일을 기준으로 조회합니다.
- 삭제 복구와 관계 보존이 필요한 데이터는 소프트 삭제를 사용합니다.

### 데이터 관계

```text
사용자
├── 로그인 세션
├── 기본 준비물
├── 알림 설정 · MVP 읽기 전용
├── FCM 기기 token · 구현 예정
├── 방 생성 세션
│   ├── 여러 장 업로드
│   └── AI 분석 실행과 1차 결과
└── 여행방 참여 관계
    └── 확정된 여행방
        ├── 참여자
        ├── 초대 링크
        ├── 확정 일정
        ├── 확정 숙소
        ├── AI 분석 이력
        ├── 공용·개인 준비물
        └── 날씨·의식주 피드백 캐시 · 필요 시
```

### 방 생성 전 데이터

S04에서 입력한 방 이름과 예상 인원은 생성 세션에 저장합니다. 생성 세션은 만든 사용자만 접근할 수 있으며 홈과 아카이브 목록에는 표시하지 않습니다.

```text
생성 세션 상태
├── DRAFT       방 정보 입력
├── UPLOADING   이미지 업로드 중
├── ANALYZING   AI 분석 중
├── REVIEWING   S07·S08 검토 중
├── CONFIRMED   실제 여행방 생성 완료
├── FAILED      분석 또는 확정 실패
└── EXPIRED     유효기간 만료
```

생성 세션에는 방 이름, 예상 인원, 생성자, 진행 상태, 만료 시점만 보관합니다. 실제 방장 권한과 여행방 참여 관계는 S08 확정 전까지 만들지 않습니다.

### 업로드와 AI 분석 데이터

- 일정표, 항공권, 캘린더 캡처, 숙소 확인서와 추가 이미지를 여러 장 관리합니다.
- 국내·해외 여부는 사용자가 직접 선택하지 않고 분석 결과의 국가와 이동수단으로 판단합니다.
- 분석 실행마다 별도 이력을 남겨 재분석과 실패 원인을 구분합니다.
- S07의 1차 결과는 여행지, 날짜, 숙소, 이동수단을 우선 확인합니다.
- S08에서 사용자가 수정·확정한 값은 AI 원문과 분리하여 저장합니다.
- 생성 세션이 만료되거나 취소되면 임시 업로드와 분석 데이터는 보관 정책에 따라 삭제합니다.

### 여행방 확정 데이터

S08에서 사용자가 목록 아이템 생성을 누르면 다음 데이터를 하나의 트랜잭션으로 확정합니다.

```text
생성 세션 검증
→ 실제 여행방 생성
→ 생성자를 OWNER로 등록
→ 일정 저장
→ 숙소 저장
→ 준비물 저장
→ 공용·개인 분류 반영
→ 분석 상태 확정
→ 생성 세션 완료
```

하나라도 실패하면 전체 작업을 취소하여 방만 존재하거나 체크리스트만 누락되는 상태를 방지합니다.

### 여행방과 참여자

- 여행방은 방장, 여행명, 예상 인원, 국내·해외 자동 판단 결과, 국가·도시, 여행 기간과 상태를 관리합니다.
- 생성 세션 단계의 데이터는 홈에 표시하지 않고 확정된 여행방만 홈과 아카이브에서 조회합니다.
- 방장은 일정 업로드·재분석, 초대 링크 발급과 참여자 관리 권한을 가집니다.
- 일반 참여자는 공유된 여행 정보와 체크리스트를 조회하고 자신의 담당 항목을 관리합니다.
- 같은 사용자가 같은 여행방에 중복 참여하지 않도록 제한합니다.

### 일정과 숙소

- 일정은 날짜, 시간, 국가·도시, 장소, 활동 내용과 정렬 순서를 관리합니다.
- 숙소는 숙소명, 주소, 체크인·체크아웃과 위치 정보를 관리합니다.
- 일정과 숙소는 AI 분석 원문이 아니라 사용자가 S08에서 검토·확정한 값을 기준으로 저장합니다.
- 예약번호와 투숙객 이름처럼 앱 기능에 필요하지 않은 개인정보는 저장하지 않습니다.

### 체크리스트

```text
공용 준비물
→ 참여자 2명 이상일 때 S11에 표시
→ 담당자 지정·해제 가능
→ 완료 상태와 진행률 관리

개인 준비물
→ S12 내 목록에 표시
→ 목록에 남아있는 항목은 챙길 물품
→ 가져가지 않을 항목은 슬라이드 삭제

분담 현황
→ 참여자 2명 이상일 때 S13에 표시
→ 담당자별 항목과 미지정 항목을 그룹화
```

- 준비물의 공용·개인 분류와 담당자 지정은 서로 다른 값으로 관리합니다.
- v/x 최종 확정 상태는 저장하지 않습니다.
- 내 목록에서 삭제한 항목은 실행취소 시간을 지원할 수 있도록 우선 소프트 삭제합니다.
- 공용 체크리스트의 체크 상태와 진행률을 위한 완료 정보는 유지합니다.
- 현재 UI는 준비물 한 개에 담당자 한 명을 지정하는 구조입니다.

### 기본 물품

- 프로필에서 관리하는 기본 물품은 특정 여행의 준비물과 분리합니다.
- 새 여행방 체크리스트를 생성할 때 활성화된 기본 물품을 개인 준비물 후보로 복사할 수 있습니다.
- 기본 물품 변경이 과거 여행의 준비물에 영향을 주지 않도록 복사 후 별도 데이터로 관리합니다.

### 날씨와 알림

- Open-Meteo 결과는 요청 시 조회하고 짧게 캐시하는 것을 기본으로 합니다.
- 여러 사용자의 반복 요청으로 응답 속도가 문제가 될 때만 여행별 날씨 캐시를 추가합니다.
- S16의 D-7, D-3, D-1 알림 설정은 MVP에서 읽기 전용으로 표시합니다.
- 사용자별 알림 커스터마이징과 FCM token 저장은 구현 예정으로 구분합니다.
- 출발일이 확정되지 않은 여행방에는 D-day 알림을 보내지 않습니다.

### API Key와 비밀값

```text
EC2 환경변수
├── DB_HOST
├── DB_NAME
├── DB_USER
├── DB_PASSWORD
├── KAKAO_REST_API_KEY
├── OPENAI_API_KEY
├── OPEN_METEO_BASE_URL
└── FCM_SERVICE_ACCOUNT_PATH · 구현 예정
```

API Key, DB 비밀번호, 로그인 token 원문은 MySQL 데이터, Android 소스 또는 GitHub README에 저장하지 않습니다.

---

## 11. 보안과 개인정보

### OpenAI API Key

- Android APK에 저장하지 않습니다.
- EC2 환경설정 또는 안전한 서버 환경변수에 저장합니다.
- GitHub에 API Key와 `.env` 파일을 올리지 않습니다.

### MySQL

- MySQL 포트를 외부에 공개하지 않습니다.
- EC2 내부 또는 허용된 서버에서만 접속합니다.
- PHP에서는 Prepared Statement를 사용합니다.
- DB 계정은 필요한 권한만 부여합니다.

### 사용자 인증

- 모든 여행방 API에서 로그인 토큰을 확인합니다.
- 여행방 참여자만 해당 여행과 준비물을 조회할 수 있습니다.
- 방장 기능은 서버에서 역할을 다시 확인합니다.

### 이미지

- 업로드 전에 이미지 크기와 파일 형식을 검사합니다.
- 예약번호, 이름 등 개인정보가 포함될 수 있음을 안내합니다.
- 원본 이미지는 OpenAI 분석 완료 후 삭제합니다.
- 이미지 원문과 분석 요청 내용을 서버 로그에 기록하지 않습니다.

### AI 결과

- 결과를 자동 확정하지 않습니다.
- 사용자가 검토하고 수정한 뒤 저장합니다.
- 반입 제한 결과는 참고 정보로 표시합니다.
- 항공사와 공항 공식 안내를 최종 확인하도록 안내합니다.
