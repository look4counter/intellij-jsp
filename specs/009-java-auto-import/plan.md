# 구현 계획: Java import 자동 추가

**작성일**: 2026-10-20 | **명세**: [spec.md](spec.md)

## 요약과 기술 문맥

Kotlin/JVM, IntelliJ 2026.1.3 및 기존 Java 주입을 사용한다. 외부 의존성·저장소는 추가하지 않는다. Java 완성 후보를 유지하면서 JSP에서 클래스 삽입만 감싸고, 현재 원문에 page import를 기록한다. 미해결 참조 빠른 수정 제공자가 클래스별 동작을 등록한다. 후보 검색은 프로젝트 해석 범위와 접근성 검사를 사용하며 인덱싱 중 생략한다.

## 헌장 점검

헌장은 미작성 템플릿이다. 한글 문서·speckit 순서를 준수한다. 설계 전후 위반 없음.

## 설계와 변경 파일

- `src/main/kotlin/com/look4counter/jsp/JspJavaImports.kt`: 공통 문맥 판별, 중복·충돌 판단, 현재 파일 지시자 추가와 참조 편집.
- `JspJavaImportCompletionContributor.kt`: Java 완성 후보의 클래스 삽입을 장식한다. 기존 후보 정렬·표시를 유지하고 생성자 괄호를 지원한다.
- `JspJavaImportQuickFixProvider.kt`: 미해결 Java 클래스 후보마다 패키지가 표시된 빠른 수정 등록.
- `src/main/resources/META-INF/plugin.xml`: 완성·미해결 참조 수정 확장 등록.
- `src/test/kotlin/com/look4counter/jsp/JspJavaImportTest.kt`: 플랫폼 완성·빠른 수정과 원문·해석·실행 취소 검증.
- `README.md`: 사용자 동작과 제외 범위 안내.

## 제약과 단계

가상 Java import 영역은 실제 편집 영역이 아니므로 기존 Java import 삽입기를 호출하지 않는다. 클래스 해석과 접근성은 기존 Java 엔진을 사용한다. 와일드카드로 해석 중인 다른 참조도 보존하도록 보수적으로 충돌을 판단한다. 지시자 추가는 참조 삽입 후 마지막에 수행하고 호스트 문서의 커서·범위 추적을 유지한다. 현재 파일 외에는 수정하지 않는다.

1. 재현 테스트와 공통 편집 정책을 작성한다.
2. 클래스 완성 삽입과 생성자 처리를 연결한다.
3. 빠른 수정과 후보 접근성 검증을 연결한다.
4. 중복·충돌·include·비대상·실행 취소 회귀를 검증한다.
5. 전체 테스트·패키징·수렴 점검을 완료한다.

## 설계 산출물

[연구](research.md), [데이터 모델](data-model.md), [편집 계약](contracts/editor.md), [검증 안내](quickstart.md).
