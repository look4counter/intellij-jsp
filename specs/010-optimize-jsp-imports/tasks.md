# 작업 목록: JSP 미사용 import 정리

현재 요구사항은 기능 제거다. 1~6단계는 철회된 추가 작업의 완료 이력이며 아래 7단계가 현재 실행 대상이다.

## 1단계: 설정

- [x] T001 src/test/kotlin/com/look4counter/jsp/JspImportOptimizerTest.kt에 미사용 제거·사용 보존·단축키 실행 재현 테스트를 작성한다. FR-001~003.

## 2단계: 공통 기반

- [x] T002 src/main/kotlin/com/look4counter/jsp/JspIncludeContext.kt에 번역 단위 완전성 판정을 추가한다. FR-004.

## 3단계: 사용자 이야기 1 — 정리 실행

독립 검증: JSP의 사용·미사용 import 목록에 액션을 실행해 원문과 실행 취소를 확인한다.

- [x] T003 [US1] src/main/kotlin/com/look4counter/jsp/JspImportOptimizer.kt에 모든 주입 영역의 미사용 분석과 원문 보존 편집을 구현한다. FR-002~005.
- [x] T004 [US1] src/main/kotlin/com/look4counter/jsp/JspOptimizeImportsAction.kt에 액션·문맥 우선순위를 구현하고 src/main/resources/META-INF/plugin.xml에 Ctrl+O와 등록을 추가한다. FR-001, FR-005, FR-006.
- [x] T005 [US1] src/test/kotlin/com/look4counter/jsp/JspImportOptimizerTest.kt에서 세 영역·여러 지시자·다른 속성·주석·문자열·실행 취소·멱등성과 키맵을 검증한다. SC-001, SC-003.

## 4단계: 사용자 이야기 2 — 보존 정책

독립 검증: include 및 불완전 문맥에서 필요한 import와 다른 파일을 보존한다.

- [x] T006 [US2] src/test/kotlin/com/look4counter/jsp/JspImportOptimizerTest.kt에서 include·다중 부모·순환·누락 경로·미완성·미해결·와일드카드·비대상 파일을 검증하고 필요한 보존 정책을 src/main/kotlin/com/look4counter/jsp/JspImportOptimizer.kt에 보완한다. FR-004, FR-006, SC-002.

## 5단계: 마무리

- [x] T007 README.md에 사용법을 추가하고 전체 테스트·빌드·설정·ZIP 검증 결과를 specs/010-optimize-jsp-imports/quickstart.md에 기록한다. FR-007, SC-004.

## 의존성과 구현 전략

T001 → T002 → T003 → T004 → T005 → T006 → T007 순서다. 최소 기능은 사용자 이야기 1이며 이야기 2의 보존 검증 후 전체 회귀를 실행한다. 테스트와 등록 파일을 공유하므로 편집은 순차 수행한다. 독립 작업을 병행한다면 T004의 액션과 T003의 분석기 검토는 공통 계약 확정 후 가능하다.

## 6단계: 수렴 점검

- [x] T008 src/main/kotlin/com/look4counter/jsp/JspImportOptimizer.kt에서 현재 파일과 include의 EL에 등장하는 클래스 이름을 보존하고 src/test/kotlin/com/look4counter/jsp/JspImportOptimizerTest.kt에 회귀 검증을 추가한다. FR-004와 지원하지 않는 사용 문맥 보존의 부분 구현을 보완한다.

## 7단계: 기능 철회

- [x] T009 [US1] src/main/kotlin/com/look4counter/jsp/JspImportOptimizer.kt, JspOptimizeImportsAction.kt와 src/test/kotlin/com/look4counter/jsp/JspImportOptimizerTest.kt를 삭제하고 src/main/resources/META-INF/plugin.xml의 정리 관련 등록을 제거한다. 현재 FR-001.
- [x] T010 [US2] src/main/kotlin/com/look4counter/jsp/JspIncludeContext.kt의 정리 전용 메타데이터를 제거하고 README.md의 정리 안내를 삭제한다. 현재 FR-002, FR-003.
- [x] T011 전체 회귀·빌드·설정·ZIP 검증 결과를 specs/010-optimize-jsp-imports/quickstart.md에 기록한다. 현재 FR-004.

현재 실행 순서는 T009 → T010 → T011이다. 앞선 작업은 다시 구현하지 않는다.
