# 작업 목록: JSP 자바 편집

## 1단계: 설정

- [x] T001 build.gradle.kts와 src/main/resources/META-INF/plugin.xml에 Java 의존성을 추가한다. (FR-001)

## 2단계: 공통 기반

- [x] T002 src/main/kotlin/com/look4counter/jsp/JspParserDefinition.kt와 JspJavaInjectionHost.kt에 복합 호스트·본문 범위·원문 갱신을 구현한다. (FR-006)

## 3단계: 사용자 이야기 1 — 자동완성

독립 검증: 변수, 메서드, import, 내장 객체와 분리된 제어문을 확인한다.

- [x] T003 [US1] src/test/kotlin/com/look4counter/jsp/JspJavaEditingTest.kt에 주입 및 완성 통합 테스트를 먼저 작성한다. (FR-001~003)
- [x] T004 [US1] src/main/kotlin/com/look4counter/jsp/JspJavaInjector.kt에 문맥 구성과 다중 호스트 주입을 구현하고 plugin.xml에 등록한다. (FR-001~003)

## 4단계: 사용자 이야기 2 — 구문강조

독립 검증: 세 종류 영역의 Java 토큰 강조와 JSP 경계 보존을 확인한다.

- [x] T005 [US2] src/test/kotlin/com/look4counter/jsp/JspJavaEditingTest.kt에 강조 및 편집 복구 검증을 추가하고 src/main/kotlin/com/look4counter/jsp/JspJavaSyntaxHighlighter.kt와 JspEditorHighlighterProvider.kt에 Java 렉서 계층을 연결한다. (FR-004, FR-006)

## 5단계: 사용자 이야기 3 — 포매팅

독립 검증: Java 간격, 반복 실행 및 선택 외부 보존을 확인한다.

- [x] T006 [US3] src/test/kotlin/com/look4counter/jsp/JspJavaEditingTest.kt와 JspFormattingTest.kt에 새 포매팅 계약을 검증한다. (FR-005)
- [x] T007 [US3] src/main/kotlin/com/look4counter/jsp/JspJavaFormatting.kt와 plugin.xml에 Java 사본 포매팅 및 선택 본문 반영 전처리를 구현한다. (FR-005)

## 6단계: 통합 검증

- [x] T008 전체 테스트 및 buildPlugin, verifyPluginProjectConfiguration을 실행하고 specs/005-java-editor/quickstart.md에 결과를 기록한다. (FR-007, SC-001~004)
- [x] T009 README.md에 Java 편집 동작과 의존성을 기록한다. (FR-001~006)

## 의존성과 실행 전략

T001 → T002 → T003 → T004 후 T005, 이어서 T006 → T007 → T008 → T009 순으로 수행한다. 같은 테스트 파일을 공유하므로 순차 실행한다. 별도 파일인 문서 초안 검토와 테스트 결과 검토는 병행 가능하다. 자동완성을 최소 동작 단위로 검증한 뒤 강조와 포매팅을 완성한다.



