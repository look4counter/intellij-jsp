# 작업 목록: import 클래스 선언 이동

## 1단계: 설정

- [x] T001 src/test/kotlin/com/look4counter/jsp/JspImportNavigationTest.kt에 실제 선언 이동 경로의 단일·목록 클래스 테스트를 작성한다. FR-001, FR-002.

## 2단계: 기반

- [x] T002 src/main/kotlin/com/look4counter/jsp/JspDirective.kt의 파싱 결과에 원문 순서 속성 목록을 보존한다. FR-002.

## 3단계: 사용자 이야기 1 — 클래스 이동

독립 검증: 실제 GotoDeclarationAction으로 각 import의 정확한 클래스를 확인한다.

- [x] T003 [US1] src/main/kotlin/com/look4counter/jsp/JspDirective.kt에 원문 범위별 Java 클래스 참조와 해석을 구현한다. FR-001~003.
- [x] T004 [US1] src/test/kotlin/com/look4counter/jsp/JspImportNavigationTest.kt에서 JSPF·반복 속성·따옴표·공백·중첩·라이브러리 탐색과 원문 보존을 검증한다. FR-002, FR-003, SC-001.

## 4단계: 사용자 이야기 2 — 비대상 보존

독립 검증: 비대상 위치에서 이동이 없으며 기존 include·Java 탐색이 유지된다.

- [x] T005 [US2] src/test/kotlin/com/look4counter/jsp/JspImportNavigationTest.kt에 와일드카드·미해결·잘못된 이름·구분자·다른 속성·주석·인덱싱 검증을 추가한다. FR-004, SC-002.

## 5단계: 마무리

- [x] T006 README.md에 사용법을 추가하고 전체 회귀·빌드·설정·ZIP 결과를 specs/011-import-class-navigation/quickstart.md에 기록한다. FR-005, SC-003.

## 의존성과 구현 전략

T001 → T002 → T003 → T004 → T005 → T006 순서다. 최소 기능은 단일·목록 클래스 이동이며 나머지 경계 검증을 완료한 뒤 전체 회귀를 실행한다. 소스와 테스트를 공유하므로 편집은 순차 수행한다. 독립 검토가 필요하면 구현 후 사용·비대상 시나리오 검토를 병행할 수 있다.
