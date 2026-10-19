# 빠른 검증: JSP 웹 포매팅

## 자동 검증

```powershell
.\gradlew.bat test buildPlugin verifyPluginProjectConfiguration
.\gradlew.bat test -PtestWebLanguages=true
```

설치 SDK 사용 시 -PlocalIdePath=D:/Common/idea-2026.1.3.win을 추가한다. 오프라인 호환 도구의 경로와 제한은 [이전 검증 안내](../003-web-completion-2026/quickstart.md)를 따른다.

## 수동 검증

1. 배포 ZIP을 IntelliJ IDEA 2026.1에 설치한다.
2. JSP에 중첩 HTML 및 script·style을 입력한다.
3. Ctrl+Alt+L을 누르고 HTML·JS·CSS 코드 스타일 반영을 확인한다.
4. JSP 서버 구문 내부 원문이 유지되는지 확인한다.
5. 선택 영역 재정렬, 반복 실행과 미완성 입력을 확인한다.

## 결과

2026-10-19, 설치된 IntelliJ IDEA 2026.1.3(261.25134.95)에서 검증했다.

- 웹 구성 전체 테스트 30개 통과: 렉서 7개, 강조 7개, 자동완성 7개, 포매팅 9개. 실패와 오류는 0개이다.
- 웹 테스트 의존성을 추가하지 않은 기본 구성도 성공했다. 기본 구성에서는 웹 완성 2개와 웹 포매팅 2개 메서드가 조건에 따라 본문을 실행하지 않으므로, 보고된 30개를 웹 기능 추가 검증으로 계산하지 않는다. 웹 기능은 앞의 웹 구성에서 실제 결과를 검증했다.
- HTML·JS·CSS의 실제 포매팅 결과를 기존 HTML 에디터와 비교하고 HTML 들여쓰기 설정과 선택 범위 보존을 확인했다.
- 지시자·주석·스크립틀릿·표현식·선언·두 종류 EL 내부 원문, 여러 줄 구문, 속성·웹 코드 안의 서버 구문을 검증했다.
- 빈 문서·미완성 구문, 공백 의미 영역과 반복 실행 결과를 검증했다.
- buildPlugin 및 verifyPluginProjectConfiguration 성공.
- ZIP에 최소 빌드 261과 CommunityJSP 포매터 등록·구현 클래스가 있고 테스트 호환 도구가 없음을 확인했다.
- 실제 IDE 화면에서의 수동 설치·단축키 조작은 수행하지 않았다.

## 환경 제한

외부 다운로드 제약으로 정식 261 테스트 프레임워크 대신 이전 기능에서 설명한 로컬 호환 테스트 도구를 사용했다. 제품 클래스와 실제 언어 포매터는 261 SDK이다. 정식 261 테스트 프레임워크에서의 재검증은 수행하지 않았다. 기본 빌드 설정은 공식 테스트 의존성과 계측을 유지하며, 로컬 검증에서만 계측을 끄고 읽기 전용 캐시를 사용했다.

## 재현 명령

```powershell
$env:GRADLE_USER_HOME = "$PWD/.gradle-user-home"
$env:GRADLE_RO_DEP_CACHE = 'C:/Users/tcpip147/.gradle/caches'
.\.gradle-user-home/gradle-9.7.1/bin/gradle.bat test buildPlugin verifyPluginProjectConfiguration -PlocalIdePath=D:/Common/idea-2026.1.3.win '-PlocalTestFrameworkPath=D:/Project/intellij-jsp/.gradle-user-home/test-harness/test-framework-compat.jar' -PinstrumentCode=false -PtestWebLanguages=true '-Pkotlin.incremental=false' --offline --no-daemon --no-configuration-cache -I .gradle-user-home/local-test-cache.init.gradle.kts '-Porg.gradle.java.installations.paths=D:/Common/idea-2026.1.3.win/jbr,D:/Project/intellij-jsp/.gradle-user-home/caches/9.7.1/transforms/41d2ee0b381ac1ce59d15aa3eb4932b1/transformed/idea-2025.3.6.1-win/jbr'
```

설치 파일: build/distributions/intellij-jsp-1.0.0-SNAPSHOT.zip.
