# 작업 목록: 2026.1 JSP 웹 자동완성

**입력**: [명세](spec.md), [계획](plan.md), [계약](contracts/completion.md)

## 1단계: 준비

- [x] T001 build.gradle.kts의 기본 대상을 2026.1.3으로 바꾸고 localIdePath 검증 경로 및 최소 지원 빌드 261을 설정한다(FR-001).

## 2단계: 기반 검증

- [x] T002 src/test/kotlin/com/look4counter/jsp/JspCompletionTest.kt에 HTML PSI 원문·위치와 JSP 영역 구분 테스트를 작성한다(FR-005, FR-006).

## 3단계: 사용자 이야기 1 — 버전 전환과 기존 기능 유지

독립 검증: 2026.1에서 기존 강조 테스트와 구성 검사를 실행한다.

- [x] T003 [US1] src/main/kotlin/com/look4counter/jsp/JspLanguage.kt 및 JspHighlightingLexer.kt에 템플릿 언어와 HTML outer 토큰·JSP 언어 토큰을 연결한다(FR-002).
- [x] T004 [US1] src/test/kotlin/com/look4counter/jsp/JspEditorHighlighterTest.kt와 JspHighlightingLexerTest.kt를 2026.1에서 검증하고 필요한 API 변경을 반영한다(FR-001, FR-002, FR-007).

## 4단계: 사용자 이야기 2 — HTML 완성

독립 검증: 태그·속성 후보와 JSP 전후의 실제 삽입 위치를 확인한다.

- [x] T005 [US2] src/test/kotlin/com/look4counter/jsp/JspCompletionTest.kt에 HTML 태그·속성 및 후보 선택 후 원문 보존 검증을 작성한다(FR-003, FR-005, FR-006).
- [x] T006 [US2] src/main/kotlin/com/look4counter/jsp/JspParserDefinition.kt에 단순 JSP parser와 PSI 파일을 구현한다.
- [x] T007 [US2] src/main/kotlin/com/look4counter/jsp/JspFileViewProvider.kt에 HTML 템플릿 데이터와 다중 root·복사 지원을 구현하고 src/main/resources/META-INF/plugin.xml에 등록한다(FR-003, FR-005, FR-006).

## 5단계: 사용자 이야기 3 — JS·CSS 완성

독립 검증: 웹 구성에서 JS 지역 변수와 CSS 속성 후보·삽입 및 JSP 혼합 문맥을 확인한다.

- [x] T008 [US3] src/test/kotlin/com/look4counter/jsp/JspCompletionTest.kt에 기존 웹 언어 기능의 후보·삽입과 JSP 혼합 문맥 검증을 추가하고 실제 연결을 확인한다(FR-004, FR-005, FR-007).
- [x] T009 [US3] src/test/testData/completion.jsp에 수동 검증 예제를 추가한다.

## 6단계: 검증과 문서

- [x] T010 README.md와 src/main/resources/META-INF/plugin.xml의 대상 버전·기능 설명을 갱신한다.
- [x] T011 specs/003-web-completion-2026/quickstart.md에 기본·웹 테스트 및 빌드·구성 검사 결과와 수동 확인 상태를 기록한다.

## 의존성과 병렬 실행

T001 → T002 → T003 → T005 → T006 → T007 → T008 → T004 → T011 순으로 진행한다. T009와 T010은 별도 문서·예제 파일로 코드 검증과 독립적으로 진행할 수 있다. HTML 완성이 최소 기능이며 JS·CSS는 해당 HTML PSI 연결에 의존한다.

## 구현 전략

테스트로 PSI·완성 연결 부재를 확인한 뒤 템플릿 구조를 구현한다. 기존 언어의 후보와 삽입 핸들러를 그대로 사용하고 하드코딩하지 않는다. 모든 구현 후 converge로 남은 작업을 확인한다.



