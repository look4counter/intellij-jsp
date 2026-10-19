# 작업 목록: JSP 파일 열기

**입력**: [명세](spec.md), [계획](plan.md), [조사](research.md), [계약](contracts/editor.md)

## 1단계: 환경 확인

- [x] T001 build.gradle.kts와 .gitignore의 대상 버전, 기본 API 및 빌드 산출물 제외 설정을 확인한다.

## 2단계: 기반 의존성

- [x] T002 src/main/resources/META-INF/plugin.xml의 불필요한 LSP 의존성을 플랫폼 모듈로 바꾼다(FR-003).

## 3단계: 사용자 이야기 1 — JSP 열기·편집·저장

목표: 기본 텍스트 에디터에서 JSP를 열고 저장한다.
독립 검증: quickstart.md의 일반 파일, 빈 파일, 한글 저장과 재열기 시나리오.

- [x] T003 [US1] src/main/kotlin/com/look4counter/jsp/JspFileType.kt에 인코딩을 강제하지 않는 비바이너리 JSP 유형을 구현한다(FR-002, FR-004).
- [x] T004 [US1] src/main/resources/META-INF/plugin.xml에 jsp 확장자만 선언적으로 등록하고 기능 설명을 갱신한다(FR-001, FR-005).

## 4단계: 문서와 검증

- [x] T005 [P] README.md에 한글로 기능 범위, 대상 버전, 실행·빌드·설치와 기존 파일 연결 조정 방법을 기록한다.
- [x] T006 specs/001-open-jsp-editor/quickstart.md에 빌드와 구성 검사 실행 결과 및 수동 수용 검증 상태를 기록한다.

## 의존성과 병렬 실행

T001 → T002 → T003 → T004 → T006 순으로 진행한다. T005는 T003·T004와 별도 파일이므로 병렬 진행할 수 있다. 사용자 이야기 하나가 이번 최소 기능 전체이다.

## 구현 전략

유형 등록 후 기본 에디터를 재사용한다. 별도 저장 로직이나 파서는 추가하지 않는다. 검증 후 converge로 명세와 구현의 차이를 점검한다.


