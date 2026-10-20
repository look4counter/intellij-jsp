# 검증 안내

## 시나리오

public 클래스를 추가하고 JSP 스크립틀릿·표현식·선언에서 Ctrl+Space로 선택한다. page import와 단순 이름 해석을 확인한다. 미해결 이름은 Alt+Enter의 JSP import 추가 후보를 선택한다.

명시적·와일드카드·include 중복 방지, 동명 충돌의 완전한 이름 처리, 조각 편집 시 부모 보존, 주석·문자열·다른 언어 후보 배제를 검증한다. 커서와 실행 취소도 확인한다.

## 실행

`gradlew.bat test buildPlugin verifyPluginProjectConfiguration`을 실행한다. 로컬 SDK·호환 테스트 프레임워크 검증 결과와 명령은 구현 완료 후 기록한다.

## 검증 결과

2026-10-20, IntelliJ IDEA 2026.1.3 SDK에서 전체 테스트 97개가 통과했다. 실패·오류는 0개이며 신규 import 테스트는 11개다. 전체 HTML·JS·CSS·Java 편집·include·이동 회귀를 함께 실행했다.

- 스크립틀릿·표현식·선언의 클래스 완성과 page import 추가를 확인했다.
- 명시적·와일드카드·include import 재사용, 동명 충돌의 완전한 이름 사용, 부모 파일 보존을 확인했다.
- 생성자 괄호·커서, import 후 클래스 해석, 완성과 빠른 수정의 실행 취소를 확인했다.
- 패키지별 빠른 수정 후보, 접근 불가 클래스 제외, 선택 패키지와 제네릭 인자 보존을 확인했다.
- HTML·EL·문자열·주석 제외와 include의 편집 불가 가상 참조 제외를 확인했다.
- `buildPlugin`, `verifyPluginProjectConfiguration`이 성공했다. ZIP 내부에 구현 클래스와 확장 등록이 포함되며 테스트 클래스는 포함되지 않는다.

## 재현 명령

```powershell
$env:GRADLE_USER_HOME = "$PWD/.gradle-user-home"
$env:GRADLE_RO_DEP_CACHE = 'C:/Users/tcpip147/.gradle/caches'
.\.gradle-user-home/gradle-9.7.1/bin/gradle.bat test buildPlugin verifyPluginProjectConfiguration -PlocalIdePath=D:/Common/idea-2026.1.3.win '-PlocalTestFrameworkPath=D:/Project/intellij-jsp/.gradle-user-home/test-harness/test-framework-compat.jar' -PinstrumentCode=false -PtestWebLanguages=true '-Pkotlin.incremental=false' --offline --no-daemon --no-configuration-cache -I .gradle-user-home/local-test-cache.init.gradle.kts '-Porg.gradle.java.installations.paths=D:/Common/idea-2026.1.3.win/jbr,D:/Project/intellij-jsp/.gradle-user-home/caches/9.7.1/transforms/41d2ee0b381ac1ce59d15aa3eb4932b1/transformed/idea-2025.3.6.1-win/jbr'
```

## 검증 환경과 수렴 점검

기존 로컬 호환 테스트 도구와 읽기 전용 의존성 캐시를 사용했으며 검증에서만 계측을 껐다. 호환 테스트 도구의 하이라이트 실행 API가 261 SDK와 달라, 빠른 수정은 실제 확장 등록을 백그라운드 읽기에서 호출하여 후보를 수집하고 쓰기 명령에서 실행했다. IDE 화면에서 Alt+Enter 메뉴를 직접 조작하거나 정식 261 테스트 프레임워크로 재검증하지는 않았다.

`speckit-converge`에서 FR-005의 편집 불가 가상 영역 검사 보완을 T009로 추가해 구현·검증했다. 재점검 결과 FR-001~007, 두 사용자 이야기와 편집 계약, 작업 T001~T009에 남은 구현 차이는 없다. 헌장은 미작성 템플릿으로 원칙 검사는 생략했다.

설치 파일: `build/distributions/intellij-jsp-1.0.0-SNAPSHOT.zip`. 실행 중인 IDE는 새 ZIP을 설치한 뒤 재시작해야 한다.
