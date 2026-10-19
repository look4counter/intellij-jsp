# 작업 목록: JSP 혼합 구문 강조

**입력**: [명세](spec.md), [계획](plan.md), [계약](contracts/highlighting.md)

## 1단계: 준비

- [x] T001 build.gradle.kts에 XML 모듈과 testWebLanguages 선택적 웹 테스트 의존성을 구성한다.

## 2단계: 공통 기반

- [x] T002 src/test/kotlin/com/look4counter/jsp/JspHighlightingLexerTest.kt에 JSP 종류, EL 중첩·이스케이프, 미완성 구문, 토큰 재시작 검증을 작성한다(FR-002, FR-006).

## 3단계: 사용자 이야기 1 — HTML·JSP 강조

독립 검증: HTML 강조 결과 비교, JSP 영역 구분과 기본 편집기 등록을 확인한다.

- [x] T003 [US1] src/main/kotlin/com/look4counter/jsp/JspHighlightingLexer.kt에 원문 전체를 덮는 JSP·HTML 경계 lexer를 구현한다(FR-002, FR-006).
- [x] T004 [US1] src/main/kotlin/com/look4counter/jsp/JspSyntaxHighlighter.kt에 종류별 테마 기반 색상 키를 구현한다(FR-002, FR-007).
- [x] T005 [US1] src/test/kotlin/com/look4counter/jsp/JspEditorHighlighterTest.kt에 실제 HTML 강조 비교와 문서 편집 갱신 검증을 작성한다(FR-001, FR-005, FR-006, FR-007).
- [x] T006 [US1] src/main/kotlin/com/look4counter/jsp/JspEditorHighlighterProvider.kt와 src/main/resources/META-INF/plugin.xml에 HTML 레이어를 등록하고 JspLanguage.kt·JspFileType.kt를 언어 파일 유형으로 연결한다(FR-001, FR-007).

## 4단계: 사용자 이야기 2 — 선택적 JS·CSS 강조

독립 검증: 웹 기능이 있는 구성과 없는 구성 모두에서 실제 HTML 강조기를 기준으로 JSP 호스트 구간을 비교한다.

- [x] T007 [US2] src/test/kotlin/com/look4counter/jsp/JspEditorHighlighterTest.kt에 script·style 및 그 안의 JSP 삽입 전후 강조와 웹 기능 미설치 구성을 검증한다(FR-003, FR-004, FR-005).
- [x] T008 [US2] src/test/testData/highlighting.jsp에 HTML·JSP·JS·CSS와 한글을 포함한 수동 검증 예제를 추가한다.

## 5단계: 검증과 문서

- [x] T009 README.md에 지원 구문과 선택적 JS·CSS 지원, 테스트 실행 방법을 갱신한다.
- [x] T010 specs/002-jsp-syntax-highlighting/quickstart.md에 기본·웹 테스트 및 빌드·구성 검사 결과와 수동 검증 상태를 기록한다.

## 의존성과 병렬 실행

T001 → T002 → T003 → T004 → T005 → T006 → T007 → T010 순으로 진행한다. T008과 T009는 코드 구현 완료 후 독립 파일이므로 함께 진행할 수 있다. 사용자 이야기 2는 이야기 1의 HTML 레이어에 의존한다.

## 구현 전략

먼저 lexer 경계와 HTML 재사용을 완성하고, 같은 HTML 레이어가 실제 웹 언어 지원을 재사용하는지 검증한다. 모든 검증 후 converge에서 남은 작업을 확인한다.




