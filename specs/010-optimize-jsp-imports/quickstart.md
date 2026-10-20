# 검증 안내: JSP 미사용 import 정리

현재 요구사항은 정리 기능 전체의 제거다. 아래 추가 기능의 사용 시나리오와 116개 검증 결과는 철회 전 이력이며 현재 사용 안내가 아니다. 제거 후 최신 검증 결과는 문서 마지막에 기록한다.

## 사용 시나리오

1. 사용·미사용 public 클래스를 만들고 JSP page import 목록에 넣는다.
2. Java 본문 또는 HTML에서 Ctrl+O를 실행해 미사용 항목만 제거되는지 확인한다.
3. 다른 속성이 있는 지시자, 따옴표·한글·여러 지시자 보존을 확인한다.
4. 선언·표현식·스크립틀릿과 include에서 사용하는 import 보존을 확인한다.
5. 모호한 부모·누락 include·미완성 Java·와일드카드·미해결 클래스 보존을 확인한다.
6. 반복 실행·실행 취소와 비대상 파일의 단축키를 확인한다.

## 실행

기존 [로컬 검증 환경](../009-java-auto-import/quickstart.md)의 명령으로 `test buildPlugin verifyPluginProjectConfiguration`을 실행한다. 신규 분석 테스트와 전체 편집 회귀를 함께 검증한다. 구현 후 결과를 기록한다.

## 검증 결과

2026-10-20, IntelliJ IDEA 2026.1.3 SDK에서 전체 테스트 116개가 통과했다. 신규 정리 테스트는 19개이며 실패·오류는 0개다. `buildPlugin`과 `verifyPluginProjectConfiguration`도 성공했다.

- 실제 등록 액션으로 미사용 제거, 세 Java 영역의 사용 보존, 여러 지시자와 다른 속성·따옴표·주변 원문 보존을 확인했다.
- 일반 주석·문자열·HTML의 이름과 완전한 클래스 이름 사용은 불필요한 import를 유지시키지 않는다.
- 와일드카드·미해결 import, 미완성 코드·잘못된 목록·미해결 참조는 보수적으로 유지한다.
- 정적 include의 사용, 부모·조각 원문, 공유 조각·누락 경로·순환 문맥 및 JSP 클래스 속성을 검증했다.
- 현재 파일 및 include의 EL에서 등장하는 클래스 이름을 유지하면서 다른 미사용 항목은 제거한다.
- 실제 키맵의 Ctrl+O 등록, 주입 Java 파일에서의 호스트 선택, 액션 우선순위, 일반 Java 비활성화를 확인했다.
- 한 번의 실행 취소, 반복 실행, 오래된 분석 결과 폐기와 인덱싱 중 비활성화를 확인했다.
- 기존 자동 import·HTML·JS·CSS·Java 편집·include·이동·포매팅 회귀가 모두 통과했다.
- ZIP 내부 정리 클래스·액션·우선순위 제공자와 Ctrl+O 등록을 확인했으며 테스트 클래스는 포함되지 않는다.

## 재현 명령

```powershell
$env:GRADLE_USER_HOME = "$PWD/.gradle-user-home"
$env:GRADLE_RO_DEP_CACHE = 'C:/Users/tcpip147/.gradle/caches'
.\.gradle-user-home/gradle-9.7.1/bin/gradle.bat test buildPlugin verifyPluginProjectConfiguration -PlocalIdePath=D:/Common/idea-2026.1.3.win '-PlocalTestFrameworkPath=D:/Project/intellij-jsp/.gradle-user-home/test-harness/test-framework-compat.jar' -PinstrumentCode=false -PtestWebLanguages=true '-Pkotlin.incremental=false' --offline --no-daemon --no-configuration-cache -I .gradle-user-home/local-test-cache.init.gradle.kts '-Porg.gradle.java.installations.paths=D:/Common/idea-2026.1.3.win/jbr,D:/Project/intellij-jsp/.gradle-user-home/caches/9.7.1/transforms/41d2ee0b381ac1ce59d15aa3eb4932b1/transformed/idea-2025.3.6.1-win/jbr'
```

## 검증 환경과 수렴 결과

기존 로컬 호환 테스트 도구·읽기 전용 의존성 캐시를 사용하고 검증에서만 계측을 껐다. 제품 코드와 언어 엔진은 실제 261 SDK다. 플랫폼 액션·키맵·우선순위 경로로 검증했으며 IDE 화면에서 물리적으로 Ctrl+O를 누르는 수동 검증과 정식 261 테스트 프레임워크 재검증은 수행하지 않았다.

`speckit-converge`에서 EL 사용 보존의 부분 구현을 찾아 T008을 추가했고 구현·검증을 완료했다. 재점검에서 FR-001~007, 두 사용자 이야기·편집 계약·작업 T001~T008에 남은 구현 차이는 없다. 헌장은 미작성 템플릿으로 원칙 검사를 생략했다.

설치 파일은 `build/distributions/intellij-jsp-1.0.0-SNAPSHOT.zip`이다. 새 ZIP을 설치한 뒤 IDE를 재시작한다.

## 기능 철회 후 최신 검증

2026-10-20 사용자 요청으로 Ctrl+O 미사용 import 정리 기능 전체를 제거했다. 액션·단축키·우선순위 제공자·분석기·전용 테스트와 include 전용 메타데이터를 제거했다. Java import 자동 추가 기능은 유지한다.

- 동일 로컬 검증 명령의 `test buildPlugin verifyPluginProjectConfiguration`이 성공했다. 기존 테스트 97개의 성공 결과를 Gradle 캐시에서 재사용했으며 실패·오류는 0개다.
- 배포 ZIP에서 정리 클래스·액션·Ctrl+O·우선순위 등록이 모두 없고 Java 자동 import 클래스는 포함됨을 확인했다.
- JspIncludeContext.kt는 정리 기능 추가 전 소스와 차이가 없다.
- speckit-converge로 현재 제거 명세 FR-001~004와 작업 T009~T011을 대조했으며 남은 구현 차이는 없다.

위의 116개 테스트와 정리 기능 안내는 철회 전 이력이다. 현재 배포물은 미사용 import 정리를 제공하지 않는다.
