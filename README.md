여행가방 대신 싸줘

여행 일정표·숙소 예약 화면과 짐 사진을 AI로 분석하고, 여행지 날씨와 항공기 반입 제한 정보를 바탕으로 준비물 목록을 만든 뒤 동행자와 담당 물품 및 준비 상태를 공유하는 Android 애플리케이션입니다.

> 본 문서는 Manyfast의 기능명세서와 유저플로우를 기준으로 작성한 개발 전 기술 설계 문서입니다.  
> 개발 환경은 Android Java + XML Layout + MVP(Model-View-Presenter)패턴을 기준으로 합니다.

1. 정리하는 범위

- 기능별로 필요한 외부 API와 선정 이유
- API별 무료 구간과 예상 비용
- Room만 사용할 수 있는지와 서버가 필요한 이유
- Java MVP 기준 전체 패키지 구조
- 주요 클래스 이름과 클래스별 책임
- 기능명세·유저플로우와 화면/Presenter/Repository 연결 관계
- Firestore 데이터 구조
- 보안, 개인정보, AI 오분석 등의 주요 위험

제외하는 내용은 다음과 같습니다.

- 함수 내부 구현 코드
- 모든 메서드의 상세 매개변수와 반환형
- XML 화면 디자인 완성본
- 전체 국가·항공사의 반입 규정 수집
- 배포 자동화와 운영 모니터링 상세 설정

즉, `어떤 클래스가 필요하고 각 클래스가 무엇을 담당하는지`까지 작성하고 함수 내부 구현은 개발 단계에서 결정합니다.

2. 기능명세 기준 핵심 기능

1. 여행 일정 캡처 자동 분석 (일정·숙소 캡처 업로드, AI 분석, 결과 검토·수정)
2. 장소 기반 날씨·의식주 피드백 (여행지 좌표 조회, 일정별 날씨, 옷·생활 준비물 추천)
3. 짐 사진 기반 금지·추천 물품 안내 (짐 사진 인식, 반입 제한 매칭, 중요도 분류)
4. 중요도별 통합 체크리스트 (필수·중간·선택 분류, 준비 상태 관리)
5. 동행자 물품 분담 (담당자 지정·취소, 구성원별 분담 현황)
6. 계정·알림 (카카오 로그인, 여행 D-day 알림)
7. 홈·여행방 관리 (방 생성, 초대 링크, 참여자 목록, 분석 화면 진입)
8. 공용 리스트 (공용/개인 목록, 담당 지정, v/x 최종 확정)
9. 프로필·개인 물품 (개인 물품 추가·수정·삭제)
10. 공용 여행 아카이브 (참여한 여행방 목록과 상세 화면 이동)

핵심 유저플로우

앱 시작
  → 카카오 로그인
  → 홈(여행방 목록)
  → 방 만들기 또는 초대 링크 참여
  → 여행방 상세·참여자 확인
  → 일정 캡처 업로드
  → AI 분석
  → 날짜·장소 검토 및 수동 수정
  → 목록 아이템 생성
  → 날씨·의식주 피드백 확인
  → 짐 사진 업로드 및 분석
  → 공용 체크리스트
  → 담당 물품 지정
  → 개인 목록 v/x 최종 확정
  → 분담 현황·아카이브·D-day 알림

3. API 선정

1. 일정·숙소 캡처 분석
- 사용할 API 서비스 : OpenAI Responses API gpt-5.4-mini
- 사용 목적 : 이미지에서 날짜, 도시, 국가, 숙소 정보 추출
- 선정 이유 :이미지 입력과 Structured Outputs 지원

2. 짐 사진 분석
- 사용할 API 서비스 : OpenAI Responses API gpt-5.4-mini
- 사용 목적 : 사진 속 물품 후보 인식 및 분류
- 선정 이유 : 일정 분석과 같은 API를 사용해 개발 범위 축소

3. 날씨
- 사용할 API 서비스 : Open-Meteo Forecast API
- 사용 목적 : 일정별 기온, 강수, 날씨 코드 조회
- 선정 이유 : 비상업 프로젝트는 키 없이 무료 사용 가능

4. 장소 좌표
- 사용할 API 서비스 : Open-Meteo Geocoding API
- 사용 목적 : 도시명을 위도·경도로 변환
- 선정 이유 : 날씨 API와 함께 사용할 수 있어 구현이 단순함

5. 로그인
- 사용할 API 서비스 : MVP (Firebase Anonymous Auth) 이후 Kakao Login
- 사용 목적 : 사용자 식별 및 카카오 계정 로그인
- 선정 이유 : 3일 개발에서는 익명 로그인으로 먼저 전체 흐름 완성

6. 공유 데이터
- 사용할 API 서비스 : Cloud Firestore
- 사용 목적 : 여행방·멤버·체크리스트 실시간 동기화
- 선정 이유 : Android 실시간 Listener와 오프라인 캐시 제공

