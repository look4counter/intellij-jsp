# 작업 목록: JSP 웹 코드 포매팅

입력: [명세](spec.md), [계획](plan.md), 연구·데이터 모델·계약·검증 안내.

## 1단계: 준비

- [x] T001 261 포매팅 API 및 기존 등록을 확인하고 specs/004-jsp-formatting/research.md에 근거를 기록한다.

## 2단계: 기반

- [x] T002 기존 빌드·검증 구성과 .gitignore의 임시 산출물 제외를 확인한다.

## 3단계: 사용자 이야기 1 — 웹 코드 재정렬

목표: 표준 재정렬로 기존 웹 코드 스타일을 적용한다. 독립 검증: 동일 웹 내용의 HTML 결과와 비교한다.

- [x] T003 [US1] src/test/kotlin/com/look4counter/jsp/JspFormattingTest.kt에 HTML·JS·CSS 비교, 코드 스타일과 선택 범위 테스트를 작성하여 실패를 확인한다. (FR-001·002·004, SC-001)
- [x] T004 [US1] src/main/kotlin/com/look4counter/jsp/JspFormattingModelBuilder.kt에 템플릿 모델을 구현하고 src/main/resources/META-INF/plugin.xml에 등록한다. (FR-001·002·004)

## 4단계: 사용자 이야기 2 — 서버 구문 보존

목표: 모든 서버 토큰을 보존한다. 독립 검증: 원문 토큰 비교와 반복 재정렬을 수행한다.

- [x] T005 [US2] src/test/kotlin/com/look4counter/jsp/JspFormattingTest.kt에 서버 구문·속성·웹 코드 내부 보존, 공백 의미 영역, 빈 문서와 미완성 구문 검증을 추가한다. (FR-003·005, SC-002·003)
- [x] T006 [US2] src/main/kotlin/com/look4counter/jsp/JspFormattingModelBuilder.kt의 서버 말단 블록 및 병합 정책을 검증하고 필요한 보정을 적용한다. (FR-003·005)

## 5단계: 문서와 통합 검증

- [x] T007 [P] README.md에 Ctrl+Alt+L 사용법, 재사용 범위와 서버 본문 보존 정책을 기록한다.
- [x] T008 포매팅 및 기존 전체 테스트, buildPlugin, verifyPluginProjectConfiguration을 실행하고 specs/004-jsp-formatting/quickstart.md에 결과와 한계를 기록한다. (FR-006, SC-004)
- [x] T009 specs/004-jsp-formatting의 의도와 실제 코드·패키지를 converge로 점검하고 남은 작업을 완료한다.

## 의존성과 실행 전략

T001 → T002 → T003 → T004 → T005 → T006 → T008 → T009 순서로 진행한다. T007은 T004 후 다른 파일 작업과 병렬 진행할 수 있다. 두 이야기의 테스트는 같은 파일이므로 순차 작성한다. 최소 제공 단위는 이야기 1이나 서버 보존까지 완료한 뒤 배포한다. 각 검증의 독립 실행은 JspFormattingTest의 해당 테스트 메서드를 선택한다.

