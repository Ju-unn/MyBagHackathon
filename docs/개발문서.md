# 여행가방 대신 싸줘

여행 일정표와 숙소 예약 화면 등 **여러 장의 캡처 이미지**를 AI로 분석하여 여행 날짜, 장소, 국가, 숙소 정보를 추출하고, 여행지 날씨와 항공기 반입 제한 정보를 바탕으로 준비물 목록을 생성하는 Android 애플리케이션입니다.

생성한 준비물은 같은 여행방에 참여한 동행자들과 공유하며, 담당자와 준비 상태를 함께 관리합니다.

> 이 문서는 Manyfast의 기능명세서와 유저플로우를 기준으로 작성했습니다.  
> Android는 **Java + XML Layout + MVP 패턴**을 사용합니다. 각 화면은 `Activity`/`Fragment`(View) + `Contract` + `Presenter`로 구성하고, `Presenter`가 `AppContainer`에 조립된 `Repository`를 호출합니다. DI 프레임워크(Dagger/Hilt)는 쓰지 않고 `AppContainer`가 생성자 조립으로 직접 처리합니다.  
> 서버는 구축이 완료된 **AWS EC2 + Apache2 + PHP + MySQL** 환경을 사용합니다. 서버 API는 이 문서(9~10번 섹션)가 아니라 **`mybag` 백엔드 레포의 `APIs.md`**를 최신 기준으로 참고하세요.  
> 앱 `applicationId`는 `com.mybagteam.mybag`, 소스 패키지(`namespace`)는 `com.example.mybaghackathon`입니다.

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
- `CHECKLIST_ASSIGNED`(담당자 지정) 알림의 앱 수신·표시 (D-7·D-3·D-1 출발 알림과 방 삭제 알림 수신·표시는 구현 완료, 8번 섹션 `service` 참고)

---

## 2. 기술 스택

### Android

- Java
- XML Layout (ViewBinding)
- MVP 패턴 (Contract + Presenter, `AppContainer` 생성자 조립 DI)
- Retrofit2
- OkHttp
- Gson
- Glide
- Kakao SDK (user / share)
- Firebase Cloud Messaging

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
- Firebase Cloud Messaging - **서버 발송 + 앱 수신 구현 완료**. 서버가 담당자 지정(`CHECKLIST_ASSIGNED`)·출발 D-7·D-3·D-1·방 삭제(`TRIP_DELETED`) 알림을 발송하고, 앱 `FcmMessagingService`가 출발 알림과 방 삭제 알림을 로컬 알림으로 표시(채널·딥링크 포함)합니다. 담당자 지정 알림의 앱 표시만 남아 있습니다

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

- 기기 내부 스케줄링 대신 서버가 FCM으로 발송하는 방식으로 확정했습니다.
- 담당자 본인을 지정하면 같은 트립의 다른 참여자에게 `CHECKLIST_ASSIGNED` 알림을 보냅니다.
- 출발 D-7·D-3·D-1이 되면 서버 crontab이 매일 09시(KST)에 대상자를 계산해 `DEPARTURE_D7`/`DEPARTURE_D3`/`DEPARTURE_D1` 알림을 보냅니다.
- 방장이 방을 삭제하면 참여자에게 `TRIP_DELETED` 알림을 보냅니다.
- 전부 FCM data 페이로드만 사용합니다(`notification` 페이로드 아님). `FcmMessagingService.onMessageReceived()`가 data의 `type`을 보고 출발 알림·방 삭제 알림을 로컬 알림(`trip_notifications` 채널)으로 표시하며, 탭하면 `RoomDetailActivity`로 딥링크합니다. `CHECKLIST_ASSIGNED`의 앱 표시만 아직 남아 있습니다. 페이로드 필드는 안드로이드팀 전달 문서를 참고하세요.

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

- 서버 발송 로직(담당자 지정, 출발 D-7·D-3·D-1, 방 삭제 알림)은 배포·검증까지 끝났습니다.
- 앱 토큰 등록은 `AuthRepository.registerFcmToken()`(`onNewToken`에서 호출)으로 연결돼 있습니다.
- 앱 수신·표시(`FcmMessagingService.onMessageReceived()`)는 출발 알림·방 삭제 알림에 대해 구현 완료입니다. 담당자 지정 알림의 앱 표시만 남아 있습니다.

사용 목적:

