# 구현 계획: JSP Java 영역 배경 제거

**작성일**: 2026-10-21 | **명세**: [spec.md](spec.md)

## 요약과 기술 문맥

Kotlin/JVM IntelliJ 플러그인과 기존 로컬 261 SDK를 사용한다. 선언부 전용 배경 억제를 공통 Java 주입 호스트로 옮겨 스크립틀릿·표현식에도 적용한다. 새 의존성·설정·외부 통신과 추가 순회는 없다.

## 헌장 점검

헌장은 미작성 템플릿으로 검사를 생략한다. 한글 문서 및 speckit 절차를 준수하며 설계 전후 충돌은 없다.

## 구조와 설계

- `src/main/kotlin/com/look4counter/jsp/JspJavaInjectionHost.kt`: 공통 호스트에 `InjectionBackgroundSuppressor`를 구현하고 선언부 전용 하위 클래스를 제거한다.
- `src/main/kotlin/com/look4counter/jsp/JspParserDefinition.kt`: 모든 Java 영역을 공통 호스트로 생성한다.
- `src/test/kotlin/com/look4counter/jsp/JspDeclarationBackgroundTest.kt`: 기존 검증을 확장해 세 종류 모두 배경 억제, Java 주입, 원문 보존, 빈·미완성 본문과 갱신을 확인한다.

## 위험과 검증

플랫폼 구현 패키지 마커 사용은 기존과 동일하다. 기존 플랫폼 배경 판단 API 테스트를 확장하고 전체 테스트 및 플러그인 빌드·설정 검증을 실행한다. 실제 화면 테마 확인 여부는 별도 기록한다.
