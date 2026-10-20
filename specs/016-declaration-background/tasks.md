# 작업 목록: JSP Java 영역 배경 제거

## 1단계: 준비

- [x] T001 `specs/016-declaration-background/spec.md`의 범위를 확장하고 명확화·품질 검토를 완료한다.

## 2단계: 기반

- [x] T002 공통 호스트 설계를 `specs/016-declaration-background/plan.md`와 조사·데이터 구조·표시 계약에 반영한다.

## 3단계: 사용자 이야기 1

**목표**: 모든 JSP Java 영역의 배경을 제거한다.
**독립 검증**: 세 종류의 플랫폼 배경 판단, Java 주입과 원문 보존을 검사한다.

- [x] T003 [US1] `src/test/kotlin/com/look4counter/jsp/JspDeclarationBackgroundTest.kt`의 기존 검증을 세 종류·빈·미완성·갱신으로 확장한다. FR-001~002, SC-001.
- [x] T004 [US1] `src/main/kotlin/com/look4counter/jsp/JspJavaInjectionHost.kt`와 `JspParserDefinition.kt`에서 배경 억제를 공통 호스트에 적용한다. FR-001~003.

## 4단계: 마무리

- [x] T005 전체 테스트·플러그인 빌드·설정 검증을 실행하고 `specs/016-declaration-background/quickstart.md`에 기록한다. SC-002~003.

## 의존성과 구현 전략

T001 → T002 → 구현 전 일관성 분석 → T003 → T004 → T005 → 수렴 검토 순서다. 단일 이야기 전체가 최소 구현 범위다. 같은 호스트 정책을 다루므로 병렬 구현은 하지 않는다.
