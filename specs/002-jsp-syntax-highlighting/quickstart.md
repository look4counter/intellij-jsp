# 빠른 검증: JSP 구문 강조

## 자동 검증

JDK 21과 기존 Gradle 환경을 사용한다.

```powershell
.\gradlew.bat test buildPlugin verifyPluginProjectConfiguration
.\gradlew.bat test -PtestWebLanguages=true
```

기본 구성은 JS·CSS 테스트 의존성 없이 IDE에 있는 HTML·웹 강조 기능을 확인한다. 미설치 상황은 임베디드 언어 제공자 확장점을 일시적으로 비운 별도 테스트로 재현한다. 웹 구성은 실제 JS·CSS 지원을 활성화해 독립 HTML 강조와 JSP 레이어 결과를 비교한다.

## 수동 검증

1. 생성된 build/distributions의 ZIP을 디스크에서 플러그인 설치로 적용한다.
2. src/test/testData/highlighting.jsp를 연다.
3. HTML 태그·속성, JSP 지시자·주석·표현식·EL의 강조를 확인한다.
4. JS·CSS 기능이 있는 IDE에서는 script·style 내용이 각각 해당 색상 설정을 따르는지 확인한다.
5. HTML 속성, JS 문자열, CSS 값 사이에 JSP를 삽입·삭제해 앞뒤 강조가 유지되는지 확인한다.
6. JSP 종료 구분자를 지우고 다시 입력해 현재 구문에 맞게 강조가 갱신되는지 확인한다.
7. 밝은 테마와 어두운 테마를 전환한 뒤 저장·재열기로 원문 보존을 확인한다.

## 실행 결과

구현 후 자동 검증 결과와 수동 확인 수행 여부를 기록한다.



## 2026-10-19 검증 기록

- 웹 구성 `test -PtestWebLanguages=true`: 렉서 7개와 에디터 통합 7개, 총 14개 테스트 통과.
- 실제 HTML 강조기와 토큰 종류·색상을 비교했다. script의 JavaScript 키워드와 style의 CSS 속성 토큰도 확인했다.
- 미설치 상황은 HTML 임베디드 확장, script 제공자와 임베디드 토큰 제공자를 일시적으로 제거해 재현했다. HTML·JSP 강조와 문서 전체 처리 유지 및 제공자 복원 후 JS·CSS 강조를 확인했다.
- HTML 속성·JS 문자열·CSS 값 사이의 JSP, EL 중첩·이스케이프, 빈 파일·미완성 구문, 구분자 편집과 테마 변경을 자동 검증했다.
- 실제 IDE 창에서의 시각 확인·설치 후 수동 조작은 수행하지 않았다. 위 수동 검증 절차와 예제 파일을 제공한다.

### 제한 환경의 실행 방법

이 환경에서는 Gradle 배포본과 쓰기 캐시를 작업 폴더에 두고 기존 Gradle 의존성 캐시를 읽기 전용으로 사용했다. JDK는 대상 IDE에 포함된 JBR 21을 지정했다. opentest4j는 기존 로컬 Maven 캐시를 추가하는 임시 초기화 스크립트로 읽었다. 저장소의 기본 빌드 설정에 개인 캐시 경로를 넣지 않았다.

실행한 명령은 다음과 같다. 일반 환경에서는 위 Gradle 래퍼 명령을 사용하면 된다.

```powershell
$env:GRADLE_USER_HOME = "$PWD/.gradle-user-home"
$env:GRADLE_RO_DEP_CACHE = 'C:/Users/tcpip147/.gradle/caches'
.\.gradle-user-home/gradle-9.7.1/bin/gradle.bat test buildPlugin verifyPluginProjectConfiguration --offline --no-daemon --no-configuration-cache -I .gradle-user-home/local-test-cache.init.gradle.kts '-Porg.gradle.java.installations.paths=D:/Project/intellij-jsp/.gradle-user-home/caches/9.7.1/transforms/41d2ee0b381ac1ce59d15aa3eb4932b1/transformed/idea-2025.3.6.1-win/jbr'
```

웹 구성은 같은 실행 환경에서 `test -PtestWebLanguages=true`로 검증했다.
설치 파일은 `build/distributions/intellij-jsp-1.0.0-SNAPSHOT.zip`이다.

최종 기본 구성 `test buildPlugin verifyPluginProjectConfiguration`도 총 14개 테스트 통과와 함께 성공했다. 플러그인 ZIP 생성과 구성 검사 모두 통과했다.
