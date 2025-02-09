# [Detalk](https://detalk.net)

## 목차
- [규칙](#규칙)
- [시작하기](#시작하기)
- [기술](#기술)
    - [BackEnd](#backend)
    - [DataBase](#database)
    - [Infra](#infra)
- [아키텍처](#아키텍처)
- [ERD](#erd)

## 규칙
- **선 구현 후 추상화 & 리팩토링** : 기능을 먼저 구현하고, 이후 추상화하여 리팩토링하는 방식으로 개발합니다.

## 시작하기
0. 요구 사항

- **Java 21** 이상이 설치되어 있어야 합니다.
- PostgreSQL 데이터베이스 연결 정보가 필요합니다. (Docker나 로컬 설치 등 원하는 방식으로 PostgreSQL을 준비하고, application-*.yaml에 관련 설정을 기입하세요.)
1. git clone

```bash
git clone https://github.com/chanwukim/detalk-api.git
```

2. add `application-*.yaml`

`application-example.yaml`을 참고하여 `application-*.yaml`을 작성하세요.

3. execute `jooqCodegen`

`jooqCodegen`을 실행하여 JOOQ 코드를 생성하세요.

```bash
 ./gradlew jooqCodegen
```

4. deployment `jar`

```bash
// build jar
./gradlew clean build -x test

// jar deployment
java -jar build/libs/api-0.0.1-SNAPSHOT.jar

// health check
curl -i http://localhost:8080/api/health
```
---
## 기술

### BackEnd
![Java 21](https://img.shields.io/badge/Java-21-007396?logo=OpenJDK&logoColor=white)
![Spring Boot 3.4](https://img.shields.io/badge/Spring%20Boot-3.4-6DB33F?logo=Spring%20Boot&logoColor=white)
![Spring Security 6.4](https://img.shields.io/badge/Spring%20Security-6.4-6DB33F?logo=Spring%20Security&logoColor=white)
![JOOQ 3.4](https://img.shields.io/badge/JOOQ-3.4-DB4437?logo=Gradle&logoColor=white)

### DataBase
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14-336791?logo=PostgreSQL&logoColor=white)

### Infra
![AWS LightSail](https://img.shields.io/badge/AWS%20LightSail-FF9900?logo=Amazon%20AWS&logoColor=white)
![AWS S3](https://img.shields.io/badge/AWS%20S3-569A31?logo=Amazon%20S3&logoColor=white)
![AWS RDS](https://img.shields.io/badge/AWS%20RDS-527FFF?logo=Amazon%20RDS&logoColor=white)
![Nginx](https://img.shields.io/badge/Nginx-009639?logo=nginx&logoColor=white)
![Cloudflare](https://img.shields.io/badge/Cloudflare-F38020?logo=Cloudflare&logoColor=white)

## 아키텍처
![ar.png](img/ar.png)



## ERD
- Product-Post
  ![product_post_dia.png](img/product_post_dia.png)
- Member
  ![member_dia.png](img/member_dia.png)