- 준비물 담당자 지정 알림 (`CHECKLIST_ASSIGNED`) — 앱 표시 미구현
- 출발 예정 D-7·D-3·D-1 알림 (`DEPARTURE_D7`/`DEPARTURE_D3`/`DEPARTURE_D1`)
- 방 삭제 알림 (`TRIP_DELETED`)
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
- 서버 발송과 앱 수신(출발·방 삭제 알림) 모두 연결 완료입니다.

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
- PHP 서버가 FCM으로 담당자 지정·출발 예정·방 삭제 알림을 발송하고, 앱 `FcmMessagingService`가 출발/방 삭제 알림을 로컬 알림으로 표시합니다.

### 데이터 흐름

1. Android 앱이 로그인 토큰과 요청 데이터를 EC2 서버로 전송합니다.
2. Apache2가 PHP API 요청을 처리합니다.
3. PHP 서버가 로그인 토큰과 여행방 권한을 검사합니다.
4. 필요한 경우 PHP 서버가 OpenAI 또는 Open-Meteo를 호출합니다.
5. 서버가 결과를 검증하고 MySQL에 저장합니다.
6. Android 앱이 JSON 응답을 받아 화면에 표시합니다.

---

## 8. Android 패키지 구조

소스 패키지는 `com.example.mybaghackathon`입니다. UI는 S01~S16 화면 흐름을 기준으로 구성하며 각 화면은 **View(Activity/Fragment) + Contract + Presenter** 세 축의 MVP로 나눕니다. 서버 연결 코드는 `data` 아래에서 Api·DTO·Mapper·Repository로 분리하고, DI는 별도 프레임워크 없이 `app/AppContainer.java`가 생성자 조립으로 처리합니다.

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
│   └── FcmMessagingService.java           # FCM 토큰 갱신·수신 진입점. 출발·방 삭제 알림 로컬 표시 구현 완료
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
│   ├── UserDefaultItem.java                # "내 기본 물품" 모델
│   ├── NotificationSettings.java
│   ├── Weather.java
│   ├── WeatherForecast.java
│   └── WeatherFeedback.java
├── data
│   ├── ChecklistItem.java                 # 체크리스트 UI 모델
│   ├── remote
│   │   ├── adapter
│   │   │   └── FlexibleBooleanAdapter.java  # 서버가 0/1·"true" 등으로 주는 boolean 관용 파싱
│   │   ├── api
│   │   │   ├── ApiClient.java
│   │   │   ├── AuthApi.java
│   │   │   ├── TripApi.java
│   │   │   ├── UploadApi.java
│   │   │   ├── AnalysisApi.java
│   │   │   ├── PackingApi.java
│   │   │   ├── WeatherApi.java
│   │   │   ├── DefaultItemApi.java
│   │   │   └── NotificationSettingsApi.java
│   │   └── dto
│   │       ├── common/ApiResponseDto.java
│   │       ├── auth/{KakaoLoginRequestDto, AuthTokenDto}.java
│   │       ├── trip/{TripDto, TripMemberDto, TripInviteDto, TripCreateRequestDto,
│   │       │         TripDetailResponseDto, TripJoinRequestDto, TripJoinResponseDto,
│   │       │         TripListResponseDto, TripMembersResponseDto, TripIdRequestDto}.java
│   │       ├── upload/TripUploadDto.java
│   │       ├── analysis/{AnalysisRequestDto, AnalysisResponseDto, ConfirmRequestDto,
│   │       │            RestrictedItemDto}.java
│   │       ├── packing/{ChecklistItemDto, ChecklistListResponseDto, ChecklistCreateRequestDto,
│   │       │           ChecklistCreateResponseDto, ChecklistCheckResponseDto, ChecklistAssignRequestDto,
│   │       │           ChecklistUpdateRequestDto, ChecklistItemIdRequestDto, ChecklistGenerateRequestDto,
│   │       │           PackingItemDto}.java
│   │       ├── defaultitem/{DefaultItemDto, DefaultItemCreateRequestDto, DefaultItemCreateResponseDto,
│   │       │               DefaultItemUpdateRequestDto, DefaultItemIdRequestDto,
│   │       │               DefaultItemListResponseDto}.java
│   │       ├── weather/{WeatherDto, WeatherForecastDto, WeatherFeedbackDto}.java
│   │       └── notification/{NotificationSettingsDto, NotificationSettingsUpdateRequestDto, FcmTokenDto}.java
│   ├── mapper
│   │   ├── UserMapper.java
│   │   ├── TripMapper.java
│   │   ├── AnalysisMapper.java
│   │   ├── PackingItemMapper.java
│   │   ├── WeatherMapper.java
│   │   ├── DefaultItemMapper.java
│   │   └── NotificationMapper.java
│   ├── repository
│   │   ├── AuthRepository.java / AuthRepositoryImpl.java         # 로그인·FCM 토큰 등록 포함
│   │   ├── TripRepository.java / TripRepositoryImpl.java
│   │   ├── UploadRepository.java / UploadRepositoryImpl.java
│   │   ├── AnalysisRepository.java / AnalysisRepositoryImpl.java
│   │   ├── PackingRepository.java / PackingRepositoryImpl.java   # 체크리스트 담당
│   │   ├── WeatherRepository.java / WeatherRepositoryImpl.java
│   │   ├── DefaultItemRepository.java / DefaultItemRepositoryImpl.java
│   │   └── NotificationSettingsRepository.java / NotificationSettingsRepositoryImpl.java
│   └── local
│       ├── TokenStorage.java              # 로그인 토큰
│       └── UserStorage.java               # 로그인 사용자 프로필 캐시
├── ui
│   ├── EdgeToEdgeUtil.java
│   ├── StepTextAnimator.java              # 분석 중 단계 안내 텍스트 애니메이션
│   ├── splash/{SplashActivity, SplashContract, SplashPresenter}.java   # S01, 로그인 분기 + 알림 권한 요청
│   ├── login/{LoginActivity, LoginContract, LoginPresenter}.java       # S02, 카카오 SDK 로그인(앱 우선, 웹 폴백)
│   ├── home/
│   │   ├── {HomeFragment, HomeContract, HomePresenter}.java            # S03
│   │   ├── TripRoomUiModel.java
│   │   └── adapter/TripRoomAdapter.java
│   ├── createroom/{CreateRoomActivity, CreateRoomContract, CreateRoomPresenter}.java   # S04
│   ├── upload/{ScheduleUploadActivity, UploadContract, UploadPresenter}.java           # S05
│   ├── analyzing/{AnalyzingActivity, AnalyzingContract, AnalyzingPresenter}.java       # S06
│   ├── analysisresult/{AnalysisResultActivity, AnalysisResultContract, AnalysisResultPresenter}.java  # S07
│   ├── review/{ScheduleReviewActivity, ReviewContract, ReviewPresenter}.java           # S08
│   ├── roomdetail/{RoomDetailActivity, RoomDetailContract, RoomDetailPresenter}.java   # S09, trip_id·초대코드 Intent로 전달
│   ├── invite/{InviteJoinActivity, InviteJoinContract, InviteJoinPresenter}.java       # 초대 링크 진입(딥링크)
│   ├── feedback/{WeatherFeedbackActivity, WeatherFeedbackContract, WeatherFeedbackPresenter}.java  # S10
│   ├── checklist/
│   │   ├── ChecklistActivity.java · ChecklistContract.java · ChecklistPresenter.java
│   │   ├── ChecklistCommonFragment.java      # S11
│   │   ├── ChecklistMineFragment.java        # S12
│   │   ├── ChecklistAssignmentFragment.java  # S13
│   │   └── ChecklistHost/ChecklistDataConsumer/ChecklistItemSelection/ChecklistItemVisibility/
│   │       ChecklistSelectionStore/ChecklistSelectionFilter/ChecklistProgressCalculator/
│   │       ChecklistDuplicateDetector.java    # 탭 간 선택·진행률·중복 계산 헬퍼
│   ├── archive/
│   │   ├── {TripArchiveFragment, ArchiveContract, ArchivePresenter}.java   # S14
│   │   ├── ArchiveTripUiModel.java
│   │   └── adapter/ArchiveTripAdapter.java
│   ├── profile/
│   │   ├── {ProfileFragment, ProfileContract, ProfilePresenter}.java       # S15
│   │   ├── {ProfileItemsActivity, ProfileItemsContract, ProfileItemsPresenter}.java   # 내 기본 물품 관리
│   │   ├── ProfileItemAdapter.java
│   │   └── PriorityLevels.java
│   ├── settings/{NotificationSettingsActivity, NotificationSettingsContract, NotificationSettingsPresenter}.java  # S16
│   ├── overlay/{AddItemSheet, EditItemSheet, EditFieldSheet, AssignItemSheet,
│   │           InviteShareSheet, RestrictionInfoSheet}.java
│   ├── atoms/{AvatarView, CheckboxView, ChipView, DDayBadgeView, IconButtonView,
│   │         PriorityDotView, RestrictionTagView, WeatherIconView}.java
│   ├── molecules/AvatarStackHelper.java
│   └── organisms/{TripRoomCardBinder, SwipeRevealHelper}.java
└── util
    ├── ImageCompressor.java
    ├── DateUtils.java
    ├── PrefsManager.java
    └── ReminderScheduler.java             # 기기 내부 스케줄 잔재. 알림은 서버 FCM으로 이관돼 현재 미사용
