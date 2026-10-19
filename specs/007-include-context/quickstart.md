# 검증: include 문맥

## 자동 검증

기존 로컬 SDK 및 호환 테스트 도구 인자는 [Java 검증 명령](../005-java-editor/quickstart.md)을 따른다. test buildPlugin verifyPluginProjectConfiguration을 웹 언어 구성으로 실행한다.

## 수동 검증

1. main.jsp에서 common.jspf를 정적으로 포함하고 공통 변수와 메서드를 완성한다.
2. common.jspf를 열어 부모의 앞선 변수를 완성한다.
3. 두 번째 부모를 추가하고 조각의 자동 부모 연결이 해제되는지 확인한다.
4. 상대경로·웹 루트 경로·순환 및 파일 생성·삭제를 확인한다.
5. 현재 파일의 완성 선택과 재정렬이 다른 파일의 원문을 바꾸지 않는지 확인한다.

## 결과

2026-10-20, IntelliJ IDEA 2026.1.3 SDK로 검증했다.

- 전체 테스트 63개 통과, 실패·오류 0개. 기존 45개와 신규 include 통합 테스트 18개를 실행했다.
- 실제 완성과 삽입으로 포함된 변수·메서드, 중첩 상대경로 import 및 웹 루트 경로를 확인했다.
- include 삽입 순서의 제어문과 지역 변수 범위, 선언 이전 참조와 블록 밖 참조의 미해결 상태를 확인했다.
- 유일한 직접·중첩 부모 문맥과 다중 부모·반복 삽입 시 자체 문맥 처리를 확인했다.
- 포함 파일의 커밋된 미저장 편집, 생성·삭제, include 경로 변경과 두 번째 부모 추가·제거에 따른 캐시 갱신을 확인했다.
- JSP 주석과 동적 include 제외, 누락·미완성·순환 및 깊이 64 제한을 확인했다.
- 완성 선택과 양방향 포매팅에서 현재 파일 밖의 문서가 보존됨을 확인했다.
- buildPlugin 및 verifyPluginProjectConfiguration 성공. 배포 JAR에 구현 클래스와 jsp;jspf 확장자 등록이 있고 테스트 클래스가 없음을 확인했다.

## 재현 명령

```powershell
$env:GRADLE_USER_HOME = "$PWD/.gradle-user-home"
$env:GRADLE_RO_DEP_CACHE = 'C:/Users/tcpip147/.gradle/caches'
.\.gradle-user-home/gradle-9.7.1/bin/gradle.bat test buildPlugin verifyPluginProjectConfiguration -PlocalIdePath=D:/Common/idea-2026.1.3.win '-PlocalTestFrameworkPath=D:/Project/intellij-jsp/.gradle-user-home/test-harness/test-framework-compat.jar' -PinstrumentCode=false -PtestWebLanguages=true '-Pkotlin.incremental=false' --offline --no-daemon --no-configuration-cache -I .gradle-user-home/local-test-cache.init.gradle.kts '-Porg.gradle.java.installations.paths=D:/Common/idea-2026.1.3.win/jbr,D:/Project/intellij-jsp/.gradle-user-home/caches/9.7.1/transforms/41d2ee0b381ac1ce59d15aa3eb4932b1/transformed/idea-2025.3.6.1-win/jbr'
```

## 환경 제한

기존 검증과 마찬가지로 외부 다운로드 제한 때문에 로컬 호환 테스트 도구를 사용했다.
제품 코드와 언어 엔진은 실제 261 SDK이며 검증 실행에서만 계측을 껐다. 정식 261 테스트 프레임워크와 실제 IDE 화면에서의 수동 설치·단축키 검증은 수행하지 않았다.

설치 파일: `build/distributions/intellij-jsp-1.0.0-SNAPSHOT.zip`.

## 후속 test 메서드 완성 누락 조사

사용자 예제 프로젝트에서 지시자는 `./include.jsp`를 참조하지만 실제 파일명은 `incldue.jsp`였으며, include.jsp는 존재하지 않았다. 같은 파일명으로 연결한 HTML 본문에서는 public String test()의 완성과 괄호 삽입이 정상 동작했다. public void test()도 스크립틀릿 기본 완성에서 확인했다. void 반환형만으로 기본 완성 실패를 단정하지 않는다.

파일명을 include.jsp로 맞추거나 지시자를 실제 incldue.jsp로 바꾸면 문맥을 연결할 수 있다. 실제 예제 프로젝트 파일은 변경하지 않았다. 플러그인 제품 코드는 변경하지 않고 재현·회귀 테스트와 명세를 보강했다.

후속 검증은 위 명령의 작업을 test로, 테스트 선택을 `--tests '*JspIncludeContextTest'`로 지정하여 실행했다. include 통합 테스트 22개가 통과했고 실패·오류는 0개였다. 제품 코드 변경이 없어 배포 ZIP 재빌드는 하지 않았다. 기존 환경 제한은 동일하다.
