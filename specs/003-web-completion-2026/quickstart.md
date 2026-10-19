# 빠른 검증: 2026.1 웹 자동완성

## 자동 검증

```powershell
.\gradlew.bat test buildPlugin verifyPluginProjectConfiguration
.\gradlew.bat test -PtestWebLanguages=true
```

설치된 SDK를 사용하려면 `-PlocalIdePath=D:/Common/idea-2026.1.3.win`을 추가한다. 기본 대상은 2026.1.3이다.

## 수동 검증

1. build/distributions의 ZIP을 2026.1 IDE의 디스크에서 플러그인 설치로 적용한다.
2. src/test/testData/completion.jsp를 열어 안내된 위치에서 Ctrl+Space를 누른다.
3. HTML 태그·속성, script 지역 변수, style CSS 속성 후보를 선택한다.
4. JSP가 앞뒤에 있는 경우 완성 위치와 주변 원문을 확인한다.
5. 미완성 JSP 입력과 기본 강조, 저장·재열기를 확인한다.

## 결과

구현 후 실제 테스트와 빌드 결과 및 수동 검증 수행 여부를 기록한다.

## 2026-10-19 검증 결과

- 대상 SDK: 설치된 IntelliJ IDEA 2026.1.3, 빌드 261.25134.95, 해당 IDE의 JBR 25.
- 웹 구성에서 총 21개 테스트 통과: 렉서 7개, 기존 에디터 강조 7개, PSI·자동완성 7개.
- HTML 태그·속성, JS 지역 변수, CSS 속성 후보와 선택 후 실제 원본 문서 삽입을 확인했다.
- JSP 앞뒤 문맥, 원문·위치 보존, 문서 복사, 구분자 편집, 빈 문서·미완성 구문과 JSP 내부 요청을 검증했다.
- 기존 강조·테마 변경과 웹 강조 제공자 부재 상황도 회귀 검증했다.
- buildPlugin 및 verifyPluginProjectConfiguration 성공. 최소 지원 빌드는 261이다.
- 실제 IDE 창에서의 수동 설치·조작·시각 확인은 수행하지 않았다.

## 검증 환경의 제한과 재현

현재 실행 환경은 외부 다운로드가 제한되어 있고 설치된 2026.1 배포본에는 testFramework.jar가 없다. 따라서 253 테스트 프레임워크의 로컬 복사본에서 바뀐 로그·스레드·데몬 준비·문서 캐시 정리 API 호출을 261 API에 연결한 임시 호환 도구를 사용했다. 검증 대상 제품 클래스와 실제 HTML·JS·CSS 기능은 모두 261 SDK이다. 테스트의 검증 조건은 변경하지 않았다.

이 결과는 정식 261 테스트 프레임워크로 실행한 결과와 구분한다. 일반 환경의 기본 빌드는 대상 버전의 공식 Platform 테스트 의존성을 사용한다. 네트워크 사용이 가능한 환경에서는 위 기본 명령으로 정식 프레임워크 검증을 다시 수행할 수 있다.

임시 도구는 `.gradle-user-home/test-harness/`에 있으며 배포 ZIP에는 포함하지 않는다. 기존 의존성은 읽기 전용 Gradle 캐시와 임시 초기화 스크립트가 추가한 로컬 Maven 캐시에서 읽었다. Kotlin 코드와 수동 작성 UI만 있으므로 이 오프라인 빌드에서는 추가 다운로드가 필요한 바이트코드 계측을 `-PinstrumentCode=false`로 끄고, 재컴파일 확인을 위해 `-Pkotlin.incremental=false`를 사용했다. 기본 빌드의 계측 설정은 유지한다.

```powershell
$env:GRADLE_USER_HOME = "$PWD/.gradle-user-home"
$env:GRADLE_RO_DEP_CACHE = 'C:/Users/tcpip147/.gradle/caches'
.\.gradle-user-home/gradle-9.7.1/bin/gradle.bat test buildPlugin verifyPluginProjectConfiguration -PlocalIdePath=D:/Common/idea-2026.1.3.win '-PlocalTestFrameworkPath=D:/Project/intellij-jsp/.gradle-user-home/test-harness/test-framework-compat.jar' -PinstrumentCode=false -PtestWebLanguages=true '-Pkotlin.incremental=false' --offline --no-daemon --no-configuration-cache -I .gradle-user-home/local-test-cache.init.gradle.kts '-Porg.gradle.java.installations.paths=D:/Common/idea-2026.1.3.win/jbr,D:/Project/intellij-jsp/.gradle-user-home/caches/9.7.1/transforms/41d2ee0b381ac1ce59d15aa3eb4932b1/transformed/idea-2025.3.6.1-win/jbr'
```

설치 파일: `build/distributions/intellij-jsp-1.0.0-SNAPSHOT.zip`.

기본 구성 테스트도 성공했다. 이 구성은 웹 테스트 의존성을 명시하지 않고 HTML·PSI 및 기존 강조를 검증한다. JS·CSS 완성 전용 2개 메서드는 조건에 따라 본문을 실행하지 않으므로 이 구성의 통과 수를 웹 완성 검증 횟수로 중복 계산하지 않는다. 웹 완성은 앞의 웹 구성에서 실제 후보와 삽입을 모두 검증했다.

패키지 검사에서 최소 빌드 261, JSP parser·view provider 클래스 포함과 테스트 호환 도구 미포함을 확인했다.