```

### 패키지별 책임

- `app`은 `AppContainer`로 `TokenStorage/UserStorage → ApiClient → 각 Api → Repository` 순으로 조립·보관합니다. 별도 DI 프레임워크(Dagger/Hilt) 없이 생성자 주입만 사용합니다.
- `service`는 FCM 백그라운드 서비스입니다. UI 패키지가 아니라 최상위에 독립적으로 있습니다.
- `model`은 화면이 실제로 사용하는 앱 내부 데이터를 정의합니다. Presenter가 Repository에서 받은 값을 Model로 다뤄 View에 전달합니다.
- `data.remote.api`는 EC2 PHP REST API의 Retrofit 요청을, `data.remote.adapter`는 서버 JSON의 관용 타입 파싱을 담당합니다.
- `data.remote.dto`는 서버의 요청·응답 JSON 형식을 기능별로 구분합니다.
- `data.mapper`는 서버 DTO를 Android Model로 변환합니다.
- `data.repository`는 화면이 사용할 데이터 접근 규칙과 구현체를 제공하며, `AppContainer`에 조립된 것만 실제로 쓰입니다.
- `data.local`은 로그인 token·사용자 프로필처럼 기기에 보관해야 하는 값만 관리합니다.
- `ui`는 각 화면을 `Activity/Fragment(View) + Contract + Presenter`로 나누고, 오버레이(`overlay`)와 `atoms`/`molecules`/`organisms` 공용 컴포넌트를 기능별 패키지로 구분합니다.

### 화면 연결 시 참고할 흐름

- S08 "방 생성 완료" 시점에 `tripRepository.createTrip()`을 호출해 서버가 반환한 `trip_id`부터 이후 화면에서 `TripRepository`를 사용합니다. (별도 "생성 세션" 단계 없이, `upload_id`/`analysis_id`만으로 여기까지 진행됩니다.)
- S09 `RoomDetailActivity`는 `trip_id`·방 이름·방장 여부·초대 코드·생성 직후 여부를 Intent extra로 받아 이후 화면(체크리스트 등)에 전달합니다.
- 초대 링크(`https://mybag.duckdns.org/invite/{code}` 또는 카카오 스킴)로 앱에 진입하면 `InviteJoinActivity`가 초대 코드를 받아 참여 처리하고, 미로그인 시 로그인 후 재개합니다.
- S10은 S09에서 진입하고 뒤로 가기로 복귀하며 체크리스트로 직접 이동하지 않습니다.
- 방 생성 시 설정한 `expected_member_count`가 1명이면 `ChecklistMineFragment`만, 2명 이상이면 체크리스트 3개 탭을 표시합니다. (현재 참여자 수가 아니라 설정 인원 기준)

