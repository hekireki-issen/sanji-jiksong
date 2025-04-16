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

### 요구사항 정의서

![image](https://github.com/user-attachments/assets/9286c14a-0796-448a-9274-f77c1ec37507)

https://docs.google.com/spreadsheets/d/1H1e962uaFwESFsv0syk0m6vaHKlMy7VjK5d2sXw-Bag/edit?usp=sharing

### ERD

![image](https://github.com/user-attachments/assets/01f83e0f-f734-4434-8252-7b012bf5fc8a)


## 🔧 주요기능
### Auth
| 기능          | HTTP 메소드 | URL                      |
|---------------|-------------|--------------------------|
| 로그인           | ```POST``` | ```/login```    |
| 엑세스 토큰 재발급 | ```POST``` | ```/refresh``` |
| 로그아웃         | ```GET``` | ```/logout```    |

### User
| 기능          | HTTP 메소드 | URL                      |
|---------------|-------------|--------------------------|
| 회원가입                 | ```POST``` | ```/api/v1/register```                |
| 회원탈퇴                 | ```PATCH``` | ```/api/v1/users/{usersId}```        |
| 비밀번호 재설정           | ```POST``` | ```/api/v1/users/{userId}/password``` |
| 회원 복구                 | ```POST``` | ```/api/v1/users/{userId}/restore``` |
| 프로필  조회(유저 정보 조회) | ```GET``` | ```/api/v1/me```                    |

### Admin
| 기능          | HTTP 메소드 | URL                      |
|---------------|-------------|--------------------------|
| 모든 유저 조회 | ```GET``` | ```/api/v1/admin/users```                   |
| 유저 탈퇴 처리 | ```PATCH``` | ```/api/v1/admin/users/{id}/deactivate``` |

### Search
| 기능          | HTTP 메소드 | URL                      |
|---------------|-------------|--------------------------|
| 상품 검색   | ```GET``` | ```/api/v1/items/search```        |
| 가게 검색   | ```GET``` | ```/api/v1/stores/search```       |
| 카테고리 검색 | ```GET``` | ```/api/v1/categories/search``` |

### Order
| 기능          | HTTP 메소드 | URL                      |
|---------------|-------------|--------------------------|
| 상품 주문 요청       | ```POST```  | ```/api/v1/orders```                          |
| 사용자 주문 목록 조회 | ```GET```   | ```/api/v1/orders```                         |
| 주문 상세 조회       | ```GET```   | ```/api/v1/orders/{orderId}```                |
| 주문 취소            | ```PATCH``` | ```/api/v1/orders/{orderId}/cancel```         |
| 주문 수정            | ```PATCH``` | ```/api/v1/orders/{orderId}/items/{itemId}``` |

### Store
| 기능          | HTTP 메소드 | URL                      |
|---------------|-------------|--------------------------|
| 가게등록 | ```POST```  | ```/api/v1/stores```                      |
| 가게조회 | ```GET```   | ```/api/v1/stores/{storeId}```            |
| 가게수정 | ```PATCH```   | ```/api/v1/stores/{storeId}```          |
| 가게삭제 | ```PATCH``` | ```/api/v1/stores/{storeId}/deactivate``` |

### Item
| 기능          | HTTP 메소드 | URL                      |
|---------------|-------------|--------------------------|
| 판매 상품 등록      | ```POST``` | ```/api/v1/stores/items```                      |
| 판매 상품 목록 조회 | ```GET```   | ```//api/v1/stores/items```                    |
| 특정 판매 상품 조회 | ```GET```   | ```/api/v1/stores/{storeId}/items/{itemId}```  |
| 판매 상품 수정      | ```PATCH``` | ```/api/v1/stores/items/{itemId}```            |
| 판매 상품 삭제      | ```PATCH``` | ```/api/v1/stores/items/{itemId}/deactivate``` |
| 판매 통계           | ```GET```  | ```/api/v1/stores/statistics```                |
| 판매 top5          | ```GET```   | ```/api/v1/stores/best-products```             |
| 주간 판매 추이      | ```GET```   | ```/api/v1/stores/weekly-sales```              |
| 시간별 판매 추이    | ```GET```   | ```/api/v1/stores/hourly-sales```              |

### Cart
| 기능          | HTTP 메소드 | URL                      |
|---------------|-------------|--------------------------|
| 장바구니 상품 등록 | ```POST```  | ```/api/v1/carts/{productId}```  |
| 장바구니 상품 조회 | ```GET```   | ```/api/v1/carts/{productId}```  |
| 장바구니 상품 수정 | ```PATCH```  | ```/api/v1/carts/{productId}``` |
| 장바구니 상품 삭제 | ```DELETE``` | ```/api/v1/carts/{productId}``` |

### OpenApi
| 기능          | HTTP 메소드 | URL                      |
|---------------|-------------|--------------------------|
| kamis 가격 정보 저장                | ```GET``` | ```/api/v1/openapi/kamis/prices```    |
| 특정 날짜 범위에 대한 가격 정보 저장 | ```GET``` | ```/api/v1/openapi/kamis/allprices``` |
| 상품 가격 정보 조회                 | ```GET``` | ```/api/v1/openapi/getPrices```       |
| 인기 검색어 크롤링                  | ```GET``` | ```/api/v1/openapi/naver/crawling```  |
| 인기 검색어 가격 조회               | ```GET``` | ```/api/v1/openapi/naver/trending```  |

