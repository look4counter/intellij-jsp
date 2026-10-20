# 검증 안내: import 클래스 이동

1. 프로젝트 또는 라이브러리에 클래스를 등록하고 JSP·JSPF의 page import에 적는다.
2. 클래스 이름의 Ctrl+클릭 또는 Ctrl+B로 선언을 확인한다.
3. 쉼표 목록·중첩 클래스·작은따옴표·여러 지시자와 반복 속성을 확인한다.
4. 쉼표·공백·없는 클래스·와일드카드·주석에서 잘못 이동하지 않는지 확인한다.
5. 기존 include·Java 이동·자동 import 회귀와 빌드를 실행한다.

기존 [로컬 SDK 검증 명령](../009-java-auto-import/quickstart.md)으로 test buildPlugin verifyPluginProjectConfiguration을 실행한다. 완료 후 결과를 기록한다.

## 검증 결과

2026-10-20, IntelliJ IDEA 2026.1.3 SDK에서 전체 테스트 107개가 통과했다. 신규 이동 테스트는 10개이며 실패·오류는 0개다. `buildPlugin`과 `verifyPluginProjectConfiguration`도 성공했다.

- 실제 GotoDeclarationAction 경로로 단일·쉼표 목록의 각 클래스와 반복 속성·여러 지시자를 확인했다.
- JSPF·작은따옴표·큰따옴표·공백·줄바꿈·한글·중첩 클래스의 정확한 이름 범위와 이동 대상을 확인했다.
- 테스트용 의존 JAR에 등록한 소스 없는 클래스로 이동하며 JAR 내부 선언을 반환하는 것을 확인했다.
- 와일드카드·미해결·잘못된 이름·미완성 지시자·구분자·다른 속성·JSP 주석 및 인덱싱 중의 비대상 처리를 확인했다.
- 탐색 전후 원문이 같으며 기존 include·Java 함수 이동·Java import 자동 추가·완성·포매팅 회귀가 통과했다.
- ZIP 내부 import 참조 구현이 포함되고 테스트 클래스와 제거한 Ctrl+O 정리 구현이 없는 것을 확인했다.

## 환경과 수렴 점검

기존 로컬 호환 테스트 도구와 읽기 전용 의존성 캐시를 사용했으며 검증에서만 계측을 껐다. 제품 엔진은 실제 261 SDK다. Ctrl+클릭과 Ctrl+B가 사용하는 플랫폼 선언 이동 경로를 검증했으며 IDE 화면의 마우스 조작과 정식 261 테스트 프레임워크 재검증은 수행하지 않았다.

speckit-converge로 FR-001~005, 두 사용자 이야기·편집 계약·작업 T001~T006을 대조했다. 남은 구현 차이는 없으며 추가 수렴 작업은 없다. 헌장은 미작성 템플릿으로 원칙 검사를 생략했다.

설치 파일: `build/distributions/intellij-jsp-1.0.0-SNAPSHOT.zip`. 새 ZIP을 설치한 뒤 IDE를 재시작한다.
