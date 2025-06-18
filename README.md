# News-For-EveryOne
## 📌 프로젝트 소개


-  여러 뉴스 API를 통합하여 사용자에게 맞춤형 뉴스를 제공하고, 의견을 나눌 수 있는 소셜 기능을 갖춘 서비스입니다.

### 📌 프로젝트 정보

| 항목 | 내용             |
|------|----------------|
| **📆 프로젝트 기간** | 2025.05.28 ~ 2025.06.18 |
| **🔗 배포 링크** | [모두의 뉴스, 모뉴](http://sprint-project-1196140422.ap-northeast-2.elb.amazonaws.com/sb/monew/login) |
| **📋 협업 문서** | [Notion 페이지](https://nebula-shoulder-dc6.notion.site/200016a491728087ace2e0feb7e4a740?v=200016a49172807fb4be000c53305ab6) |
| **📘 API 문서** | [Swagger 문서](http://sprint-project-1196140422.ap-northeast-2.elb.amazonaws.com/sb/monew/api/swagger-ui/index.html) |

## 🏃‍♀️ 프로젝트 구성원과 R&R

| 이름        | 역할 및 기여                   |
|-----------|---------------------------|
| [황지환(팀장)] |  |
| [김보경](https://github.com/BokyungKim-SPRING)     | • 사용자 관리 도메인 설계 및 기능 구현<br> • Spring Security를 통한 세션 인증 구현<br> • Comment / CommentLike 도메인 설계 및 기능 구현<br>• 댓글 목록 커서 페이지네이션 구현<br> |
| [한상엽]     | 	• 뉴스 기사 수집 도메인 설계 및 기능 구현<br> • Spring Batch 기반 Naver Open API 및 RSS 기사 수집 구현 <br> • 뉴스 기사 백업 및 복구 기능 개발 (AWS S3 연동) |
| [강지훈](https://github.com/homeA90)     |  • 뉴스 기사 수집 도메인 설계 및 기능 구현<br> • Spring Batch 기반 Naver Open API 및 RSS 기사 수집 구현 <br> • 뉴스 기사 백업 및 복구 기능 개발 (AWS S3 연동) <br> • CI/CD 파이프라인 구축 |
| [윤영로]     |  • 뉴스 기사 수집 도메인 설계 및 기능 구현<br> • Spring Batch 기반 Naver Open API 및 RSS 기사 수집 구현 <br> • 뉴스 사 백업 및 복구 기능 개발 (AWS S3 연동) |

## 🏫 주요 기능

### 사용자 관리
- 회원가입, 로그인, 닉네임 수정 기능
- 논리적 삭제 지원으로 데이터 무결성 유지
- Spring Security를 통한 인증 검사
- securityFilterChain을 통한 허용 경로 제한

### 관심사 관리

### 뉴스 기사 관리
- Naver: Open API, 한국경제, 조선일보, 연합뉴스: RSS Feed 기반 수집
- Spring Batch를 이용하여 Chunk 단위 처리로 대용량 데이터 효율적으로 수집
- 정렬 및 커서 페이지네이션을 이용한 검색 및 조회 기능
- 뉴스 백업 및 복구 기능
  
### 댓글 관리
- 기사별 댓글 등록, 수정, 삭제
- 댓글 좋아요 등록 및 취소
- 최신순 정렬 커서 기반 페이지네이션
- 댓글 작성/수정/삭제 시 사용자 활동 내역과 연동

### 활동 내역 관리

### 알림 관리

### 🫙 ERD
<img src="readmeImageFile/erd.png" alt="img_1" width="600"/>

## 🛠️ 기술 스택

### 백엔드
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Web](https://img.shields.io/badge/Spring_Web-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring Batch](https://img.shields.io/badge/Spring_Batch-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)

### 데이터베이스
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)

### 클라우드/인프라
![AWS S3](https://img.shields.io/badge/AWS_S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white)
![AWS ECS](https://img.shields.io/badge/AWS_ECS-FF9900?style=for-the-badge&logo=amazonecs&logoColor=white)
![AWS ECR](https://img.shields.io/badge/AWS_ECR-232F3E?style=for-the-badge&logo=amazonaws&logoColor=white)
![AWS RDS](https://img.shields.io/badge/AWS_RDS-D22128?style=for-the-badge&logo=amazonaws&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

### 테스트
![JUnit Jupiter](https://img.shields.io/badge/JUnit_Jupiter-25A162?style=for-the-badge&logo=junit5&logoColor=white)
![TestContainers](https://img.shields.io/badge/TestContainers-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![H2](https://img.shields.io/badge/H2_Database-0074BD?style=for-the-badge&logo=h2&logoColor=white)

### 협업
![Jira](https://img.shields.io/badge/Jira-326CE5?style=for-the-badge&logo=jira&logoColor=white)
![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white)
![Discord](https://img.shields.io/badge/Discord-5865F2?style=for-the-badge&logo=discord&logoColor=white)

### 부하테스트

