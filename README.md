# 여행가방 대신 싸줘

여행 일정표와 숙소 예약 화면 등 **여러 장의 캡처 이미지**를 AI로 분석하여 여행 날짜, 장소, 국가, 숙소 정보를 추출하고, 여행지 날씨와 항공기 반입 제한 정보를 바탕으로 준비물 목록을 생성하는 Android 애플리케이션입니다.

생성한 준비물은 같은 여행방에 참여한 동행자들과 공유하며, 담당자와 준비 상태를 함께 관리합니다.

> 이 문서는 Manyfast의 기능명세서와 유저플로우를 기준으로 작성했습니다.  
> Android는 **Java + XML Layout + MVP(Model-View-Presenter)** 패턴을 사용합니다.  
> 서버는 구축이 완료된 **AWS EC2 + Apache2 + PHP + MySQL** 환경을 사용합니다.

---

## 1. 문서화 범위

이 README는 팀원들이 개발 전에 구조를 이해하고 기능별 담당을 나눌 수 있는 수준까지 작성합니다.

- 사용할 API와 선정 이유
- API 및 서버 예상 비용
- Android Java MVP 구조
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
- FCM 푸시 알림 연동

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
- 국토교통부·한국교통안전공단 반입 제한 공공데이터
- Firebase Cloud Messaging - 추후 연결 예정

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
- 초대 코드 또는 링크로 동행자가 참여합니다.
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

- MVP에서는 WorkManager로 기기 내부 D-day 알림을 구현합니다.
- 담당자 지정과 공용 목록 변경 푸시는 FCM 연결 후 구현합니다.

---

## 4. 유저플로우

1. 앱을 실행합니다.
2. 카카오 계정으로 로그인합니다.
3. EC2 서버가 카카오 토큰을 확인하고 앱 전용 로그인 토큰을 발급합니다.
4. 홈에서 참여 중인 여행방을 확인합니다.
5. 방을 만들거나 초대 코드로 여행방에 참여합니다.
6. 여행방 상세에서 참여자 목록을 확인합니다.
7. 여행 일정표와 숙소 예약 캡처를 여러 장 업로드합니다.
8. EC2 서버가 OpenAI API에 분석을 요청합니다.
9. 사용자가 날짜, 장소, 국가, 숙소 정보를 검토하고 수정합니다.
10. 사용자가 목록 생성 버튼을 누릅니다.
11. EC2 서버가 날씨와 기본 준비물 규칙을 결합해 체크리스트를 생성합니다.
12. 참여자가 준비물 담당자를 지정합니다.
13. 개인 목록에서 준비 상태를 v/x로 확정합니다.
14. 여행 종료 후 해당 여행방을 아카이브에서 확인합니다.
15. 여행 전 WorkManager 또는 추후 FCM 알림으로 최종 점검합니다.

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

- 구현 예정

추후 사용 목적:

- 여행 D-day 푸시 알림
- 준비물 담당자 지정 알림
- 공용 체크리스트 변경 알림

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
- FCM은 추후 PHP 서버와 연결합니다.

### 데이터 흐름

1. Android 앱이 로그인 토큰과 요청 데이터를 EC2 서버로 전송합니다.
2. Apache2가 PHP API 요청을 처리합니다.
3. PHP 서버가 로그인 토큰과 여행방 권한을 검사합니다.
4. 필요한 경우 PHP 서버가 OpenAI 또는 Open-Meteo를 호출합니다.
5. 서버가 결과를 검증하고 MySQL에 저장합니다.
6. Android 앱이 JSON 응답을 받아 화면에 표시합니다.

---

## 8. Android 패키지 구조