7. AI 호출 서버
- 사용할 API 서비스 : Firebase Cloud Functions 2nd gen
- 사용 목적 : OpenAI API Key 보호, 인증, 입력 검증
- 선정 이유 : API Key를 APK에 포함하지 않기 위해 필요

8. 알림
- 사용할 API 서비스 : WorkManager + FCM
- 사용 목적 : D-day 로컬 알림, 담당자 변경 푸시
- 선정 이유 : WorkManager와 FCM 모두 직접 사용 비용이 없음

9. 반입 제한 정보
- 사용할 API 서비스 : 국토교통부·한국교통안전공단 공공데이터
- 사용 목적 : AI가 인식한 물품을 국내 반입 제한 목록과 비교
- 선정 이유 : AI 답변만 사용하지 않고 공식 데이터와 교차 확인

API 사용 원칙

- OpenAI API Key는 Android 프로젝트 또는 `BuildConfig`에 넣지 않습니다.
- Android 앱은 인증된 서버 API만 호출합니다.
- AI 결과는 자동 확정하지 않고 항상 사용자가 검토·수정합니다.
- 반입 제한 결과에는 데이터 기준일과 공식 확인 안내를 표시합니다.
- 여행 캡처와 짐 사진은 압축 후 전달하고 분석 완료 후 즉시 폐기합니다.

4. 예상 비용

가격은 2026-08-04 공식 가격표 확인 기준이며, 실제 비용은 이미지 크기와 출력 길이에 따라 달라질 수 있습니다.

OpenAI

gpt-5.4-mini 기준:
- 입력: USD 0.75 / 1M tokens
- 출력: USD 4.50 / 1M tokens

여행 1건을 다음과 같이 가정합니다.
- 일정 캡처 3장
- 짐 사진 1장
- AI 호출 2회
- 총 입력 약 7,000 tokens
- 총 출력 약 2,000 tokens
- 계획 환율 1 USD = 1,400원

입력 비용 = 7,000 / 1,000,000 × $0.75 = $0.00525
출력 비용 = 2,000 / 1,000,000 × $4.50 = $0.00900
여행 1건 = 약 $0.01425 = 약 20원

월 이용 규모 : OpenAI 예상 비용 / Firebase/Open-Meteo = 월 합계 계획치 
- 해커톤 30여행 : 약 600원 / 무료 구간 예상 = 약 600원
- 파일럿 300여행 : 약 6,000원 / 무료 구간 예상 = 약 6,000원
- 초기 3,000여행 : 약 60,000원 / 사용량에 따라 소액 발생 가능 = 약 6만~8만원

Firebase 무료 구간
- Firestore 저장 공간: 1 GiB
- 문서 읽기: 50,000회/일
- 문서 쓰기: 20,000회/일
- 문서 삭제: 20,000회/일
- 네트워크 송신: 10 GiB/월
- Cloud Functions 호출: 2,000,000회/월 무료 구간
- FCM: 무료

Cloud Functions를 사용하려면 Blaze 요금제와 결제 계정 연결이 필요합니다. Blaze는 무료 할당량을 초과한 만큼 과금되는 방식이므로 해커톤 사용량에서는 Firebase 비용이 거의 발생하지 않을 것으로 예상합니다.

비용 제한 방법

- 업로드 이미지는 긴 변 1,600px 이하로 압축
- 일정 이미지 최대 3장, 짐 사진 최대 1장으로 제한
- AI 출력 token 수 제한
- 동일 이미지 분석 결과 캐시
- 재분석 버튼 연속 호출 제한
- Firebase 예산 알림 설정
- 인증 사용자만 AI API 호출 허용

5. Room만 사용할 수 있는가?

Room은 한 기기 안에 데이터를 저장하는 로컬 데이터베이스입니다.

요구사항          
1. 내 휴대폰에 목록 저장 
- Room만 사용 : 가능
- Firestore 사용 : 가능

2. 동행자와 실시간 공유
- Room만 사용 : 불가능
- Firestore 사용 : 가능

3. 초대 링크·여행방 참여 
- Room만 사용 : 불가능
- Firestore 사용 : 가능

4. 재설치·기기 변경 복구 
- Room만 사용 : 불가능
- Firestore 사용 : 가능

5. OpenAI API Key 보호 
- Room만 사용 : 불가능
- Firestore 사용 : 서버와 함께 가능

6. 오프라인 사용 
- Room만 사용 : 가능
- Firestore 사용 : Android SDK 캐시 지원

결론
- 동행자 공유 기능 때문에 서버 데이터베이스가 필요합니다.
- Room은 이후 개인 준비물 템플릿, 최근 검색, 업로드 대기열이 필요할 때 추가합니다.
- OpenAI API Key 보호를 위해 최소한의 서버도 필요합니다.

6. 전체 패키지 구조

기본 패키지는 `com.team.packmate`입니다. 아래처럼 패키지마다 제목을 나누고 클래스는 한 줄에 하나씩 작성합니다.

app
앱 실행과 공용 객체 생성을 담당합니다.

- PackMateApplication.java
- AppContainer.java
- BaseActivity.java