---

## 9. EC2 PHP 서버 구조 (개발 전 계획안)

이 섹션은 개발 시작 전에 세운 계획이라 실제 구현과 다른 부분이 있습니다. 실제 서버는 "생성 세션" 리소스 없이 `upload_id → analysis_id → trip_id`를 체이닝하는 방식으로 갔고, 라우팅도 `/api/trips/{tripId}` 같은 경로 파라미터 대신 `html/api/도메인/동작.php` 형태의 파일 기반 엔드포인트로 구현했습니다. 최신 API 스펙은 `mybag` 백엔드 레포의 `APIs.md`, 서버 구현 히스토리는 `내가방서버현황.md`를 참고해주세요. 아래는 초기 설계 의도를 남겨두는 용도입니다.

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
└── FCM HTTP v1 API · 발송 구현 완료
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

## 10. MySQL 데이터 구조

계획 단계에서는 "생성 세션"이라는 별도 테이블을 두는 안을 검토했지만, 실제로는 그런 중간 테이블 없이 `trip_uploads`/`ai_analyses`를 `trip_id NULL` 상태로 먼저 만들고 방 생성이 확정되는 시점에 `UPDATE`로 채우는 방식으로 구현했습니다. 아래는 서버에 실제로 올라가 있는 13개 테이블 구조입니다.

