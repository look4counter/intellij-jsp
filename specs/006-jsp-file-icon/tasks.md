# 작업 목록: IntelliJ 기본 JSP 아이콘

## 1단계: 설정

- [x] T001 SDK에서 AllIcons.FileTypes.Jsp 제공 여부를 확인하고 specs/006-jsp-file-icon/research.md에 기록한다. (FR-001)

## 2단계: 공통 기반

- [x] T002 src/main/resources/icons/jsp.svg와 jsp_dark.svg를 제거한다. (FR-003)

## 3단계: 사용자 이야기 1 — 기본 아이콘 표시

독립 검증: 컴파일된 getIcon이 플랫폼 JSP 아이콘을 직접 반환하는지 확인한다.

- [x] T003 [US1] src/main/kotlin/com/look4counter/jsp/JspFileType.kt의 반환값을 AllIcons.FileTypes.Jsp로 바꾼다. (FR-001, FR-002)

## 4단계: 검증과 문서

- [x] T004 README.md와 specs/006-jsp-file-icon/quickstart.md를 갱신하고 빌드·설정·배포물을 검증한다. (FR-003, SC-001~003)

## 의존성과 실행 전략

T001 → T002 → T003 → T004 순서로 진행한다. 리소스 제거와 Kotlin 변경은 별도 파일이므로 병행 가능하지만 작은 변경이므로 순차 수행한다. 기본 아이콘 적용 전체가 단일 완료 단위다.

