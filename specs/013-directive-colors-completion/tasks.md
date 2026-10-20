# 작업 목록: 지시자 색상과 완성

## 1단계: 재현과 기반

- [x] T001 src/test/kotlin/com/look4counter/jsp/JspDirectiveAppearanceTest.kt와 JspDirectiveValueCompletionTest.kt에 색상·import·include 후보 선택 검증을 작성한다. FR-001, FR-004, FR-005.

## 2단계: 사용자 이야기 1 — 세부 색상

독립 검증: 의미별 색상 키가 다르고 미완성·증분 편집에서 누락이 없다.

- [x] T002 [US1] src/main/kotlin/com/look4counter/jsp/JspDirectiveSyntaxHighlighter.kt와 JspSyntaxHighlighter.kt 및 JspEditorHighlighterProvider.kt에 지시자 전용 표시 레이어와 색상 키를 연결한다. FR-001, FR-002.
- [x] T003 [US1] src/test/kotlin/com/look4counter/jsp/JspDirectiveAppearanceTest.kt에 테마·미완성·여러 지시자·증분 편집 회귀를 추가한다. SC-001.

## 3단계: 사용자 이야기 2 — 입력 완성

독립 검증: 실제 완성 선택 후 현재 항목·경로만 변경되고 원문·커서가 유지된다.

- [x] T004 [US2] src/main/kotlin/com/look4counter/jsp/JspIncludeContext.kt에 기존 경로 기준을 재사용하는 디렉터리 탐색을 제공한다. FR-005.
- [x] T005 [US2] src/main/kotlin/com/look4counter/jsp/JspDirectiveCompletionContributor.kt에 클래스·패키지·경로 후보와 범위 보존 삽입·자동 팝업을 추가한다. FR-003~008.
- [x] T006 [US2] src/test/kotlin/com/look4counter/jsp/JspDirectiveValueCompletionTest.kt에서 목록·따옴표·접미사·상대·상위·웹 루트·인덱싱·비대상·자동 팝업을 검증한다. SC-002.

## 4단계: 마무리

- [x] T007 README.md를 갱신하고 전체 회귀·빌드·설정·ZIP 검증을 specs/013-directive-colors-completion/quickstart.md에 기록한다. FR-008, SC-003.

## 의존성과 구현 전략

T001 → T002 → T003 → T004 → T005 → T006 → T007 순서로 수행한다. 두 이야기는 각자의 테스트로 검증하며 공통 등록·검증 파일 편집은 순차 수행한다. 독립 검토는 색상과 완성으로 나눌 수 있다. 최소 기능은 세부 색상이며 동적 완성과 회귀까지 완료한다.

## 5단계: 수렴 점검

- [x] T008 src/main/kotlin/com/look4counter/jsp/JspDirectiveAutoPopupHandler.kt와 src/main/resources/META-INF/plugin.xml에 현재 SDK의 typedHandler를 연결하고 JspDirectiveCompletionContributor.kt의 구형 팝업 훅을 제거한다. JspDirectiveValueCompletionTest.kt에서 실제 등록 처리기의 지시자 문맥을 검증한다. FR-006 부분 구현을 보완한다.

## 6단계: 수렴 점검

- [x] T009 src/test/kotlin/com/look4counter/jsp/JspDirectiveValueCompletionTest.kt에서 점·슬래시로 패키지·폴더 후보를 선택할 때 구분 문자가 한 번만 삽입되는지 검증하고 필요 시 JspDirectiveCompletionContributor.kt의 선택 문자 처리를 보완한다. FR-007 부분 검증을 완료한다.