### 설계 기준

- MySQL 8.0, InnoDB, `utf8mb4_unicode_ci`를 씁니다.
- Android는 MySQL에 직접 붙지 않고 EC2 PHP REST API를 통해서만 접근합니다.
- PK는 `BIGINT UNSIGNED AUTO_INCREMENT`, boolean은 `TINYINT(1)`, 시간은 전부 UTC `DATETIME`으로 저장합니다.
- 업로드 이미지는 MySQL에 BLOB으로 넣지 않고 EC2 서버 경로에 파일로 저장한 뒤 경로만 저장합니다.
- 삭제가 필요한 데이터 중 관계 보존이 중요한 건(체크리스트 항목, 초대 코드 등) 실제 DELETE 대신 상태 컬럼으로 소프트 삭제합니다.
- 진행중/지난 여행 구분은 별도 상태값이 아니라 `end_date` 기준으로 조회합니다.

### 테이블 관계

```text
users
├── notification_settings (1:1, 가입 시 기본값 행 자동 생성)
├── fcm_tokens (1:N)
├── user_default_items (1:N, 프로필 "내 기본 물품")
├── trips (owner_user_id, 1:N)
└── trip_members (1:N, 여러 여행방에 참여자로 소속)

trips
├── trip_members (1:N, OWNER/MEMBER)
├── trip_invites (1:N, 초대 코드)
├── trip_uploads (1:N, 일정·숙소 캡처 이미지)
├── ai_analyses (1:N, 분석 실행마다 새 행)
├── packing_items (1:N, 체크리스트)
└── notification_log (1:N, D-day 알림 중복 발송 방지)
```

### CREATE TABLE

