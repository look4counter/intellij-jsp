# 작업 목록: include 문맥

## 1단계: 설정

- [x] T001 src/main/resources/META-INF/plugin.xml에 jspf 확장자를 등록한다. (FR-004)

## 2단계: 공통 기반

- [x] T002 src/test/kotlin/com/look4counter/jsp/JspIncludeContextTest.kt에 포함·경로·범위 통합 테스트를 먼저 작성한다. (FR-001, FR-002)
- [x] T003 src/main/kotlin/com/look4counter/jsp/JspIncludeContext.kt에 지시자 분석·경로 해석·순환 방지 번역 단위를 구현한다. (FR-001, FR-002, FR-006)

## 3단계: 사용자 이야기 1 — 포함 파일 사용

독립 검증: 포함된 변수·메서드·import 완성과 지역 변수 순서를 확인한다.

- [x] T004 [US1] src/main/kotlin/com/look4counter/jsp/JspJavaInjector.kt에 번역 단위와 현재 파일 전용 호스트 매핑을 연결한다. (FR-001, FR-007)

## 4단계: 사용자 이야기 2 — 조각 편집

독립 검증: 유일 부모는 연결하고 다중 부모와 반복 삽입은 연결하지 않는다.

- [x] T005 [US2] src/test/kotlin/com/look4counter/jsp/JspIncludeContextTest.kt에 부모 선택 테스트를 추가한다. (FR-003, FR-004)
- [x] T006 [US2] src/main/kotlin/com/look4counter/jsp/JspIncludeContext.kt에 역그래프 캐시와 유일 부모 선택을 구현한다. (FR-003)

## 5단계: 사용자 이야기 3 — 변경과 보존

독립 검증: 커밋 편집과 생성·삭제·지시자 변경을 반영하고 다른 문서는 보존한다.

- [x] T007 [US3] src/test/kotlin/com/look4counter/jsp/JspIncludeContextTest.kt에 변경 반영·오류·완성 삽입·포매팅 보존 테스트를 추가한다. (FR-005~007)
- [x] T008 [US3] src/main/kotlin/com/look4counter/jsp/JspIncludeContext.kt에 의존성 무효화와 중첩·크기 제한을 검증·보완한다. (FR-005, FR-006)

## 6단계: 통합 검증

- [x] T009 전체 테스트·빌드·설정 검증 후 specs/007-include-context/quickstart.md에 결과를 기록한다. (SC-001~004)
- [x] T010 README.md에 정적 include, 웹 루트 및 부모 선택 정책을 기록한다. (FR-001~007)

## 의존성과 실행 전략

T001 → T002 → T003 → T004 → T005 → T006 → T007 → T008 → T009 → T010 순서로 수행한다. 경로와 주입 검토는 별도 파일이어서 병행 가능하지만 구현은 순차 진행한다. 포함 방향을 먼저 검증하고 조각 편집과 오류 복구로 확장한다.

## 7단계: test 메서드 완성 누락 재현

- [x] T011 [US1] src/test/kotlin/com/look4counter/jsp/JspIncludeContextTest.kt에 실제 HTML 페이지와 include.jsp의 반환값·void 및 호출 위치별 test 완성을 재현한다. (FR-008)
- [x] T012 [US1] 정상 Java 호출 위치에서 누락이 재현되면 src/main/kotlin/com/look4counter/jsp/JspJavaInjector.kt와 JspIncludeContext.kt의 원인을 수정하고, 재현되지 않으면 Java 후보 필터링 결과를 specs/007-include-context/quickstart.md에 기록한다. (FR-008)
- [x] T013 추가 회귀 테스트와 필요한 빌드를 실행하고 specs/007-include-context/quickstart.md에 실행 결과와 한계를 기록한다. (FR-008, SC-004)

후속 작업은 T011 → T012 → T013 순서로 수행한다. 기존 1~6단계의 완료 결과는 유지한다.



