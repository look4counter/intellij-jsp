# 구현 계획: JSP 자바 편집

**작성일**: 2026-10-20 | **명세**: [spec.md](spec.md)

## 요약

Java 플러그인의 MultiHostInjector로 기존 완성·강조·포매터를 재사용한다. 자바 영역을 복합 PSI 호스트로 만들고 실행 조각을 하나의 가상 메서드에 연결한다. 선언 조각은 별도 클래스 본문 주입으로 편집하고 실행 문맥에는 선언 사본을 제공한다. 주입 호스트는 항상 원문 순서를 유지한다. JSP 구분자와 HTML 위치는 유지한다.

## 기술 문맥

- Kotlin/JVM, 기존 Gradle, IntelliJ IDEA 2026.1.3 및 Java 번들 플러그인을 사용한다.
- 저장소는 없으며 열린 JSP 파일 단위로 PSI 캐시를 활용한다.
- 기존 플랫폼 fixture 및 신규 통합 테스트, 패키징으로 검증한다.
- 외부 다운로드 제한 시 기존 로컬 호환 테스트 도구를 사용하고 제한을 기록한다.

## 헌장 점검

헌장은 미작성 템플릿이므로 규범 검사는 해당 없음. 한글 문서 및 speckit 순서를 준수한다. 설계 전후 위반 없음.

## 연구 및 설계

[연구](research.md), [모델](data-model.md), [편집 계약](contracts/java-editing.md), [검증](quickstart.md)을 따른다.

## 파일 구성

- build.gradle.kts 및 src/main/resources/META-INF/plugin.xml: Java 의존성과 주입 등록.
- src/main/kotlin/com/look4counter/jsp/JspParserDefinition.kt: 복합 호스트 생성.
- src/main/kotlin/com/look4counter/jsp/JspJavaInjectionHost.kt: 원문 escaper와 갱신.
- src/main/kotlin/com/look4counter/jsp/JspJavaInjector.kt: import, 내장 객체, 선언 및 실행 조각 연결.
- src/main/kotlin/com/look4counter/jsp/JspJavaFormatting.kt: HTML 포매팅 전 Java 사본을 재정렬하고 선택된 호스트 본문에만 결과를 반영하는 전처리.
- src/main/kotlin/com/look4counter/jsp/JspJavaSyntaxHighlighter.kt 및 JspEditorHighlighterProvider.kt: 구분자를 보존하는 Java 렉서 계층.
- src/test/kotlin/com/look4counter/jsp/JspJavaEditingTest.kt: 신규 통합 검증.
- 기존 포매팅 테스트: 자바 원문 보존에서 재정렬 계약으로 갱신.

## 단계

1. 의존성과 호스트 기반을 추가한다.
2. 자동완성 및 문맥 연결을 구현한다.
3. 강조 및 포매팅을 검증한다.
4. 회귀 검증, 패키징, 문서 갱신과 수렴 점검을 수행한다.