```text
com.team.packmate
├── app
│   ├── PackMateApplication.java
│   ├── AppContainer.java
│   └── BaseActivity.java
├── common
│   ├── AppResult.java
│   ├── AppError.java
│   └── Constants.java
├── model
│   ├── User.java
│   ├── Trip.java
│   ├── TripMember.java
│   ├── Itinerary.java
│   ├── PackingItem.java
│   ├── AnalysisResult.java
│   └── Weather.java
├── data
│   ├── repository
│   │   ├── AuthRepository.java
│   │   ├── AuthRepositoryImpl.java
│   │   ├── TripRepository.java
│   │   ├── TripRepositoryImpl.java
│   │   ├── PackingRepository.java
│   │   ├── PackingRepositoryImpl.java
│   │   ├── AnalysisRepository.java
│   │   ├── AnalysisRepositoryImpl.java
│   │   ├── WeatherRepository.java
│   │   └── WeatherRepositoryImpl.java
│   ├── remote
│   │   └── server
│   │       ├── ApiClient.java
│   │       ├── AuthApi.java
│   │       ├── TripApi.java
│   │       ├── AnalysisApi.java
│   │       ├── PackingApi.java
│   │       ├── WeatherApi.java
│   │       └── ArchiveApi.java
│   ├── dto
│   │   ├── LoginRequestDto.java
│   │   ├── LoginResponseDto.java
│   │   ├── TripDto.java
│   │   ├── ItineraryDto.java
│   │   ├── PackingItemDto.java
│   │   ├── AnalysisResponseDto.java
│   │   └── WeatherDto.java
│   └── mapper
│       ├── UserMapper.java
│       ├── TripMapper.java
│       ├── ItineraryMapper.java
│       ├── PackingItemMapper.java
│       ├── AnalysisMapper.java
│       └── WeatherMapper.java
├── feature
│   ├── auth
│   │   ├── LoginContract.java
│   │   ├── LoginActivity.java
│   │   └── LoginPresenter.java
│   ├── home
│   │   ├── HomeContract.java
│   │   ├── HomeActivity.java
│   │   └── HomePresenter.java
│   ├── trip
│   │   ├── TripRoomContract.java
│   │   ├── TripRoomActivity.java
│   │   ├── TripRoomPresenter.java
│   │   └── CreateTripDialog.java
│   ├── itinerary
│   │   ├── ItineraryContract.java
│   │   ├── ItineraryUploadActivity.java
│   │   ├── ItineraryReviewActivity.java
│   │   ├── ItineraryPresenter.java
│   │   └── ItineraryAdapter.java
│   ├── weather
│   │   ├── WeatherContract.java
│   │   ├── WeatherFragment.java
│   │   └── WeatherPresenter.java
│   ├── checklist
│   │   ├── ChecklistContract.java
│   │   ├── ChecklistFragment.java
│   │   ├── ChecklistPresenter.java
│   │   └── PackingItemAdapter.java
│   ├── profile
│   │   ├── ProfileContract.java
│   │   ├── ProfileFragment.java
│   │   └── ProfilePresenter.java
│   └── archive
│       ├── ArchiveContract.java
│       ├── ArchiveFragment.java
│       └── ArchivePresenter.java
└── util
    ├── ImageCompressor.java
    ├── DateUtils.java
    ├── TokenManager.java
    └── ReminderScheduler.java
```

기본 패키지는 `com.team.packmate`입니다. 위 트리의 패키지별 책임은 아래에서 설명합니다.

### app

앱 실행과 공용 객체 생성을 담당합니다.

- `PackMateApplication.java`
- `AppContainer.java`
- `BaseActivity.java`

### common

공통 결과, 오류, 상수를 관리합니다.

- `AppResult.java`
- `AppError.java`
- `Constants.java`

### model

Android 앱에서 사용하는 데이터 모델입니다.

- `User.java`
- `Trip.java`
- `TripMember.java`
- `Itinerary.java`
- `PackingItem.java`
- `AnalysisResult.java`
- `Weather.java`

### data.repository

Presenter가 사용할 데이터 접근 규칙과 구현체입니다.

- `AuthRepository.java`
- `AuthRepositoryImpl.java`
- `TripRepository.java`
- `TripRepositoryImpl.java`
- `PackingRepository.java`
- `PackingRepositoryImpl.java`
- `AnalysisRepository.java`
- `AnalysisRepositoryImpl.java`
- `WeatherRepository.java`
- `WeatherRepositoryImpl.java`

### data.remote.server

AWS EC2 PHP REST API와 통신합니다.

- `ApiClient.java`
- `AuthApi.java`
- `TripApi.java`
- `AnalysisApi.java`
- `PackingApi.java`
- `WeatherApi.java`
- `ArchiveApi.java`

### data.dto

서버 요청과 응답 형식을 정의합니다.

- `LoginRequestDto.java`
- `LoginResponseDto.java`
- `TripDto.java`
- `ItineraryDto.java`
- `PackingItemDto.java`
- `AnalysisResponseDto.java`
- `WeatherDto.java`

### data.mapper

서버 DTO를 Android Model로 변환합니다.

- `UserMapper.java`
- `TripMapper.java`
- `ItineraryMapper.java`
- `PackingItemMapper.java`
- `AnalysisMapper.java`
- `WeatherMapper.java`

### feature.auth

카카오 로그인과 EC2 서버 인증 기능입니다.

- `LoginContract.java`
- `LoginActivity.java`
- `LoginPresenter.java`

### feature.home

참여 중인 여행방 목록을 표시합니다.

- `HomeContract.java`
- `HomeActivity.java`
- `HomePresenter.java`

### feature.trip

여행방 생성, 초대, 참여자 관리 기능입니다.

- `TripRoomContract.java`
- `TripRoomActivity.java`
- `TripRoomPresenter.java`
- `CreateTripDialog.java`

### feature.itinerary

여러 장의 일정 캡처 업로드, 분석, 검토·수정 기능입니다.

- `ItineraryContract.java`
- `ItineraryUploadActivity.java`
- `ItineraryReviewActivity.java`
- `ItineraryPresenter.java`
- `ItineraryAdapter.java`

