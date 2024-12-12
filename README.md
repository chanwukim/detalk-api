# detalk-api
## 규칙
- **선 구현 후 추상화 & 리팩토링** : 기능을 먼저 구현하고, 이후 추상화하여 리팩토링하는 방식으로 개발합니다.

## 개발하는 법
1. 프로젝트 clone

```bash
git clone https://github.com/chanwukim/detalk-api.git
```

2. `application-*.yaml` 작성

`application-example.yaml`을 참고하여 `application-*.yaml`을 작성하세요.

3. `jooqCodegen` 실행

`jooqCodegen`을 실행하여 JOOQ 코드를 생성하세요.

```bash
 ./gradlew jooqCodegen
```