```sql
-- 1. users
CREATE TABLE users (
  user_id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  kakao_user_id      VARCHAR(100)  NOT NULL,
  email              VARCHAR(255)  NULL,
  nickname           VARCHAR(50)   NOT NULL,
  profile_image_url  VARCHAR(500)  NULL,
  account_status     VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',   -- ACTIVE, WITHDRAWN, BLOCKED
  last_login_at      DATETIME      NULL,
  created_at         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_kakao_user_id (kakao_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. trips
CREATE TABLE trips (
  trip_id                BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  owner_user_id          BIGINT UNSIGNED NOT NULL,
  trip_name              VARCHAR(100) NOT NULL,
  expected_member_count  INT UNSIGNED NOT NULL DEFAULT 1,
  trip_type              VARCHAR(20)  NULL,   -- DOMESTIC, OVERSEAS
  destination_country    VARCHAR(100) NULL,
  destination_city       VARCHAR(100) NULL,
  start_date             DATE NULL,
  end_date               DATE NULL,
  status                 VARCHAR(20)  NOT NULL DEFAULT 'CREATED',
  created_at             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_trips_owner FOREIGN KEY (owner_user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. trip_members
CREATE TABLE trip_members (
  trip_member_id  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  trip_id         BIGINT UNSIGNED NOT NULL,
  user_id         BIGINT UNSIGNED NOT NULL,
  role            VARCHAR(20) NOT NULL DEFAULT 'MEMBER',   -- OWNER, MEMBER
  member_status   VARCHAR(20) NOT NULL DEFAULT 'JOINED',   -- JOINED, LEFT, KICKED (나가기/강퇴는 MVP 범위 밖)
  joined_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  left_at         DATETIME NULL,
  UNIQUE KEY uk_trip_user (trip_id, user_id),
  CONSTRAINT fk_trip_members_trip FOREIGN KEY (trip_id) REFERENCES trips(trip_id) ON DELETE CASCADE,
  CONSTRAINT fk_trip_members_user FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. trip_invites
CREATE TABLE trip_invites (
  invite_id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  trip_id             BIGINT UNSIGNED NOT NULL,
  created_by_user_id  BIGINT UNSIGNED NOT NULL,
  invite_code         VARCHAR(100) NOT NULL,
  max_uses            INT UNSIGNED NULL,
  used_count          INT UNSIGNED NOT NULL DEFAULT 0,
  expires_at          DATETIME NULL,
  is_active           TINYINT(1) NOT NULL DEFAULT 1,
  UNIQUE KEY uk_invite_code (invite_code),
  CONSTRAINT fk_trip_invites_trip FOREIGN KEY (trip_id) REFERENCES trips(trip_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. trip_uploads (사진 원본은 저장하지 않고 file_path만 저장)
-- trip_id는 NULL 허용 — 업로드 시점엔 아직 방이 없고, 방 생성이 확정되면 UPDATE로 채워짐
CREATE TABLE trip_uploads (
  upload_id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  trip_id              BIGINT UNSIGNED NULL,
  uploader_user_id     BIGINT UNSIGNED NOT NULL,
  upload_type          VARCHAR(20) NOT NULL,   -- ITINERARY, ACCOMMODATION, EXTRA
  file_path            VARCHAR(500) NOT NULL,
  original_file_name   VARCHAR(255) NULL,
  mime_type            VARCHAR(100) NULL,
  file_size            BIGINT UNSIGNED NULL,
  sort_order           INT UNSIGNED NOT NULL DEFAULT 0,
  upload_status        VARCHAR(20) NOT NULL DEFAULT 'UPLOADED',   -- UPLOADING, UPLOADED, ANALYZED, FAILED, DELETED
  created_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_trip_uploads_trip FOREIGN KEY (trip_id) REFERENCES trips(trip_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. ai_analyses (재분석해도 덮어쓰지 않고 새 행을 추가)
CREATE TABLE ai_analyses (
  analysis_id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  trip_id               BIGINT UNSIGNED NULL,
  requested_by_user_id  BIGINT UNSIGNED NOT NULL,
  status                VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING, PROCESSING, SUCCEEDED, FAILED, CANCELLED
  input_upload_ids      JSON NULL,
  result_json           JSON NULL,   -- { trip, accommodation, weather[], restrictions[] }
  error_message         VARCHAR(500) NULL,
  created_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_ai_analyses_trip FOREIGN KEY (trip_id) REFERENCES trips(trip_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. packing_items (물품 1개 = 담당자 1명)
CREATE TABLE packing_items (
  packing_item_id     BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  trip_id             BIGINT UNSIGNED NOT NULL,
  analysis_id         BIGINT UNSIGNED NULL,
  item_name           VARCHAR(100) NOT NULL,
  category            VARCHAR(50) NULL,
  priority            VARCHAR(20) NOT NULL DEFAULT 'OPTIONAL',   -- REQUIRED, RECOMMENDED, OPTIONAL
  item_scope          VARCHAR(20) NOT NULL DEFAULT 'COMMON',     -- COMMON, PERSONAL
  source              VARCHAR(20) NOT NULL,                      -- AI, USER, DEFAULT
  restriction_type    VARCHAR(30) NULL,    -- PROHIBITED, CARRY_ON_ONLY, CHECKED_ONLY, LIMITED, CAUTION
  restriction_reason  VARCHAR(500) NULL,
  assignee_user_id    BIGINT UNSIGNED NULL,   -- NULL이면 미지정, 공용 물품도 미지정 가능
  is_completed        TINYINT(1) NOT NULL DEFAULT 0,
  item_status         VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',   -- ACTIVE, EXCLUDED, DELETED
  sort_order          INT UNSIGNED NOT NULL DEFAULT 0,
  created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_packing_items_trip FOREIGN KEY (trip_id) REFERENCES trips(trip_id) ON DELETE CASCADE,
  CONSTRAINT fk_packing_items_analysis FOREIGN KEY (analysis_id) REFERENCES ai_analyses(analysis_id),
  CONSTRAINT fk_packing_items_assignee FOREIGN KEY (assignee_user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. user_default_items (프로필 "내 기본 물품")
CREATE TABLE user_default_items (
  default_item_id   BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id            BIGINT UNSIGNED NOT NULL,
  item_name          VARCHAR(100) NOT NULL,
  category           VARCHAR(50) NULL,
  default_priority   VARCHAR(20) NOT NULL DEFAULT 'OPTIONAL',
  is_active          TINYINT(1) NOT NULL DEFAULT 1,
  UNIQUE KEY uk_user_item (user_id, item_name),
  CONSTRAINT fk_user_default_items_user FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. notification_settings (가입 시 users와 함께 기본값 행 생성, 전부 켜짐)
CREATE TABLE notification_settings (
  user_id                   BIGINT UNSIGNED PRIMARY KEY,
  d7_enabled                TINYINT(1) NOT NULL DEFAULT 1,
  d3_enabled                TINYINT(1) NOT NULL DEFAULT 1,
  d1_enabled                TINYINT(1) NOT NULL DEFAULT 1,
  assignment_enabled        TINYINT(1) NOT NULL DEFAULT 1,
  checklist_change_enabled  TINYINT(1) NOT NULL DEFAULT 1,
  weather_enabled           TINYINT(1) NOT NULL DEFAULT 1,
  CONSTRAINT fk_notification_settings_user FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. fcm_tokens
CREATE TABLE fcm_tokens (
  fcm_token_id   BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id        BIGINT UNSIGNED NOT NULL,
  token          VARCHAR(512) NOT NULL,
  device_id      VARCHAR(255) NULL,
  platform       VARCHAR(20) NOT NULL DEFAULT 'ANDROID',
  is_active      TINYINT(1) NOT NULL DEFAULT 1,
  last_seen_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_token (token),
  CONSTRAINT fk_fcm_tokens_user FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. api_cache (호출 한도가 있는 외부 API 응답 캐싱, 현재 날씨 API가 사용)
CREATE TABLE api_cache (
  cache_id      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  cache_key     VARCHAR(255) NOT NULL,
  response_json JSON NOT NULL,
  expires_at    DATETIME NOT NULL,
  UNIQUE KEY uk_cache_key (cache_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. notification_log (D-7/D-3/D-1 알림 중복 발송 방지 + 배치가 하루 스킵돼도 캐치업)
-- (trip_id, user_id, notify_type) 조합당 한 행만 존재 — 있으면 이미 보낸 것
CREATE TABLE notification_log (
  notification_log_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  trip_id      BIGINT UNSIGNED NOT NULL,
  user_id      BIGINT UNSIGNED NOT NULL,
  notify_type  VARCHAR(20) NOT NULL,  -- D7, D3, D1
  sent_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_trip_user_type (trip_id, user_id, notify_type),
  CONSTRAINT fk_notification_log_trip FOREIGN KEY (trip_id) REFERENCES trips(trip_id),
  CONSTRAINT fk_notification_log_user FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. auth_sessions
-- JWT를 stateless 30일 만료로 발급하는 방식이라 refresh token 갱신·폐기가 필요 없어서 현재는 생성만 해두고 미사용
```

