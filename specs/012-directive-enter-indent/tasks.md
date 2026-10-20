# 작업 목록: 지시자 Enter 들여쓰기

## 1단계: 재현과 기반

- [x] T001 src/test/kotlin/com/look4counter/jsp/JspDirectiveEnterTest.kt에 실제 Enter로 사용자 예시를 재현하고 수정 전 결과를 확인한다. FR-001.

## 2단계: 사용자 이야기 1 — 지시자 다음 줄

독립 검증: 최상위 지시자 다음 줄의 공백이 없고 기존 공백·탭은 유지된다.

- [x] T002 [US1] src/main/kotlin/com/look4counter/jsp/JspDirectiveEnterHandler.kt에 범위 제한 Enter 처리를 구현하고 src/main/resources/META-INF/plugin.xml에 등록한다. FR-001, FR-002.
- [x] T003 [US1] src/test/kotlin/com/look4counter/jsp/JspDirectiveEnterTest.kt에 page·include·taglib·JSPF·공백·탭·반복·커서·실행 취소를 검증한다. FR-002, FR-004.

## 3단계: 사용자 이야기 2 — 기존 입력 유지

독립 검증: 비대상 위치는 기본 Enter를 사용하며 기존 편집 회귀가 유지된다.

- [x] T004 [US2] src/test/kotlin/com/look4counter/jsp/JspDirectiveEnterTest.kt에 HTML·Java·미완성·인라인 지시자와 다른 파일 유형의 비대상 처리를 검증한다. FR-003.

## 4단계: 마무리

- [x] T005 README.md에 사용 동작을 기록하고 전체 테스트·빌드·설정·ZIP 검증 결과를 specs/012-directive-enter-indent/quickstart.md에 남긴다. FR-004.

## 의존성과 구현 전략

T001 → T002 → T003 → T004 → T005 순서로 수행한다. 테스트와 등록 파일을 공유하므로 순차 편집한다. 최소 기능은 사용자 예시이며 비대상 회귀까지 검증해 완료한다.
