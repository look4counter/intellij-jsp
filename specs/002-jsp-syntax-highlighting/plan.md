# 구현 계획: JSP 혼합 구문 강조

**작성일**: 2026-10-19 | **명세**: [spec.md](spec.md)
**기능 브랜치**: 없음

## 요약

기본 에디터를 유지하고 파일 유형을 LanguageFileType으로 전환한다. JSP 구간 lexer와 HTML 레이어를 결합한 EditorHighlighterProvider를 등록한다. HTML 레이어가 기존 JS·CSS 플러그인의 임베디드 강조 기능을 자동 활용한다.

## 기술 환경

- Kotlin 2.3.20, Gradle 9.7.1, IntelliJ IDEA 2025.3.6.1, JDK 21.
- 필수 API: 플랫폼 및 XML/HTML 모듈. JS·CSS는 선택적 기능이며 해당 클래스를 직접 참조하지 않는다.
- 저장: 기존 문서와 파일 저장 기능. 강조기는 원문을 변경하지 않는다.
- 검증: JUnit lexer 경계 검사, 플랫폼 기반 실제 HTML 레이어 및 문서 편집 검사, 실제 JS·CSS 연결 및 임베디드 제공자를 제거한 미설치 상황의 비교, 빌드 및 플러그인 구성 검사.
- 규모: 단일 JSP 파일 유형의 구문 강조. PSI·파서·Java 의미 분석은 추가하지 않는다.

## 원칙 점검

헌법은 미작성 템플릿이므로 구체 원칙 검사를 생략한다. 한글 문서와 speckit 순서를 준수한다. 설계 전후 위반은 없다.

## 조사와 설계 결정

[조사](research.md)를 따른다. JSP lexer는 HTML_TEXT, 지시자, 스크립틀릿, 표현식, 선언, 주석, EL 토큰을 만든다. 각 JSP 영역은 하나의 토큰으로 반환하고 닫히지 않은 영역은 문서 끝까지 소비한다. 토큰 경계에서 상태는 0이며 어디서든 해당 토큰 시작점에서 재시작할 수 있다. EL은 인용부호·이스케이프·중괄호 깊이를 추적한다.

LayeredLexerEditorHighlighter에 HTML_TEXT 레이어를 등록하고 HtmlFileHighlighter를 재사용한다. 구간 사이 구분자는 빈 문자열로 해 JSP를 제외한 호스트 문맥을 연결한다. JS·CSS는 HtmlEmbeddedContentSupport 확장점으로 기존 지원이 있을 때 연결되므로 필수 플러그인 의존성은 추가하지 않는다. 테스트 구성에서만 웹 플러그인을 선택적으로 활성화한다.

JSP 종류별 TextAttributesKey는 기본 언어 색상 키를 대체값으로 사용한다. HTML·JS·CSS 키를 다시 정의하지 않는다. 기존 파일 유형 이름·확장자·저장 동작을 유지한다.

## 프로젝트 구조

- `src/main/kotlin/com/look4counter/jsp/JspLanguage.kt`와 `JspFileType.kt`: 전용 언어 식별자와 언어 파일 유형 연결.
- `src/main/kotlin/com/look4counter/jsp/JspHighlightingLexer.kt`: 경계 분리와 토큰 종류.
- `src/main/kotlin/com/look4counter/jsp/JspSyntaxHighlighter.kt`: JSP 색상 키.
- `src/main/kotlin/com/look4counter/jsp/JspEditorHighlighterProvider.kt`: HTML 레이어 결합.
- `src/main/resources/META-INF/plugin.xml`: XML 의존성과 강조기 등록.
- `build.gradle.kts`: XML 모듈 및 선택적인 웹 플러그인 테스트 구성.
- `src/test/kotlin/com/look4counter/jsp/`: lexer 및 실제 편집기 강조 통합 검증.
- `src/test/testData/highlighting.jsp`: 수동 검증용 혼합 언어 예제.
- `README.md`: 지원 범위와 실행 안내 갱신.
- `specs/002-jsp-syntax-highlighting/`: 명세·설계·작업·검증 기록.

## 단계

1. JSP 경계와 재시작 테스트를 작성한다.
2. JSP lexer와 색상 키를 구현한다.
3. HTML 레이어 제공자를 등록하고 실제 HTML·임베디드 언어·증분 편집을 검증한다.
4. 웹 언어 지원 유무에 따른 테스트와 패키징을 수행한다.
5. 문서 갱신 후 converge로 남은 작업을 확인한다.

## 복잡도

파일 유형의 이름과 확장자는 유지하고 전용 Language 식별자만 추가한다. 언어 파서나 PSI는 추가하지 않는다. JSP 내부 Java 코드는 단일 JSP 색상을 사용한다. HTML과 웹 언어의 토큰화·색상은 기존 구현에 위임한다.


