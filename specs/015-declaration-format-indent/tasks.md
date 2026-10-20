# 작업 목록: 선언부와 함수 포매팅

## 1단계: 준비

- [x] T001 specs/015-declaration-format-indent/checklists/requirements.md의 완료 상태와 기존 포매터·주입 경로 및 .gitignore를 확인한다.

## 2단계: 재현 기반

- [x] T002 src/test/kotlin/com/look4counter/jsp/JspDeclarationFormattingTest.kt에 사용자 예시의 정확한 기대 정렬 테스트를 만들고 수정 전 실패를 확인한다. FR-001, FR-002.

## 3단계: 사용자 이야기 1 — 선언부 정렬

독립 검증: 사용자 예시의 구분자 0칸·함수 4칸·본문 8칸 및 HTML 내부 위치가 정확하고 반복 결과가 같다.

- [x] T003 [US1] src/main/kotlin/com/look4counter/jsp/JspFormattingModelBuilder.kt에서 선언부 호스트와 잎에 HTML 부모 들여쓰기 정책을 적용한다. FR-001.
- [x] T004 [US1] src/main/kotlin/com/look4counter/jsp/JspJavaFormatting.kt에서 전체 여러 줄 선언부의 Java 포매팅과 경계 공백 정렬을 공유 후처리로 구현하고 src/main/resources/META-INF/plugin.xml에 등록한다. FR-002.
- [x] T005 [US1] src/test/kotlin/com/look4counter/jsp/JspDeclarationFormattingTest.kt에 HTML 내부·여러 선언부·빈 선언부·미완성 선언부·반복 재정렬 검증을 추가한다. FR-001, FR-002, FR-004.

## 4단계: 사용자 이야기 2 — 함수 스타일과 보존

독립 검증: 함수 스타일 설정을 따르며 문자열·주석과 선택 밖 텍스트가 같다.

- [x] T006 [US2] src/test/kotlin/com/look4counter/jsp/JspDeclarationFormattingTest.kt에서 Java 들여쓰기·중괄호·중첩 블록·탭과 문자열·주석 및 전체/부분 선택을 검증하고 src/main/kotlin/com/look4counter/jsp/JspJavaFormatting.kt에 필요한 보존 처리를 적용한다. FR-003, FR-004.

## 5단계: 마무리

- [x] T007 README.md에 선언부 정책을 반영하고 전체 테스트·배포 빌드·설정·ZIP 검증 결과를 specs/015-declaration-format-indent/quickstart.md에 기록한다. FR-005.

## 의존성과 구현 전략

T001 → T002 → T003 → T004 → T005 → T006 → T007 순서로 진행한다. 최소 범위는 사용자 예시이며 스타일·선택 보존까지 완료한다. 두 이야기 모두 같은 포매터와 테스트 파일을 공유하므로 구현은 순차 진행한다. 읽기 전용 연구와 원문 조사만 병렬로 가능하다.