### feature.weather

여행지 날씨와 의식주 피드백을 표시합니다.

- `WeatherContract.java`
- `WeatherFragment.java`
- `WeatherPresenter.java`

### feature.checklist

공용·개인 준비물과 담당자, 준비 상태를 관리합니다.

- `ChecklistContract.java`
- `ChecklistFragment.java`
- `ChecklistPresenter.java`
- `PackingItemAdapter.java`

### feature.profile

개인 준비물 추가·수정·삭제 기능입니다.

- `ProfileContract.java`
- `ProfileFragment.java`
- `ProfilePresenter.java`

### feature.archive

종료된 여행방 목록과 상세 내용을 표시합니다.

- `ArchiveContract.java`
- `ArchiveFragment.java`
- `ArchivePresenter.java`

### util

이미지 압축, 날짜 변환, 로그인 토큰, 알림을 담당합니다.

- `ImageCompressor.java`
- `DateUtils.java`
- `TokenManager.java`
- `ReminderScheduler.java`

---

## 9. EC2 PHP 서버 구조

```text
server
├── config
│   ├── database.php
│   ├── env.php
│   └── cors.php
├── middleware
│   ├── auth.php
│   ├── room_permission.php
│   └── rate_limit.php
└── api
    ├── auth
    │   ├── kakao_login.php
    │   └── logout.php
    ├── trips
    │   ├── list.php
    │   ├── create.php
    │   ├── detail.php
    │   ├── join.php
    │   ├── members.php
    │   └── archive.php
    ├── itinerary
    │   ├── upload.php
    │   ├── analyze.php
    │   └── confirm.php
    ├── weather
    │   └── forecast.php
    ├── checklist
    │   ├── generate.php
    │   ├── list.php
    │   ├── create.php
    │   ├── update.php
    │   ├── delete.php
    │   ├── assign.php
    │   └── check.php
    └── notifications
        └── FCM 연동 예정
```

함수 내부 코드는 작성하지 않고 PHP 파일과 책임만 정의합니다.

### config

- `database.php` - MySQL 연결 설정
- `env.php` - OpenAI Key 등 환경설정 로딩
- `cors.php` - 허용 Origin과 Header 설정

### middleware

- `auth.php` - 앱 로그인 토큰 확인
- `room_permission.php` - 여행방 참여 권한 확인
- `rate_limit.php` - AI 요청 횟수 제한

### api/auth

- `kakao_login.php` - 카카오 토큰 확인 및 앱 로그인 토큰 발급
- `logout.php` - 로그인 토큰 종료

### api/trips

- `list.php` - 참여 중인 여행방 목록
- `create.php` - 여행방 생성
- `detail.php` - 여행방 상세
- `join.php` - 초대 코드 참여
- `members.php` - 참여자 목록
- `archive.php` - 여행방 아카이브

### api/itinerary

- `upload.php` - 여러 장의 일정·숙소 이미지 수신
- `analyze.php` - OpenAI 이미지 분석 요청
- `confirm.php` - 사용자가 수정·확정한 일정 저장

### api/weather

- `forecast.php` - 장소 좌표와 일정별 날씨 조회

### api/checklist

- `generate.php` - 일정·날씨 기반 준비물 생성
- `list.php` - 준비물 목록 조회
- `create.php` - 준비물 직접 추가
- `update.php` - 준비물 수정
- `delete.php` - 준비물 삭제
- `assign.php` - 담당자 지정·취소
- `check.php` - 준비 상태 v/x 변경

### api/notifications

- FCM 연동 시 추가 예정

---

## 10. MySQL 데이터 구조

### users

- 사용자 기본 정보
- Kakao 사용자 식별자
- 닉네임과 프로필 이미지
- 생성일과 수정일

### auth_tokens

- 앱 로그인 토큰
- 사용자 식별자
- 만료일

### trips

- 여행방 이름
- 방장 사용자 식별자
- 여행 시작일과 종료일
- 여행 국가와 대표 도시
- 초대 코드
- 아카이브 상태

### trip_members

- 여행방 식별자
- 사용자 식별자
- 방장 또는 참여자 역할
- 참여일

### itineraries

- 여행방 식별자
- 날짜
- 도시와 국가
- 숙소 이름
- 체크인·체크아웃
- AI 신뢰도
- 사용자 확정 여부

### ai_analyses

- 여행방 식별자
- 분석 상태
- 사용 모델
- 분석 결과 JSON
- 프롬프트 버전
- 생성일

원본 이미지 데이터는 MySQL에 저장하지 않습니다.

### packing_items

- 여행방 식별자
- 준비물 이름
- 카테고리
- 중요도
- 담당자 식별자
- 준비 상태
- AI 추천 또는 사용자 추가 출처

### notification_tokens

- FCM 연결 시 추가 예정
- 사용자별 기기 token 저장

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
