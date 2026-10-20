# 작업 목록: 지시자 포매팅 정렬

## 1단계: 재현

- [x] T001 src/test/kotlin/com/look4counter/jsp/JspDirectiveFormattingTest.kt에 사용자 예시와 잘못 들여쓴 예시의 재정렬 테스트를 작성하고 수정 전 결과를 확인한다. FR-001, FR-002.

## 2단계: 사용자 이야기 1 — 최상위 정렬

독립 검증: 세 지시자와 doctype이 0열에 있고 반복 실행 결과가 같다.

- [x] T002 [US1] src/main/kotlin/com/look4counter/jsp/JspFormattingModelBuilder.kt에 지시자 블록의 명시적 추가 들여쓰기 없음 정책을 적용한다. FR-001, FR-002.
- [x] T003 [US1] src/test/kotlin/com/look4counter/jsp/JspDirectiveFormattingTest.kt에서 여러 줄·작은따옴표·빈 줄·주석의 원문 보존을 검증한다. FR-003.

## 3단계: 사용자 이야기 2 — 기존 재정렬 유지

독립 검증: HTML 내부 지시자·일반 HTML·선택 영역 및 기존 다른 언어 회귀를 통과한다.

- [x] T004 [US2] src/test/kotlin/com/look4counter/jsp/JspDirectiveFormattingTest.kt에 HTML 부모 구조·선택 영역 보존 검증을 추가한다. FR-004.

## 4단계: 마무리

- [x] T005 README.md에 동작을 반영하고 전체 테스트·빌드·설정·ZIP 검증 결과를 specs/014-directive-format-indent/quickstart.md에 기록한다. FR-005.

## 의존성과 구현 전략

T001 → T002 → T003 → T004 → T005 순서다. 최소 기능은 사용자 예시이며 다른 구조를 보존하는 검증까지 완료한다. 소스·검증 파일을 공유하므로 편집은 순차 수행한다.
