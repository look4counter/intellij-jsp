# 작업 목록: 이동과 지시자 완성

## 1단계: 설정

- [x] T001 src/test/kotlin/com/look4counter/jsp/JspNavigationTest.kt 및 JspDirectiveCompletionTest.kt에 재현 테스트를 작성한다. (FR-001~006)

## 2단계: 공통 기반

- [x] T002 src/main/kotlin/com/look4counter/jsp/JspDirective.kt와 JspParserDefinition.kt에 지시자 PSI·공통 구문을 추가하고 JspIncludeContext.kt와 구문·경로를 공유한다. (FR-001, FR-006)

## 3단계: 사용자 이야기 1 — 파일 이동

독립 검증: 경로 참조와 플랫폼 선언 이동이 실제 파일을 반환한다.

- [x] T003 [US1] src/main/kotlin/com/look4counter/jsp/JspDirective.kt에 file 값 참조를 구현한다. (FR-001)

## 4단계: 사용자 이야기 2 — 함수 이동

독립 검증: 중첩·오버로드·수정 뒤 실제 파일과 이름 오프셋을 반환한다.

- [x] T004 [US2] src/main/kotlin/com/look4counter/jsp/JspJavaSourceMap.kt 및 JspJavaInjector.kt에 가상 Java 원본 범위 매핑을 기록한다. (FR-002, FR-003)
- [x] T005 [US2] src/main/kotlin/com/look4counter/jsp/JspGotoDeclarationHandler.kt에 해석 결과 기반 원본 이동을 구현하고 plugin.xml에 등록한다. (FR-002, FR-003)

## 5단계: 사용자 이야기 3 — 지시자 완성

독립 검증: 이름·속성·값 후보와 삽입·중복 방지 결과를 확인한다.

- [x] T006 [US3] src/main/kotlin/com/look4counter/jsp/JspDirectiveCompletionContributor.kt에 문맥별 후보와 삽입을 구현하고 plugin.xml에 등록한다. (FR-004~006)

## 6단계: 검증과 마무리

- [x] T007 src/test/kotlin/com/look4counter/jsp/JspNavigationTest.kt 및 JspDirectiveCompletionTest.kt에 경계·음성·변경 반영 검증을 보강한다. (FR-003, FR-006)
- [x] T008 전체 테스트·빌드·배포물 검증 후 specs/008-navigation-directives/quickstart.md에 결과를 기록한다. (FR-007, SC-001~004)
- [x] T009 README.md에 이동과 지시자 완성 동작을 기록한다. (FR-001~006)

## 의존성과 전략

T001 → T002 → T003 → T004 → T005 → T006 → T007 → T008 → T009 순서다. 테스트 파일과 설계 검토는 병행 가능하다. 파일 이동을 먼저 완성하고 함수 이동, 지시자 완성으로 확장한다. 각 이야기는 독립적으로 실제 플랫폼 결과를 검증한다.


