# 🥗 산지직송

농산물 유통정보 데이터를 활용한 농수산 거래 플랫폼입니다.  

농산물 유통정보(KAMIS)의 오픈 API를 활용하여 실제 데이터 기반의 신뢰성 높은 정보 전달합니다.

---

## 👥 팀원

| 팀장 | 팀원 | 팀원 | 팀원 | 팀원 |
|:---:|:---:|:---:|:---:|:---:|
| [**전준영**](https://github.com/Isonade2) | [**김지상**](https://github.com/jisnag) | [**안형준**](https://github.com/ahn-h-j) | [**우상진**](https://github.com/SangJin521) | [**최혜원**](https://github.com/choihywon) |

---

## ✏️ 기술 스택

### Tech
<img src="https://img.shields.io/badge/java-FC4C02?style=for-the-badge&logo=java&logoColor=white"><img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=SpringBoot&logoColor=white"><img src="https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=SpringSecurity&logoColor=white"><img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=MySQL&logoColor=white"><img src="https://img.shields.io/badge/Amazon_S3-569A31?style=for-the-badge&logo=AmazonS3&logoColor=white"><img src="https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=JUnit5&logoColor=white">

### Dev
<img src="https://img.shields.io/badge/DOCKER-2496ED?style=for-the-badge&logo=docker&logoColor=white"><img src="https://img.shields.io/badge/Github_Actions-2088FF?style=for-the-badge&logo=GithubActions&logoColor=white">

### Tool
<img src="https://img.shields.io/badge/DISCORD-5865F2?style=for-the-badge&logo=discord&logoColor=white"><img src="https://img.shields.io/badge/NOTION-FFFFFF?style=for-the-badge&logo=notion&logoColor=black"><img src="https://img.shields.io/badge/Github-000000?style=for-the-badge&logo=Github&logoColor=white"/><img src="https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=Postman&logoColor=white"/>

---

## 📜 프로젝트 목표
- RESTful API로 설계된 프로젝트 구축
- 클린 코드, 표준 API 문서화, 버전 관리 체계 마련
- 데이터 모델링 전략 수립하여 수집한 데이터로 유용한 통계와 순위를 제공
- API 성능 최적화하여 사용자경험 향상
- 데일리 스크럼, git 등 협업 도구 및 프로세스 도입
- 기술 명세서를 작성하여 고객 요구 사항을 기술적으로 분석하고 구현
- 인증, 권한 관리, 데이터 암호화 등 보안 기술을 적용
- CI/CD 도구 활용한 테스트 자동화, 클라우드 배포 파이프라인 구축



## 🖥️ 프로젝트 산출물

### 프로젝트 아키텍처

![image](https://github.com/user-attachments/assets/8648102f-b437-44b1-ab78-cc4497298e39)

### 유스케이스

![image](https://github.com/user-attachments/assets/0499c1f2-6fcc-4f91-8121-b1d0d6d5c2bd)

### ERD

![image](https://github.com/user-attachments/assets/01f83e0f-f734-4434-8252-7b012bf5fc8a)

## 🔧 주요기능

<details>
<summary>User</summary>
<div markdown="1">
  
BUYER, SELLER  
- 회원가입, 탈퇴, 복구, 비밀번호 재설정

ADMIN  
- 모든 유저 조회, 탈퇴 처리

</div>
</details>

<details>
<summary>Store</summary>
<div markdown="1">

- 가게 도메인 
- 가게 등록, 조회, 수정, 삭제
- S3 이미지 업로드

</div>
</details>

<details>
<summary>Item</summary>
<div markdown="1">

- 상품 등록, 조회, 수정, 삭제
- 상품명, 카테고리 검색
- 품목 매출 현황, Top5 매출, 주간 매출 추이, 시간별 매출 추이 그래프를 통한 판매통계 제공

</div>
</details>
<details>
<summary>Cart</summary>
<div markdown="1">

- 장바구니 생성, 조회, 수정, 삭제

</div>
</details>
<details>
<summary>Order</summary>
<div markdown="1">

- 주문 생성, 조회, 수정, 삭제

</div>
</details>
<details>
<summary>OpenApi</summary>
<div markdown="1">

- 스프링 스케줄러를 통한 데이터 저장
- KAMIS OPENAPI 기반 실시간 유통 가격 정보 및 추이 제공
- 네이버 트랜드랩 인기검색어 크롤링을 활용한 인기품목 기반 가격 정보 제공

</div>
</details>
