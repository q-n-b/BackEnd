# 📘 QnB (Question & Book)

> **도서 기반 질문·답변(Q&A) 서비스 – Backend 서버**  
> 책을 중심으로 질문을 생성하고, 답변을 통해 지식을 축적하는 플랫폼

---

## 🔍 프로젝트 소개

**QnB**는 도서를 매개로 한 질문·답변 서비스입니다.  
사용자는 책을 스크랩하고, 책과 연관된 질문을 확인하며, 질문에 대한 답변을 작성할 수 있습니다.

본 저장소는 **QnB 서비스의 백엔드 서버**로,  
Spring Boot 기반 REST API와 JWT 인증을 중심으로 구성되어 있습니다.

---

## 👤 담당 역할

- **Backend Developer**
- API 설계 및 구현
- JWT 기반 인증/인가 구현
- 도메인별 패키지 구조 설계
- RDS(MySQL) 연동 및 JPA 기반 데이터 관리
- EC2 서버 배포 및 운영

---

## 🛠 기술 스택

### Backend
- Java 17
- Spring Boot
  - Spring Web
  - Spring Security
  - Spring Data JPA
- JWT (Access Token 기반 인증)

### Database
- MySQL (AWS RDS)

### Infra
- AWS EC2
- AWS RDS
- DuckDNS

### Build Tool
- Gradle

---

## 🌐 서비스 도메인

- **Backend API Domain**  
  👉 http://qnb.duckdns.org/

---

## 📁 프로젝트 패키지 구조

```text
src/main/java/qnb
├── answer                  # 답변 도메인
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   └── service
│
├── book                    # 도서 도메인
│
├── common                  # 공통 모듈
│   ├── JWT                 # JWT 관련 로직
│   ├── config              # 전역 설정 (Security, Web, etc.)
│   ├── dto                 # 공통 DTO
│   ├── exception           # 공통 예외 정의
│   ├── handler             # Global Exception Handler
│   ├── util                # 유틸 클래스
│   └── web                 # 공통 Web 설정
│
├── like                    # 좋아요 기능
├── question                # 질문 도메인
├── recommend               # 추천 기능 (ML 연동)
├── scrap                   # 스크랩 기능
├── search                  # 검색 기능
├── user                    # 사용자 도메인
│
└── AuthApplication.java    # Spring Boot 메인 클래스
```
## 🔐 인증 방식 (JWT)

- 로그인 성공 시 **Access Token** 발급
- 인증이 필요한 모든 API 요청은 아래 헤더 필수
```Authorization: Bearer {ACCESS_TOKEN}```

- Spring Security Filter 기반 인증 처리
- Stateless 구조로 서버 확장성 고려

---

## 📌 주요 기능

### 👤 사용자
- 회원가입 / 로그인
- 사용자 정보 조회
- JWT 기반 인증·인가

### 📚 도서
- 도서 정보 조회
- 도서 스크랩 (상태 관리)

### ❓ 질문
- 도서 기반 질문 조회
- 질문 상세 조회
- 질문 스크랩

### ✍️ 답변
- 질문에 대한 답변 작성
- 답변 목록 조회

### ❤️ 공통 기능
- 좋아요
- 스크랩
- 검색
- 추천 (ML 서버 연동)

---

## ⚠️ 예외 처리

- 공통 ErrorCode 기반 예외 처리
- `GlobalExceptionHandler`를 통한 일관된 에러 응답
- HTTP Status Code 표준에 맞춘 응답 설계

---
## 🌟 차별점

- **질문 자동 생성 (ML 연동)**  
  도서 메타데이터(제목, 설명, 키워드)를 기반으로 ML 서버에서 질문을 자동 생성하고,  
  백엔드에서 이를 저장·관리하여 프론트엔드에 제공  
  → 사용자는 *질문을 떠올리는 부담 없이* 바로 사고 확장이 가능

- **개인 맞춤 도서 추천 (임베딩 기반)**  
  단순 장르/카테고리 추천이 아닌,  
  사용자 활동 이력과 도서 정보를 벡터 임베딩하여 **유사도 기반 추천** 제공  
  → 백엔드에서 추천 요청을 중개하고, ML 결과를 서비스 데이터 구조에 맞게 가공

- **히스토리 기반 주간 추천 시스템**  
  사용자의 독서·질문·답변 이력을 주 단위로 분석하여  
  매주 한 권의 대표 추천 도서를 자동 확정  
  → 개인화된 독서 히스토리와 추천 흐름을 지속적으로 제공

- **ML 서버와의 실전 연동 경험**  
  - 백엔드 ↔ ML 서버 간 HTTP 기반 통신 설계  
  - 추천/질문 생성 요청·응답 DTO 정의 및 데이터 검증  
  - ML 결과를 DB에 저장하여 **중복 생성 방지 및 성능 최적화**  
  - 서비스 흐름을 고려한 *비동기적 사고와 책임 분리 구조* 설계

- **End-to-End 직접 구축 경험**  
  백엔드(Spring Boot), 프론트엔드(React), ML 서버, 인프라(AWS EC2·RDS)를  
  하나의 서비스 흐름으로 직접 설계·구현·배포  
  → 단순 기능 구현이 아닌 **실제 운영을 고려한 구조 설계 경험**
---

## 🧪 테스트

```src/test/java/qnb```

- 도메인 단위 테스트
- API 테스트 확장 예정

---

## 🚀 실행 방법

### 1️⃣ 프로젝트 빌드

```./gradlew clean build ```

### 2️⃣ 서버 실행

```java -jar build/libs/qnb-*.jar```
또는
```./gradlew bootRun```

---

## ✨ Author

- **Backend Developer**
- Spring Boot · JPA · Spring Security · JWT
- AWS EC2 / RDS 기반 서비스 운영