### 체크리스트 동작

- 물품은 공용(`COMMON`)·개인(`PERSONAL`) 스코프와 담당자 지정이 각각 별도 컬럼입니다. 공용 물품도 담당자 미지정 상태로 둘 수 있습니다.
- 체크 여부(`is_completed`)는 실제로 컬럼에 저장하고, 진행률은 매번 계산합니다.
- 항목 삭제는 물리 삭제가 아니라 `item_status = 'DELETED'`로 처리합니다.
- 재분석을 실행하면 이전 AI 항목은 소프트 삭제되고 새 항목이 들어오지만, 사용자가 직접 추가한(`source = 'USER'`) 항목은 그대로 남습니다.
- 현재는 물품 하나에 담당자 한 명만 지정할 수 있는 구조입니다. 여러 명 배정이 필요해지면 별도 매핑 테이블로 분리할 수 있습니다.

### 알림

- 담당자 지정, 출발 D-7·D-3·D-1 알림은 `notification_settings`의 사용자별 토글 값을 보고 발송 대상을 정합니다.
- `notification_log`로 같은 트립·같은 사람·같은 타입 알림이 중복 발송되지 않게 막습니다.
- 날씨 API 응답은 `api_cache`에 짧게 캐시해서 호출 횟수를 아낍니다.

### 환경변수

```text
DB_HOST, DB_USER, DB_PASS, DB_NAME
JWT_SECRET
KAKAO_REST_API_KEY, KAKAO_CLIENT_SECRET, KAKAO_REDIRECT_URI
OPENAI_API_KEY, OPENAI_MODEL
OPENWEATHER_API_KEY
FCM_PROJECT_ID, FCM_SERVICE_ACCOUNT_PATH
```

API Key, DB 비밀번호, 로그인 토큰 원문은 MySQL, Android 소스, GitHub README 어디에도 저장하지 않습니다.

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
