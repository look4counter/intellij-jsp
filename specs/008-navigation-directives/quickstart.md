# 검증: JSP 이동과 지시자 완성

## 자동 검증

기존 [include 검증 명령](../007-include-context/quickstart.md)의 SDK 및 로컬 테스트 도구 인자로 test buildPlugin verifyPluginProjectConfiguration을 실행한다.

## 수동 검증

1. 정적 include 경로를 Ctrl+클릭하여 JSP 또는 JSPF 파일을 연다.
2. test 함수 호출을 Ctrl+클릭하거나 Ctrl+B로 실제 선언 이름에 이동한다.
3. 중첩 include와 오버로드, 선언 편집 뒤 이동 위치를 확인한다.
4. <%@ 뒤 이름, page 뒤 속성, session과 pageEncoding 값의 완성과 삽입을 확인한다.
5. 미완성 구문·주석·없는 파일에서도 주변 문서가 보존되는지 확인한다.

## 결과

2026-10-20, IntelliJ IDEA 2026.1.3 SDK로 검증했다.

- 전체 테스트 86개 통과, 실패·오류 0개. 기존 67개와 선언 이동 8개, 지시자 완성 11개를 실행했다.
- 실제 GotoDeclarationAction 경로로 include 상대·상위·웹 루트 파일 이동과 없는·미완성 경로를 확인했다.
- 포함된 test 함수와 같은 파일의 함수에서 실제 원본 파일과 정확한 함수 이름 오프셋을 확인했다.
- 중첩 include의 오버로드 선택, 선언 앞 한글 주석 삽입 뒤 갱신, 모호한 부모의 잘못된 이동 방지 및 일반 Java 메서드 탐색을 확인했다.
- 세 지시자 이름, page·include·taglib 속성, boolean·인코딩 값, 미완성 이름과 등호·따옴표 삽입을 확인했다.
- 기존 등호·작은따옴표 보존, 값 따옴표 삽입, 중복 속성 제외와 반복 가능한 import, taglib의 uri·tagdir 상호 배제 및 지시자 밖의 음성 검증을 확인했다.
- 기존 include·Java·HTML·JS·CSS의 완성·강조·포매팅 회귀 테스트가 통과했다.
- buildPlugin 및 verifyPluginProjectConfiguration 성공. ZIP 내 탐색 핸들러·완성 기여자 등록과 원본 매핑 클래스 포함, 테스트 클래스 미포함을 확인했다.

## 재현 명령

```powershell
$env:GRADLE_USER_HOME = "$PWD/.gradle-user-home"
$env:GRADLE_RO_DEP_CACHE = 'C:/Users/tcpip147/.gradle/caches'
.\.gradle-user-home/gradle-9.7.1/bin/gradle.bat test buildPlugin verifyPluginProjectConfiguration -PlocalIdePath=D:/Common/idea-2026.1.3.win '-PlocalTestFrameworkPath=D:/Project/intellij-jsp/.gradle-user-home/test-harness/test-framework-compat.jar' -PinstrumentCode=false -PtestWebLanguages=true '-Pkotlin.incremental=false' --offline --no-daemon --no-configuration-cache -I .gradle-user-home/local-test-cache.init.gradle.kts '-Porg.gradle.java.installations.paths=D:/Common/idea-2026.1.3.win/jbr,D:/Project/intellij-jsp/.gradle-user-home/caches/9.7.1/transforms/41d2ee0b381ac1ce59d15aa3eb4932b1/transformed/idea-2025.3.6.1-win/jbr'
```

## 환경 제한

외부 다운로드 제한으로 기존 로컬 호환 테스트 도구와 읽기 전용 의존성 캐시를 사용하고 검증 실행에서만 계측을 껐다. 제품 코드와 언어 엔진은 실제 261 SDK다. 이동은 플랫폼의 실제 대상 해석 API로 검증했으며 IDE 화면에서 마우스로 Ctrl+클릭하거나 단축키를 누르는 수동 검증은 수행하지 않았다. 정식 261 테스트 프레임워크에서의 재검증은 수행하지 않았다.

설치 파일: `build/distributions/intellij-jsp-1.0.0-SNAPSHOT.zip`. 실행 중인 개발용 IDE는 종료 후 runIde를 다시 실행해야 새 코드를 로드한다.
