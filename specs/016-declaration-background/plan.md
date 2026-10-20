# 구현 계획: 선언부 배경 제거

**작성일**: 2026-10-21 | **명세**: [spec.md](spec.md)

## 요약

선언부 전용 Java 주입 호스트에 플랫폼의 배경 억제 마커를 적용한다. 기존 Java 편집 기능과 다른 호스트의 배경 정책을 유지한다.

## 기술 문맥

Kotlin/JVM IntelliJ 플러그인, 로컬 IntelliJ 261 SDK, 기존 Gradle 및 BasePlatformTestCase를 사용한다. 새 의존성·저장소·외부 통신·설정은 없다. 호스트 생성 시 타입만 분기하므로 추가 순회는 없다. 범위는 선언부의 주입 배경 정책으로 제한한다.

## 헌장 점검

헌장은 미작성 템플릿이므로 원칙 검사는 생략한다. 한글 문서와 speckit 단계 지침을 준수한다. 조사 전후와 설계 후 충돌이 없다.

## 구조와 설계

- `src/main/kotlin/com/look4counter/jsp/JspJavaInjectionHost.kt`: 기존 호스트를 상속 가능하게 하고 선언부 전용 하위 클래스에 `InjectionBackgroundSuppressor`를 구현한다.
- `src/main/kotlin/com/look4counter/jsp/JspParserDefinition.kt`: 첫 자식 토큰이 선언부인 Java 호스트에만 전용 클래스를 생성한다.
- `src/test/kotlin/com/look4counter/jsp/JspDeclarationBackgroundTest.kt`: 플랫폼 배경 판단 API와 주입 PSI, 호스트 갱신, 다른 Java 호스트 정책을 검증한다.
- 현재 기능 문서: 명세, 조사, 데이터 구조, 표시 계약, 검증 안내와 작업 목록을 사용한다.

## 위험과 검증

플랫폼 구현 패키지의 마커에 의존하므로 대상 SDK에서 컴파일과 동작 검증을 수행한다. 공통 호스트 검색과 치환이 유지되는지 확인하고 전체 기존 테스트, 플러그인 빌드 및 설정 검증을 실행한다. 실제 화면 테마 검증 여부는 결과에 별도로 기록한다.