common
여러 기능에서 함께 사용하는 결과, 오류, 상수를 관리합니다.

- AppResult.java
- AppError.java
- Constants.java

model
앱에서 사용하는 데이터 형태를 정의합니다.

- User.java
- Trip.java
- TripMember.java
- Itinerary.java
- PackingItem.java
- AnalysisResult.java
- Weather.java

data.repository
Presenter가 사용할 데이터 접근 규칙과 실제 구현체입니다.

- AuthRepository.java
- AuthRepositoryImpl.java
- TripRepository.java
- TripRepositoryImpl.java
- PackingRepository.java
- PackingRepositoryImpl.java
- AnalysisRepository.java
- AnalysisRepositoryImpl.java
- WeatherRepository.java
- WeatherRepositoryImpl.java

data.remote.firebase

Firebase Authentication과 Firestore 통신을 담당합니다.

- FirebaseAuthDataSource.java
- FirestoreTripDataSource.java

data.remote.backend

OpenAI API Key를 보관한 서버와 통신합니다.

- BackendApi.java
- AnalysisDto.java
- KakaoTokenDto.java

data.remote.weather

Open-Meteo API를 호출하고 날씨 응답을 받습니다.

- OpenMeteoApi.java
- WeatherDto.java

data.mapper

외부 응답 데이터를 앱의 Model 객체로 변환합니다.

- TripMapper.java
- AnalysisMapper.java
- WeatherMapper.java

feature.auth

로그인 화면 기능입니다.

- LoginContract.java
- LoginActivity.java
- LoginPresenter.java

feature.home

여행방 목록을 표시하는 홈 기능입니다.

- HomeContract.java
- HomeActivity.java
- HomePresenter.java

feature.trip

여행방 생성, 초대, 참여자 확인 기능입니다.

- TripRoomContract.java
- TripRoomActivity.java
- TripRoomPresenter.java
- CreateTripDialog.java

feature.itinerary

일정 캡처 업로드, AI 분석, 검토·수정 기능입니다.

- ItineraryContract.java
- ItineraryUploadActivity.java
- ItineraryReviewActivity.java
- ItineraryPresenter.java
- ItineraryAdapter.java

feature.weather

여행지 날씨와 의식주 피드백 기능입니다.

- WeatherContract.java
- WeatherFragment.java
- WeatherPresenter.java

feature.baggage

짐 사진 분석과 반입 제한·추천 물품 확인 기능입니다.

- BaggageContract.java
- BaggageUploadActivity.java
- BaggageResultActivity.java
- BaggagePresenter.java
- BaggageItemAdapter.java

feature.checklist

공용·개인 체크리스트와 담당자 지정 기능입니다.

- ChecklistContract.java
- ChecklistFragment.java
- ChecklistPresenter.java
- PackingItemAdapter.java

feature.profile

개인 준비물 추가·수정·삭제 기능입니다.

- ProfileContract.java
- ProfileFragment.java
- ProfilePresenter.java

feature.archive

참여했던 여행방 목록과 상세 이동 기능입니다.

- ArchiveContract.java
- ArchiveFragment.java
- ArchivePresenter.java

util

이미지 압축, 날짜 변환, D-day 알림을 담당합니다.

- ImageCompressor.java
- DateUtils.java
- ReminderScheduler.java

7. Firestore 데이터 구조
users/{uid}
trips/{tripId}
trips/{tripId}/members/{uid}
trips/{tripId}/itineraries/{itineraryId}
trips/{tripId}/packingItems/{itemId}
trips/{tripId}/analyses/{analysisId}

8. 경로 및 주요 데이터 
- users/{uid} : 닉네임, 프로필 이미지, 생성일
- trips/{tripId} : 방 이름, 방장, 여행 기간, 여행지, 초대코드 해시
- members/{uid} : 역할, 표시 이름, 참여일
- itineraries/{id}` : 날짜, 도시, 숙소, AI 신뢰도, 사용자 확정 여부
- packingItems/{id} : 이름, 카테고리, 중요도, 담당자, 준비 상태, 생성 출처 
- analyses/{id} : 분석 종류, 상태, 결과, 모델, 프롬프트 버전, 생성일 

원본 이미지는 Firestore에 저장하지 않습니다.

11. 위험 요소와 대응
- AI 일정·물품 오인식 : 신뢰도와 근거 표시, 검토 화면에서 사용자 확정 필수 
- 국가·항공사별 규정 차이 : 국내 출발 기준 MVP, 공식 사이트 최종 확인 안내
- OpenAI API Key 유출 : APK 저장 금지, 서버 Secret으로 관리 
- 예약 캡처 개인정보 : 이미지 메타데이터 제거, 분석 후 즉시 폐기, 원문 로그 금지 
- Firestore 읽기 비용 증가 : 여행방 단위 Query, 화면 종료 시 Listener 해제 
- 카카오 로그인 개발 지연 : 익명 로그인으로 전체 기능을 먼저 완성 
- 외부 API 장애 : 수동 입력 경로, 재시도 1회, 마지막 성공 데이터 표시
