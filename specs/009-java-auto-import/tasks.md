# 작업 목록: Java import 자동 추가

## 1단계: 설정

- [x] T001 src/test/kotlin/com/look4counter/jsp/JspJavaImportTest.kt에 완성·빠른 수정 재현 테스트를 작성한다. (FR-001, FR-002)

## 2단계: 공통 기반

- [x] T002 src/main/kotlin/com/look4counter/jsp/JspJavaImports.kt에 현재 JSP 판별과 import 중복·충돌·원문 보존 편집 정책을 구현한다. (FR-003~006)

## 3단계: 사용자 이야기 1 — 완성과 import

독립 검증: 세 Java 영역에서 클래스 선택 후 지시자·참조 해석·생성자 괄호를 확인한다.

- [x] T003 [US1] src/main/kotlin/com/look4counter/jsp/JspJavaImportCompletionContributor.kt에 클래스 후보 삽입을 연결하고 src/main/resources/META-INF/plugin.xml에 등록한다. (FR-001, FR-005, FR-006)
- [x] T004 [US1] src/test/kotlin/com/look4counter/jsp/JspJavaImportTest.kt에 중복·충돌·include·커서·실행 취소 검증을 추가한다. (FR-003~006, SC-001, SC-003)

## 4단계: 사용자 이야기 2 — 빠른 수정

독립 검증: Alt+Enter 후보에서 원하는 패키지를 선택하고 현재 JSP만 변경되는지 확인한다.

- [x] T005 [US2] src/main/kotlin/com/look4counter/jsp/JspJavaImportQuickFixProvider.kt에 후보 접근성·유효성 검사와 클래스별 수정 동작을 구현하고 src/main/resources/META-INF/plugin.xml에 등록한다. (FR-002, FR-005, FR-006)
- [x] T006 [US2] src/test/kotlin/com/look4counter/jsp/JspJavaImportTest.kt에 복수 후보·접근 불가·주석·문자열·다른 언어 제외 검증을 추가한다. (FR-002, FR-006, SC-002, SC-003)

## 5단계: 마무리

- [x] T007 전체 테스트·빌드·설정·ZIP 검증을 실행하고 specs/009-java-auto-import/quickstart.md에 결과를 기록한다. (FR-007, SC-004)
- [x] T008 README.md에 완성·Alt+Enter 사용법과 범위를 갱신한다. (FR-001, FR-002)

## 의존성과 구현 전략

T001 → T002 → T003 → T004 → T005 → T006 → T007 → T008 순서로 수행한다. 먼저 완성 시나리오를 검증하고 빠른 수정을 연결한다. 공통 기반 완료 후 두 이야기의 구현 검토는 병행할 수 있지만 등록·테스트 파일이 공유되므로 편집은 순차 수행한다. 각 이야기는 독립적인 플랫폼 동작으로 검증한다.

## 6단계: 수렴 점검

- [x] T009 JspJavaImports.kt에서 참조 이름이 실제 편집 가능한 주입 범위에 속하는지 검사하고 JspJavaImportTest.kt에서 include로 복사한 가상 참조에 빠른 수정이 등록되지 않는지 검증한다. FR-005, 편집 계약 6번의 부분 구현을 보완한다.
