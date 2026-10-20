# 작업 목록: 선언부 배경 제거

## 1단계: 준비

- [x] T001 명세·조사와 로컬 SDK 배경 억제 API를 확인한다. 대상: `specs/016-declaration-background/research.md`.

## 2단계: 기반

- [x] T002 선언부 전용 호스트 생성과 기존 치환 경로의 호환성을 확인한다. 대상: `src/main/kotlin/com/look4counter/jsp/JspParserDefinition.kt`, `JspJavaInjectionHost.kt`.

## 3단계: 사용자 이야기 1

**목표**: 선언부의 별도 주입 배경을 제거하고 Java 편집과 다른 영역 정책을 보존한다.
**독립 검증**: 플랫폼 배경 판단과 Java PSI 주입 및 원문 보존을 함께 검증한다.

- [x] T003 [US1] 선언부 전용 배경 억제 호스트와 생성 분기를 구현한다. 대상: `src/main/kotlin/com/look4counter/jsp/JspJavaInjectionHost.kt`, `JspParserDefinition.kt`. FR-001~003.
- [x] T004 [US1] 함수·필드·복수·빈·미완성 선언부, 갱신, 스크립틀릿·표현식 정책을 검증한다. 대상: `src/test/kotlin/com/look4counter/jsp/JspDeclarationBackgroundTest.kt`. SC-001~003.

## 4단계: 마무리

- [x] T005 전체 테스트와 플러그인 빌드·설정 검증을 수행하고 결과를 기록한다. 대상: `specs/016-declaration-background/quickstart.md`.

## 의존성과 구현 전략

T001 → T002 → T003 → T004 → T005 순서로 단일 사용자 이야기를 완료한다. 작업 범위가 작고 호스트 타입 변경을 공유하므로 병렬 구현은 하지 않는다. 전체 결과를 명세와 대조해 수렴 여부를 점검한다.
